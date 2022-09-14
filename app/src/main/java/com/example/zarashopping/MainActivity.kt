package com.example.zarashopping

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.telecom.TelecomManager
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.zarashopping.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * 1 Firebase Console:
 *  1.1 Register the application
 *  1.2 google.json file (save it in our app folder)
 *  1.3 Services to handle incoming messages
 *  1.4 Notification Manager
 */

/**
 * Main Screen
 */
class MainActivity : AppCompatActivity() {
// 1 Create object
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var firebaseAnalytics: FirebaseAnalytics


    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Switch to AppTheme for displaying the activity
        //setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtain the FirebaseAnalytics instance.
        firebaseAnalytics = Firebase.analytics

        var buddle = Bundle()
        buddle.putString("APP_LAUNCHED", Telephony.Carriers.NAME)
        buddle.putInt("TimeSpend", 10)

        firebaseAnalytics.logEvent("APP_LAUNCHED", buddle)



        var button = findViewById<Button>(R.id.btnFetch)
        button.setOnClickListener(){
            fetchRemoteConfigValues()
        }


        remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 36
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

         // assign default values
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)


        /**
         * Push Notification setting
         */
        // Get token
        if (checkGooglePlayServices()) {
            // [START retrieve_current_token]
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, getString(R.string.token_error), task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result
                    Log.d(TAG, token)

                    // Log and toast
                    val msg = getString(R.string.token_prefix, token)
                    Log.d(TAG, msg)
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                })
            // [END retrieve_current_token]
        } else {
            //You won't be able to send notifications to this device
            Log.w(TAG, "Device doesn't have google play services")
        }
    }

    /**
     * Get values from Firebase Remote Config API
     */
        fun fetchRemoteConfigValues(){
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this){
                    // successful , get and apply to UI
                    if(it.isSuccessful){
                          val updated = it.result
                          Log.i("Remote", "$updated")
                           val bg_color: String = remoteConfig.getString("backgrdcolor")
                           var layout = findViewById<ConstraintLayout>(R.id.layout)

                           layout.setBackgroundColor(Color.parseColor(bg_color))



                    }else{
                      // error
                    }
                }

        }

    companion object {

        private const val TAG = "MainActivity"

    }
    // [END display_welcome_message]
    /**
     * Push notification
     */
    private fun checkGooglePlayServices(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return if (status != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Error")
            // ask user to update google play services.
            false
        } else {
            Log.i(TAG, "Google play services updated")
            true
        }
    }


}
