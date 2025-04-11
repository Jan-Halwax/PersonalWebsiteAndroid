package dev.halwax.personalWebsite.model

import java.util.Date

/**
 * Datenklasse für Skills
 * Entspricht den Skills-Objekten in Firestore
 */
data class Skill(
    val id: String = "",
    val name: String = "",
    val date: Date = Date(),
    val iconUrl: String = "",
    val isLearning: Boolean = false
)

/**
 * Datenklasse für Features
 * Wird als Unterobjekt in Project verwendet
 */
data class Feature(
    val title: String = "",
    val description: String = ""
)

/**
 * Datenklasse für Projekte
 * Entspricht den Projekt-Objekten in Firestore
 */
data class Project(
    val id: String = "",
    val name: String = "",
    val startDate: Date = Date(),
    val endDate: Date? = null,
    val imageUrl: String = "",
    val githubUrl: String = "",
    val appUrl: String? = null,
    val description: String = "",
    val isCurrentProject: Boolean = true,
    val features: List<Feature> = emptyList()
)