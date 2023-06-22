package com.example.ytscrobblefilter

import android.Manifest
import android.accounts.Account
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.YouTubeScopes
import com.google.api.services.youtube.model.Channel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var mCredential: GoogleAccountCredential
    private val RC_SIGN_IN = 123
    private var mService: YouTube? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    val REQUEST_ACCOUNT_PICKER = 1000
    val REQUEST_AUTHORIZATION = 1001
    val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    val REQUEST_PERMISSION_GET_ACCOUNTS = 1003

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mCredential = getCredential()


        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
        mService = YouTube.Builder(
            transport, jsonFactory, mCredential
        )
            .setApplicationName("YTScrobbleFilter")
            .build()

        // Call this method to start the sign-in process
        signIn()
        chooseAccount()
        var data: List<String?>

        scope.launch {
            val deferredData = async { dataFromApi() }

            try{
                data = deferredData.await()
                val test = 0
            }
            catch (e: UserRecoverableAuthIOException){
                startActivityForResult(e.intent,REQUEST_AUTHORIZATION)
            }
            // Use the data or update UI with the fetched data
        }

    }

    private fun mServiceInnit(credential: GoogleAccountCredential){

    }





    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }


    private fun getCredential(): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(this, listOf(YouTubeScopes.YOUTUBE_READONLY)).setBackOff(ExponentialBackOff())
    }

    private fun getResultsFromApi(): List<String?>{
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            acquireGooglePlayServices()
        }
        if (mCredential.selectedAccountName == null) {
            chooseAccount()
        }
        if (!isDeviceOnline()) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_SHORT).show()
        }


        return dataFromApi()
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


    private fun signIn() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    private fun chooseAccount() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PERMISSION_GRANTED) {
            val accountName = "Jonas"

            if (accountName != null) {

                mCredential.selectedAccount = Account("jonaskucinskas2@gmail.com", "com.example.ytscrobblefilter")
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    1000
                )
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            requestPermissions(arrayOf(Manifest.permission.GET_ACCOUNTS),1003)

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Handle successful sign-in
                val account = task.getResult(ApiException::class.java)
                // Use the account for further API requests or store the access token
                val accessToken = account?.idToken
                Log.e("TAG", "Sign-in succeeded")
            } catch (e: ApiException) {
                // Handle sign-in failure
                Log.e("TAG", "Sign-in failed", e)
            }
        }

        if (requestCode == REQUEST_AUTHORIZATION) {
            signIn()
        }
    }

    private fun dataFromApi(): List<String?>{
        // Get a list of up to 10 files.
        val channelInfo: MutableList<String?> = ArrayList()
        val result = mService!!.channels().list("snippet,contentDetails,statistics")
            .setForUsername("GoogleDevelopers")
            .execute()
        val channels: List<Channel>? = result.items
        if (channels != null) {
            val channel: Channel = channels[0]
            channelInfo.add(
                "This channel's ID is " + channel.getId() + ". " +
                        "Its title is '" + channel.snippet.title + ", " +
                        "and it has " + channel.statistics.viewCount + " views."
            )
        }
        return channelInfo
    }
}
