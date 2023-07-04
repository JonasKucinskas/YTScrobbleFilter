package com.example.ytscrobblefilter

import android.Manifest
import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.Channel
import com.google.api.services.youtube.model.ChannelListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mService: YouTube
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ytUtils = YTUtils(this)
        ytUtils.getCredential()
        checkPermissions()

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        var userEmail = sharedPreferences.getString("email", null)

        if (userEmail == null){
            signIn()
            userEmail = sharedPreferences.getString("email", null)
        }
        ytUtils.mCredential.selectedAccount = Account(userEmail, "com.example.ytscrobblefilter")

        val intent = Intent(this, NotifListenerService::class.java)

        startService(intent)
    }

    override fun onStart() {
        super.onStart()

        var data: List<String?>? = null
        val isSong: Boolean? = null


        val button = findViewById<Button>(R.id.button)
        val text = findViewById<TextView>(R.id.text)

        button.setOnClickListener {

            button.text = "Working."

            //Need to start child job in order to use job.cancelAndJoin,
            //once child job is canceled, main one is canceled too,
            scope.launch {
                val job = launch {
                    ensureActive()
                    Log.i("Coroutine", "Working, ${this.coroutineContext}.")
                    data = dataFromApi()
                    //isSong = isSong()
                    Log.i("Coroutine", "Done, ${this.coroutineContext}.")

                }

                delay(1000L)
                Log.i("Coroutine", "Canceling.")
                job.cancelAndJoin()
                Log.i("Coroutine", "Canceled.")


                withContext(Dispatchers.Main) {
                    text.text = isSong.toString()
                    button.text = getString(R.string.button_text)
                }
            }
        }
    }





    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        val data: Intent? = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)

                //save user info
                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("email", account.email)
                editor.apply()

                Log.i("TAG", "Sign-in succeeded")
            } catch (e: ApiException) {
                Log.e("TAG", "Sign-in failed", e)
            }
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signInLauncher.launch(googleSignInClient.signInIntent)
    }


    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }



    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }

    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@MainActivity,
            connectionStatusCode,
            1002)
        dialog!!.show()
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PERMISSION_DENIED) {
            requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS), 1003)
        }
    }

    private fun dataFromApi(): List<String?>{
        // Get a list of up to 10 files.
        val channelInfo: MutableList<String?> = ArrayList()
        var result: ChannelListResponse? = null
        try{
             result = mService!!.channels().list("snippet,contentDetails,statistics")
                .setForUsername("GoogleDevelopers")
                .execute()
        }
        catch (e: Exception){
            Log.e("API", e.message.toString())
        }

        val channels: List<Channel>? = result?.items
        if (channels != null) {
            val channel: Channel = channels[0]
            channelInfo.add(
                "This channel's ID is " + channel.id + ". " +
                        "Its title is '" + channel.snippet.title + ", " +
                        "and it has " + channel.statistics.viewCount + " views."
            )
        }
        return channelInfo
    }
}
