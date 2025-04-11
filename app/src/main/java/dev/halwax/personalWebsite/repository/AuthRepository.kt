package dev.halwax.personalWebsite.repository

import dev.halwax.personalWebsite.firebase.FirebaseConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository für die Authentifizierung
 * Implementiert das Single-Responsibility-Prinzip für Authentifizierungs-Operationen
 */
class AuthRepository {

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    init {
        // Initialer Auth-Status
        FirebaseConfig.auth.addAuthStateListener { auth ->
            _isSignedIn.value = auth.currentUser != null
        }
    }

    /**
     * Anmeldung mit E-Mail und Passwort
     */
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            FirebaseConfig.auth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Abmeldung des aktuellen Benutzers
     */
    fun signOut() {
        FirebaseConfig.auth.signOut()
    }

    /**
     * Prüft, ob ein Benutzer angemeldet ist
     */
    fun isUserSignedIn(): Boolean {
        return FirebaseConfig.auth.currentUser != null
    }
}