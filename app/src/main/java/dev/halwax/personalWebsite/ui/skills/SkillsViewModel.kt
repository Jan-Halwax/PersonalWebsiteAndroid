package dev.halwax.personalWebsite.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.halwax.personalWebsite.model.Skill
import dev.halwax.personalWebsite.repository.SkillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel für den Skills-Bildschirm
 * Kapselt die Verwaltung der Skills und die zugehörige UI-Logik
 */
class SkillsViewModel(
    private val skillRepository: SkillRepository = SkillRepository()
) : ViewModel() {

    // UI-Status
    private val _uiState = MutableStateFlow<SkillsUiState>(SkillsUiState.Loading)
    val uiState: StateFlow<SkillsUiState> = _uiState.asStateFlow()

    // Formularfelder für Hinzufügen/Bearbeiten
    private val _skillName = MutableStateFlow("")
    val skillName: StateFlow<String> = _skillName.asStateFlow()

    private val _skillDate = MutableStateFlow(Date())
    val skillDate: StateFlow<Date> = _skillDate.asStateFlow()

    private val _skillIconUrl = MutableStateFlow("")
    val skillIconUrl: StateFlow<String> = _skillIconUrl.asStateFlow()

    private val _skillIsLearning = MutableStateFlow(false)
    val skillIsLearning: StateFlow<Boolean> = _skillIsLearning.asStateFlow()

    // Aktuell bearbeiteter Skill
    private val _currentEditSkill = MutableStateFlow<Skill?>(null)
    val currentEditSkill: StateFlow<Skill?> = _currentEditSkill.asStateFlow()

    init {
        loadSkills()
    }

    /**
     * Skills aus dem Repository laden
     */
    fun loadSkills() {
        viewModelScope.launch {
            _uiState.value = SkillsUiState.Loading

            // Skills vom Repository abonnieren
            try {
                skillRepository.loadSkills()

                skillRepository.skills.collectLatest { skills ->
                    _uiState.value = if (skills.isEmpty()) {
                        SkillsUiState.Empty
                    } else {
                        SkillsUiState.Success(skills)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = SkillsUiState.Error(e.message ?: "Unbekannter Fehler")
            }
        }
    }

    /**
     * Formularfelder aktualisieren
     */
    fun updateSkillName(name: String) {
        _skillName.value = name
    }

    fun updateSkillDate(date: Date) {
        _skillDate.value = date
    }

    fun updateSkillIconUrl(iconUrl: String) {
        _skillIconUrl.value = iconUrl
    }

    fun updateSkillIsLearning(isLearning: Boolean) {
        _skillIsLearning.value = isLearning
    }

    /**
     * Formular für ein neues Skill zurücksetzen
     */
    fun resetSkillForm() {
        _skillName.value = ""
        _skillDate.value = Date()
        _skillIconUrl.value = ""
        _skillIsLearning.value = false
        _currentEditSkill.value = null
    }

    /**
     * Formular mit den Daten eines vorhandenen Skills befüllen
     */
    fun prepareEditSkill(skill: Skill) {
        _skillName.value = skill.name
        _skillDate.value = skill.date
        _skillIconUrl.value = skill.iconUrl
        _skillIsLearning.value = skill.isLearning
        _currentEditSkill.value = skill
    }

    /**
     * Skill speichern (neu oder aktualisieren)
     */
    fun saveSkill() {
        if (_skillName.value.isBlank() || _skillIconUrl.value.isBlank()) {
            return
        }

        val skill = Skill(
            id = _currentEditSkill.value?.id ?: "",
            name = _skillName.value.trim(),
            date = _skillDate.value,
            iconUrl = _skillIconUrl.value.trim(),
            isLearning = _skillIsLearning.value
        )

        viewModelScope.launch {
            val result = if (_currentEditSkill.value == null) {
                skillRepository.addSkill(skill)
            } else {
                skillRepository.updateSkill(skill)
            }

            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                _uiState.value = SkillsUiState.Error(errorMessage)
            }

            // Formular zurücksetzen
            resetSkillForm()
        }
    }

    /**
     * Skill löschen
     */
    fun deleteSkill(skillId: String) {
        viewModelScope.launch {
            val result = skillRepository.deleteSkill(skillId)

            if (result.isFailure) {
                val errorMessage = result.exceptionOrNull()?.message ?: "Unbekannter Fehler"
                _uiState.value = SkillsUiState.Error(errorMessage)
            }
        }
    }
}

/**
 * Sealed Class für den UI-Status der Skills-Verwaltung
 */
sealed class SkillsUiState {
    object Loading : SkillsUiState()
    object Empty : SkillsUiState()
    data class Success(val skills: List<Skill>) : SkillsUiState()
    data class Error(val message: String) : SkillsUiState()
}