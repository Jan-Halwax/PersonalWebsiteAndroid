package dev.halwax.personalWebsite.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.halwax.personalWebsite.repository.AuthRepository
import dev.halwax.personalWebsite.ui.login.LoginScreen
import dev.halwax.personalWebsite.ui.projects.ProjectsScreen
import dev.halwax.personalWebsite.ui.skills.SkillsScreen

/**
 * Enum für die verschiedenen Routen der App
 */
enum class AppRoute {
    Login,
    Skills,
    Projects,
    Home
}

/**
 * Hauptnavigation der App
 * Handhabt Routing und Navigation zwischen den Bildschirmen
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authRepository: AuthRepository = AuthRepository(),
    openWebsite: () -> Unit
) {
    // Auth-Status beobachten
    val isSignedIn by authRepository.isSignedIn.collectAsState()

    // Startbildschirm basierend auf Auth-Status
    val startDestination = if (isSignedIn) {
        AppRoute.Skills.name
    } else {
        AppRoute.Login.name
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login-Bildschirm
        composable(AppRoute.Login.name) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AppRoute.Skills.name) {
                        popUpTo(AppRoute.Login.name) { inclusive = true }
                    }
                }
            )
        }

        // Skills-Bildschirm
        composable(AppRoute.Skills.name) {
            SkillsScreen(
                onNavigateToProjects = {
                    navController.navigate(AppRoute.Projects.name)
                },
                onNavigateToHome = {
                    openWebsite()
                }
            )
        }

        // Projekte-Bildschirm
        composable(AppRoute.Projects.name) {
            ProjectsScreen(
                onNavigateToSkills = {
                    navController.navigate(AppRoute.Skills.name) {
                        popUpTo(AppRoute.Projects.name) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    openWebsite()
                }
            )
        }
    }
}