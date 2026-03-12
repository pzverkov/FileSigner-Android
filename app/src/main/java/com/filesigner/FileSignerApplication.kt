package com.filesigner

import android.app.Application
import com.filesigner.util.SanitizedDebugTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class FileSignerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(SanitizedDebugTree())
        }
    }
}
