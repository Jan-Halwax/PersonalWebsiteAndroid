package dev.halwax.personalWebsite

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.halwax.personalWebsite.navigation.AppNavigation
import dev.halwax.personalWebsite.ui.theme.PersonalWebsiteTheme
import androidx.core.net.toUri
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase initialisieren
        try {
            Firebase.initialize(this)
            Firebase.firestore
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            PersonalWebsiteTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        openWebsite = { openWebsite() }
                    )
                }
            }
        }
    }

    /**
     * Öffnet die Website im Browser
     */
    private fun openWebsite() {
        val webIntent = Intent(Intent.ACTION_VIEW, "https://halwax.dev".toUri())
        startActivity(webIntent)
    }
}