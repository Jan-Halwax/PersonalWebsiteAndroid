package dev.halwax.personalWebsite

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Application-Klasse für die App
 * Initialisiert Firebase bei App-Start
 */
class PersonalWebsiteApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Firebase initialisieren
        FirebaseApp.initializeApp(this)
    }
}