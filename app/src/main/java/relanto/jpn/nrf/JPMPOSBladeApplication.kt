package relanto.jpn.nrf

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JPMPOSBladeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-level configurations here
    }
}
