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
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.Channel
import com.google.api.services.youtube.model.ChannelListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mCredential: GoogleAccountCredential
    private var mService: YouTube? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val REQUEST_GOOGLE_PLAY_SERVICES = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCredential = getCredential()
        mServiceInnit()

        checkPermissions()

        if (mCredential.selectedAccountName == null) {
            signIn()

        }
    }

    override fun onStart() {
        super.onStart()

        var data: List<String?>? = null

        val button = findViewById<Button>(R.id.button)
        val text = findViewById<TextView>(R.id.text)

        button.setOnClickListener {

            button.text = "Working."

            //Need to start child job in order to use job.cancelAndJoin,
            //once child job is canceled, main one is canceled too,
            scope.launch {
                val job = launch {
                    ensureActive()
                    Log.e("Coroutine", "Working, ${this.coroutineContext}.")
                    data = dataFromApi()
                    Log.e("Coroutine", "Done, ${this.coroutineContext}.")

                }

                //delay(1000L)
                Log.e("Coroutine", "Canceling.")
                job.cancelAndJoin()
                Log.e("Coroutine", "Canceled.")

                if (data != null && data!!.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        text.text = data!![0]
                        button.text = "Done."
                    }
                }
            }
        }
    }

    //TODO user information isn't saved, so this is launched every time app is launched.
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        val data: Intent? = result.data
        if (data != null) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Handle successful sign-in
                val account = task.getResult(ApiException::class.java)
                mCredential.selectedAccount = Account(account.email, "com.example.ytscrobblefilter")
                // Use the account for further API requests or store the access token
                Log.e("TAG", "Sign-in succeeded")
            } catch (e: ApiException) {
                // Handle sign-in failure
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

    private fun mServiceInnit(){
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
        mService = YouTube.Builder(
            transport, jsonFactory, mCredential
        )
            .setApplicationName("YTScrobbleFilter")
            .build()
    }
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun getCredential(): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(this, listOf(YouTubeScopes.YOUTUBE_READONLY)).setBackOff(ExponentialBackOff())
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
            REQUEST_GOOGLE_PLAY_SERVICES)
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

    private suspend fun dataFromApi(): List<String?>{
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
