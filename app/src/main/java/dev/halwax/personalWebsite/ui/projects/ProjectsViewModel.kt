package dev.halwax.personalWebsite.ui.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.halwax.personalWebsite.model.Feature
import dev.halwax.personalWebsite.model.Project
import dev.halwax.personalWebsite.repository.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel für den Projekte-Bildschirm
 * Kapselt die Verwaltung der Projekte und die zugehörige UI-Logik
 */
class ProjectsViewModel(
    private val projectRepository: ProjectRepository = ProjectRepository()
) : ViewModel() {

    // UI-Status
    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    // Aktuelle/Portfolio-Projekte
    private val _currentProjects = MutableStateFlow<List<Project>>(emptyList())
    val currentProjects: StateFlow<List<Project>> = _currentProjects.asStateFlow()

    private val _portfolioProjects = MutableStateFlow<List<Project>>(emptyList())
    val portfolioProjects: StateFlow<List<Project>> = _portfolioProjects.asStateFlow()

    // Formularfelder für Hinzufügen/Bearbeiten
    private val _projectName = MutableStateFlow("")
    val projectName: StateFlow<String> = _projectName.asStateFlow()

    private val _projectStartDate = MutableStateFlow(Date())
    val projectStartDate: StateFlow<Date> = _projectStartDate.asStateFlow()

    private val _projectEndDate = MutableStateFlow<Date?>(null)
    val projectEndDate: StateFlow<Date?> = _projectEndDate.asStateFlow()

    private val _projectImageUrl = MutableStateFlow("")
    val projectImageUrl: StateFlow<String> = _projectImageUrl.asStateFlow()

    private val _projectGithubUrl = MutableStateFlow("")
    val projectGithubUrl: StateFlow<String> = _projectGithubUrl.asStateFlow()

    private val _projectAppUrl = MutableStateFlow("")
    val projectAppUrl: StateFlow<String> = _projectAppUrl.asStateFlow()

    private val _projectDescription = MutableStateFlow("")
    val projectDescription: StateFlow<String> = _projectDescription.asStateFlow()

    private val _projectIsCurrentProject = MutableStateFlow(true)
    val projectIsCurrentProject: StateFlow<Boolean> = _projectIsCurrentProject.asStateFlow()

    // Features für das aktuelle Projekt
    private val _projectFeatures = MutableStateFlow<List<Feature>>(emptyList())
    val projectFeatures: StateFlow<List<Feature>> = _projectFeatures.asStateFlow()

    // Temporäre Feature-Felder
    private val _featureTitle = MutableStateFlow("")
    val featureTitle: StateFlow<String> = _featureTitle.asStateFlow()

    private val _featureDescription = MutableStateFlow("")
    val featureDescription: StateFlow<String> = _featureDescription.asStateFlow()

    // Aktuell bearbeitetes Projekt
    private val _currentEditProject = MutableStateFlow<Project?>(null)
    val currentEditProject: StateFlow<Project?> = _currentEditProject.asStateFlow()

    init {
        loadProjects()
    }

    /**
     * Projekte aus dem Repository laden
     */
    fun loadProjects() {
        viewModelScope.launch {
            _uiState.value = ProjectsUiState.Loading

            try {
                projectRepository.loadProjects()

                projectRepository.projects.collectLatest { projects ->
                    if (projects.isEmpty()) {
                        _uiState.value = ProjectsUiState.Empty
                        _currentProjects.value = emptyList()
                        _portfolioProjects.value = emptyList()
                    } else {
                        // Projekte aufteilen
                        val currentProjects = projects.filter { it.isCurrentProject }
                        val portfolioProjects = projects.filter { !it.isCurrentProject }

                        _currentProjects.value = currentProjects
                        _portfolioProjects.value = portfolioProjects
                        _uiState.value = ProjectsUiState.Success(projects)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ProjectsUiState.Error(e.message ?: "Unbekannter Fehler")
            }
        }
    }

    /**
     * Formularfelder aktualisieren
     */
    fun updateProjectName(name: String) {
        _projectName.value = name
    }

    fun updateProjectStartDate(date: Date) {
        _projectStartDate.value = date
    }

    fun updateProjectEndDate(date: Date?) {
        _projectEndDate.value = date
    }

    fun updateProjectImageUrl(url: String) {
        _projectImageUrl.value = url
    }

    fun updateProjectGithubUrl(url: String) {
        _projectGithubUrl.value = url
    }

    fun updateProjectAppUrl(url: String) {
        _projectAppUrl.value = url
    }

    fun updateProjectDescription(description: String) {
        _projectDescription.value = description
    }

    fun updateProjectIsCurrentProject(isCurrentProject: Boolean) {
        _projectIsCurrentProject.value = isCurrentProject
    }

    fun updateFeatureTitle(title: String) {
        _featureTitle.value = title
    }

    fun updateFeatureDescription(description: String) {
        _featureDescription.value = description
    }

    /**
     * Feature zum Projekt hinzufügen
     */
    fun addFeature() {
        if (_featureTitle.value.isBlank() || _featureDescription.value.isBlank()) {
            return
        }

        val newFeature = Feature(
            title = _featureTitle.value.trim(),
            description = _featureDescription.value.trim()
        )

        _projectFeatures.update { currentFeatures ->
            currentFeatures + newFeature
        }

        // Feature-Felder zurücksetzen
        _featureTitle.value = ""
        _featureDescription.value = ""
    }

    /**
     * Feature aus dem Projekt entfernen
     */
    fun removeFeature(index: Int) {
        _projectFeatures.update { currentFeatures ->
            currentFeatures.filterIndexed { i, _ -> i != index }
        }
    }

    /**
     * Formular für ein neues Projekt zurücksetzen
     */
    fun resetProjectForm() {
        _projectName.value = ""
        _projectStartDate.value = Date()
        _projectEndDate.value = null
        _projectImageUrl.value = ""
        _projectGithubUrl.value = ""
        _projectAppUrl.value = ""
        _projectDescription.value = ""
        _projectIsCurrentProject.value = true
        _projectFeatures.value = emptyList()
        _currentEditProject.value = null
    }

    /**
     * Formular mit den Daten eines vorhandenen Projekts befüllen
     */
    fun prepareEditProject(project: Project) {
        _projectName.value = project.name
        _projectStartDate.value = project.startDate
        _projectEndDate.value = project.endDate
        _projectImageUrl.value = project.imageUrl
        _projectGithubUrl.value = project.githubUrl
        _projectAppUrl.value = project.appUrl ?: ""
        _projectDescription.value = project.description
        _projectIsCurrentProject.value = project.isCurrentProject
        _projectFeatures.value = project.features
        _currentEditProject.value = project
    }

    /**
     * Projekt speichern (neu oder aktualisieren)
     */
    fun saveProject() {
        if (_projectName.value.isBlank() ||
            _projectImageUrl.value.isBlank() ||
            _projectGithubUrl.value.isBlank() ||
            _projectDescription.value.isBlank()) {
            return
        }

        // Prüfen, ob ein Enddatum benötigt wird (für Portfolio-Projekte)
        if (!_projectIsCurrentProject.value && _projectEndDate.value == null) {
            // Ende-Datum wird für Portfolio-Projekte benötigt
            return
        }

        val project = Project(
            id = _currentEditProject.value?.id ?: "",
            name = _projectName.value.trim(),
            startDate = _projectStartDate.value,
            endDate = if (_projectIsCurrentProject.value) null else _projectEndDate.value,
            imageUrl = _projectImageUrl.value.trim(),
            githubUrl = _projectGithubUrl.value.trim(),
            appUrl = _projectAppUrl.value.takeIf { it.isNotBlank() }?.trim(),
            description = _projectDescription.value.trim(),
            isCurrentProject = _projectIsCurrentProject.value,
            features = _projectFeatures.value
        )

        viewModelScope.launch {
            val result = if (_currentEditProject.value == null) {
                projectRepository.addProject(project)
            } else {
                projectRepository.updateProject(project)
            }

            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                _uiState.value = ProjectsUiState.Error(errorMessage)
            }

            // Formular zurücksetzen
            resetProjectForm()
        }
    }

    /**
     * Projekt löschen
     */
    fun deleteProject(projectId: String) {
        viewModelScope.launch {
            val result = projectRepository.deleteProject(projectId)

            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                _uiState.value = ProjectsUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Projekt zwischen aktuell und Portfolio verschieben
     */
    fun moveProject(projectId: String, isCurrentProject: Boolean) {
        viewModelScope.launch {
            val result = projectRepository.moveProject(projectId, isCurrentProject)

            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                _uiState.value = ProjectsUiState.Error(errorMessage)
            }
        }
    }
}

/**
 * Sealed Class für den UI-Status der Projekte-Verwaltung
 */
sealed class ProjectsUiState {
    object Loading : ProjectsUiState()
    object Empty : ProjectsUiState()
    data class Success(val projects: List<Project>) : ProjectsUiState()
    data class Error(val message: String) : ProjectsUiState()
}