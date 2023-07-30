package com.example.ytscrobblefilter

import android.accounts.Account
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException



class MainActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val ytUtils = YTUtils(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()


    }

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        val data: Intent = result.data ?: return@registerForActivityResult

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)

            //save user email
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("email", account.email)
            editor.apply()
            //

            ytUtils.mCredential.selectedAccount = Account(account.email, "com.example.ytscrobblefilter")


            Log.i("TAG", "Sign-in succeeded")
        } catch (e: ApiException) {
            Log.e("TAG", "Sign-in failed", e)
        }
    }

    private var notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.packageName)) {
            Log.e("Notification listener permission", "Denied")
            finishAffinity()//close the app
        }
        else
        {
            Log.i("Notification listener permission", "Granted")
            ytUtils.getCredential()

            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val userEmail = sharedPreferences.getString("email", null)

            if (userEmail == null){
                signIn()
            }

            val intent = Intent(this, NotifListenerService::class.java)

            startService(intent)
        }
    }

    private fun signIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun checkPermissions(){

        //check for notification access
        if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.packageName)) {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog(){

        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.permission_dialog_message)
            .setPositiveButton(R.string.permission_dialog_approved_button,
                DialogInterface.OnClickListener { dialog, id ->
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    notificationPermissionLauncher.launch(intent)
                })
            .setNegativeButton(R.string.permission_dialog_denied_button,
                DialogInterface.OnClickListener { dialog, id ->
                    finishAffinity()//close the app
                })
        builder.create()
        builder.show()
    }
}