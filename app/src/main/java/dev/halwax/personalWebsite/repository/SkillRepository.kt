package dev.halwax.personalWebsite.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import dev.halwax.personalWebsite.firebase.FirebaseConfig
import dev.halwax.personalWebsite.model.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository für Skills
 * Implementiert das Single-Responsibility-Prinzip für die Verwaltung von Skills
 */
class SkillRepository {

    private val _skills = MutableStateFlow<List<Skill>>(emptyList())
    val skills: Flow<List<Skill>> = _skills.asStateFlow()

    /**
     * Lädt alle Skills aus Firestore
     */
    suspend fun loadSkills() {
        try {
            val skillsSnapshot = FirebaseConfig.firestore
                .collection(FirebaseConfig.skillsCollection)
                .get()
                .await()

            val skillsList = skillsSnapshot.documents.map { doc ->
                documentToSkill(doc)
            }.sortedByDescending { it.date }

            _skills.update { skillsList }
        } catch (e: Exception) {
            // Bei Fehler bleibt die Skills-Liste unverändert
            e.printStackTrace()
        }
    }

    /**
     * Fügt einen neuen Skill hinzu
     */
    suspend fun addSkill(skill: Skill): Result<Skill> {
        return try {
            val skillData = hashMapOf(
                "name" to skill.name,
                "date" to skill.date,
                "iconUrl" to skill.iconUrl,
                "isLearning" to skill.isLearning
            )

            val docRef = FirebaseConfig.firestore
                .collection(FirebaseConfig.skillsCollection)
                .add(skillData)
                .await()

            val newSkill = skill.copy(id = docRef.id)

            // Liste aktualisieren
            _skills.update { currentList ->
                (currentList + newSkill).sortedByDescending { it.date }
            }

            Result.success(newSkill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualisiert einen bestehenden Skill
     */
    suspend fun updateSkill(skill: Skill): Result<Skill> {
        return try {
            val skillData = hashMapOf(
                "name" to skill.name,
                "date" to skill.date,
                "iconUrl" to skill.iconUrl,
                "isLearning" to skill.isLearning
            )

            FirebaseConfig.firestore
                .collection(FirebaseConfig.skillsCollection)
                .document(skill.id)
                .update(skillData as Map<String, Any>)
                .await()

            // Liste aktualisieren
            _skills.update { currentList ->
                currentList.map {
                    if (it.id == skill.id) skill else it
                }.sortedByDescending { it.date }
            }

            Result.success(skill)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Löscht einen Skill
     */
    suspend fun deleteSkill(skillId: String): Result<String> {
        return try {
            FirebaseConfig.firestore
                .collection(FirebaseConfig.skillsCollection)
                .document(skillId)
                .delete()
                .await()

            // Liste aktualisieren
            _skills.update { currentList ->
                currentList.filter { it.id != skillId }
            }

            Result.success(skillId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Konvertiert ein Firestore-Dokument in ein Skill-Objekt
     */
    private fun documentToSkill(doc: DocumentSnapshot): Skill {
        val data = doc.data ?: mapOf<String, Any>()

        // Datum aus Timestamp oder Date konvertieren
        val date = when (val dateValue = data["date"]) {
            is Timestamp -> dateValue.toDate()
            is Date -> dateValue
            else -> Date()
        }

        return Skill(
            id = doc.id,
            name = data["name"] as? String ?: "",
            date = date,
            iconUrl = data["iconUrl"] as? String ?: "",
            isLearning = data["isLearning"] as? Boolean ?: false
        )
    }
}