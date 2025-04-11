package dev.halwax.personalWebsite.firebase

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

/**
 * Singleton-Klasse für die Firebase-Konfiguration
 * Stellt Zugriff auf Firebase-Dienste zur Verfügung
 */
object FirebaseConfig {
    // Firebase Auth Instanz
    val auth: FirebaseAuth by lazy { Firebase.auth }

    // Firestore Instanz
    val firestore: FirebaseFirestore by lazy { Firebase.firestore }

    // Sammlung für Skills
    val skillsCollection = "skills"

    // Sammlung für Projekte
    val projectsCollection = "projects"
}