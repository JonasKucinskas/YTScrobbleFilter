package com.example.ytscrobblefilter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException




class MainActivity : AppCompatActivity() {

    private val SCOPES = listOf("https://www.googleapis.com/auth/youtube.readonly")
    private val APPLICATION_NAME = "YTScrobbleFilter"
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Call this method to start the sign-in process
        signIn()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
    }
}

