package dev.halwax.personalWebsite.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import dev.halwax.personalWebsite.firebase.FirebaseConfig
import dev.halwax.personalWebsite.model.Feature
import dev.halwax.personalWebsite.model.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository für Projekte
 * Implementiert das Single-Responsibility-Prinzip für die Verwaltung von Projekten
 */
class ProjectRepository {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: Flow<List<Project>> = _projects.asStateFlow()

    /**
     * Lädt alle Projekte aus Firestore
     */
    suspend fun loadProjects() {
        try {
            val projectsSnapshot = FirebaseConfig.firestore
                .collection(FirebaseConfig.projectsCollection)
                .get()
                .await()

            val projectsList = projectsSnapshot.documents.map { doc ->
                documentToProject(doc)
            }.sortedByDescending { it.startDate }

            _projects.update { projectsList }
        } catch (e: Exception) {
            // Bei Fehler bleibt die Liste unverändert
            e.printStackTrace()
        }
    }

    /**
     * Gibt aktuelle Projekte zurück
     */
    fun getCurrentProjects(): List<Project> {
        return _projects.value.filter { it.isCurrentProject }
    }

    /**
     * Gibt Portfolio-Projekte zurück
     */
    fun getPortfolioProjects(): List<Project> {
        return _projects.value.filter { !it.isCurrentProject }
    }

    /**
     * Fügt ein neues Projekt hinzu
     */
    suspend fun addProject(project: Project): Result<Project> {
        return try {
            val projectData = hashMapOf(
                "name" to project.name,
                "startDate" to project.startDate,
                "endDate" to project.endDate,
                "imageUrl" to project.imageUrl,
                "githubUrl" to project.githubUrl,
                "appUrl" to project.appUrl,
                "description" to project.description,
                "isCurrentProject" to project.isCurrentProject,
                "features" to project.features.map { feature ->
                    mapOf(
                        "title" to feature.title,
                        "description" to feature.description
                    )
                }
            )

            val docRef = FirebaseConfig.firestore
                .collection(FirebaseConfig.projectsCollection)
                .add(projectData)
                .await()

            val newProject = project.copy(id = docRef.id)

            // Liste aktualisieren
            _projects.update { currentList ->
                (currentList + newProject).sortedByDescending { it.startDate }
            }

            Result.success(newProject)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aktualisiert ein bestehendes Projekt
     */
    suspend fun updateProject(project: Project): Result<Project> {
        return try {
            val projectData = hashMapOf(
                "name" to project.name,
                "startDate" to project.startDate,
                "endDate" to project.endDate,
                "imageUrl" to project.imageUrl,
                "githubUrl" to project.githubUrl,
                "appUrl" to project.appUrl,
                "description" to project.description,
                "isCurrentProject" to project.isCurrentProject,
                "features" to project.features.map { feature ->
                    mapOf(
                        "title" to feature.title,
                        "description" to feature.description
                    )
                }
            )

            FirebaseConfig.firestore
                .collection(FirebaseConfig.projectsCollection)
                .document(project.id)
                .update(projectData as Map<String, Any>)
                .await()

            // Liste aktualisieren
            _projects.update { currentList ->
                currentList.map {
                    if (it.id == project.id) project else it
                }.sortedByDescending { it.startDate }
            }

            Result.success(project)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Löscht ein Projekt
     */
    suspend fun deleteProject(projectId: String): Result<String> {
        return try {
            FirebaseConfig.firestore
                .collection(FirebaseConfig.projectsCollection)
                .document(projectId)
                .delete()
                .await()

            // Liste aktualisieren
            _projects.update { currentList ->
                currentList.filter { it.id != projectId }
            }

            Result.success(projectId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Verschiebt ein Projekt zwischen Aktuell und Portfolio
     */
    suspend fun moveProject(projectId: String, isCurrentProject: Boolean): Result<String> {
        return try {
            FirebaseConfig.firestore
                .collection(FirebaseConfig.projectsCollection)
                .document(projectId)
                .update("isCurrentProject", isCurrentProject)
                .await()

            // Liste aktualisieren
            _projects.update { currentList ->
                currentList.map {
                    if (it.id == projectId) it.copy(isCurrentProject = isCurrentProject) else it
                }
            }

            Result.success(projectId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Konvertiert ein Firestore-Dokument in ein Projekt-Objekt
     */
    private fun documentToProject(doc: DocumentSnapshot): Project {
        val data = doc.data ?: mapOf<String, Any>()

        // Startdatum konvertieren
        val startDate = when (val dateValue = data["startDate"]) {
            is Timestamp -> dateValue.toDate()
            is Date -> dateValue
            else -> Date()
        }

        // Enddatum konvertieren (kann null sein)
        val endDate = when (val dateValue = data["endDate"]) {
            is Timestamp -> dateValue.toDate()
            is Date -> dateValue
            else -> null
        }

        // Features konvertieren
        val features = (data["features"] as? List<Map<String, Any>> ?: emptyList()).map { featureMap ->
            Feature(
                title = featureMap["title"] as? String ?: "",
                description = featureMap["description"] as? String ?: ""
            )
        }

        return Project(
            id = doc.id,
            name = data["name"] as? String ?: "",
            startDate = startDate,
            endDate = endDate,
            imageUrl = data["imageUrl"] as? String ?: "",
            githubUrl = data["githubUrl"] as? String ?: "",
            appUrl = data["appUrl"] as? String,
            description = data["description"] as? String ?: "",
            isCurrentProject = data["isCurrentProject"] as? Boolean ?: true,
            features = features
        )
    }
}