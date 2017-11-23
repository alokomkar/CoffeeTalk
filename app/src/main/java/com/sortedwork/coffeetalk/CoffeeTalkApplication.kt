package com.sortedwork.coffeetalk

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.storage.UploadTask
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterConfig
import uk.co.chrisjenx.calligraphy.CalligraphyConfig
import java.io.File

/**
 * Created by Alok on 23/11/17.
 */
class CoffeeTalkApplication : Application() {

    lateinit var uploadTasks: HashMap<String, UploadTask>

    override fun onCreate() {
        super.onCreate()
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Quicksand-Medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build())
        instance = this
        uploadTasks = HashMap()
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
        val config = TwitterConfig.Builder(this)
                .logger(DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(TwitterAuthConfig(getString(R.string.twitter_consumer_key), getString(R.string.twitter_consumer_secret)))
                .debug(true)
                .build()
        Twitter.initialize(config)
        // Setup handler for uncaught exceptions.
        //Thread.setDefaultUncaughtExceptionHandler { thread, e -> handleUncaughtException(thread, e) }
    }

    private fun handleUncaughtException(thread: Thread?, ex: Throwable?) {
        if (ex != null) {
            Log.e("ExceptionHandler", ex.message)
            ex.printStackTrace()
        }
        Log.d("ExceptionHandler", "Restarted App")
        val mStartActivity = Intent(instance, SplashActivity::class.java)
        val mPendingIntentId = 123456
        val mPendingIntent = PendingIntent.getActivity(instance, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT)
        val mgr = instance.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
        System.exit(1)
    }


    companion object {

        /*var fitLinksPreference : FitLinksPreference ? = null
        fun getPreferences() : FitLinksPreference {
            if( fitLinksPreference == null ) fitLinksPreference = FitLinksPreference(instance)
            return fitLinksPreference as FitLinksPreference
        }*/

        lateinit var instance: CoffeeTalkApplication
            private set
    }

    fun clearApplicationData() {
        try {
            val cacheDirectory = cacheDir
            val applicationDirectory = File(cacheDirectory.parent)
            if (applicationDirectory.exists()) {
                val fileNames = applicationDirectory.list()
                for (fileName in fileNames) {
                    if (fileName != "lib") {
                        deleteFile(File(applicationDirectory, fileName))
                    }
                }
            }
        } catch ( e : Exception ) {
            e.printStackTrace()
        }

    }

    private fun deleteFile(file: File?): Boolean {
        try {
            var deletedAll = true
            if (file != null) {
                if (file.isDirectory()) {
                    val children = file.list()
                    for (i in children.indices) {
                        deletedAll = deleteFile(File(file, children[i])) && deletedAll
                    }
                } else {
                    deletedAll = file.delete()
                }
            }

            return deletedAll
        } catch ( e: Exception ) {
            e.printStackTrace()
            return true
        }

    }


}