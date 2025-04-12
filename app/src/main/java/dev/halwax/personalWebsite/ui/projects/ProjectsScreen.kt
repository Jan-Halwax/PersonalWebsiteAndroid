package dev.halwax.personalWebsite.ui.projects

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.halwax.personalWebsite.model.Feature
import dev.halwax.personalWebsite.model.Project
import dev.halwax.personalWebsite.ui.theme.DarkBackground
import dev.halwax.personalWebsite.ui.theme.DarkCard
import dev.halwax.personalWebsite.ui.theme.DarkSurface
import dev.halwax.personalWebsite.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Hauptbildschirm für die Projekte-Verwaltung
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    viewModel: ProjectsViewModel = viewModel(),
    onNavigateToSkills: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentProjects by viewModel.currentProjects.collectAsState()
    val portfolioProjects by viewModel.portfolioProjects.collectAsState()

    // Formular-Felder
    val projectName by viewModel.projectName.collectAsState()
    val projectStartDate by viewModel.projectStartDate.collectAsState()
    val projectEndDate by viewModel.projectEndDate.collectAsState()
    val projectImageUrl by viewModel.projectImageUrl.collectAsState()
    val projectGithubUrl by viewModel.projectGithubUrl.collectAsState()
    val projectAppUrl by viewModel.projectAppUrl.collectAsState()
    val projectDescription by viewModel.projectDescription.collectAsState()
    val projectIsCurrentProject by viewModel.projectIsCurrentProject.collectAsState()
    val projectFeatures by viewModel.projectFeatures.collectAsState()
    val featureTitle by viewModel.featureTitle.collectAsState()
    val featureDescription by viewModel.featureDescription.collectAsState()
    val currentEditProject by viewModel.currentEditProject.collectAsState()

    // UI-Zustände
    var tabIndex by remember { mutableIntStateOf(0) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf("") }
    var projectNameToDelete by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projekte verwalten") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White
                ),
                actions = {
                    TextButton(onClick = onNavigateToSkills) {
                        Text("Skills", color = Primary)
                    }
                    TextButton(onClick = onNavigateToHome) {
                        Text("Zur Website", color = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.resetProjectForm()
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Hinzufügen") },
                text = { Text("Projekt hinzufügen") },
                containerColor = Primary
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = padding.calculateBottomPadding()
                )
                .background(DarkBackground)
        ) {
            // Tab-Leiste
            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = DarkSurface,
                contentColor = Primary
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text("Aktuelle Projekte") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text("Portfolio") }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (uiState) {
                    is ProjectsUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }

                    is ProjectsUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Fehler: ${(uiState as ProjectsUiState.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        // Je nach Tab aktuelle Projekte oder Portfolio anzeigen
                        if (tabIndex == 0) {
                            // Aktuelle Projekte
                            if (currentProjects.isEmpty()) {
                                EmptyProjectsPlaceholder(
                                    isCurrentProjects = true,
                                    onAddClick = {
                                        viewModel.resetProjectForm()
                                        viewModel.updateProjectIsCurrentProject(true)
                                        showAddEditDialog = true
                                    }
                                )
                            } else {
                                LazyColumn(
                                    // Füge ein zusätzliches Padding unten hinzu, um Platz für den FAB zu schaffen
                                    contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
                                ) {
                                    items(currentProjects) { project ->
                                        ProjectItem(
                                            project = project,
                                            onEditClick = {
                                                viewModel.prepareEditProject(project)
                                                showAddEditDialog = true
                                            },
                                            onMoveClick = {
                                                viewModel.moveProject(project.id, false)
                                            },
                                            onDeleteClick = {
                                                projectToDelete = project.id
                                                projectNameToDelete = project.name
                                                showDeleteDialog = true
                                            },
                                            showFeatures = true
                                        )
                                    }
                                }
                            }
                        } else {
                            // Portfolio Projekte
                            if (portfolioProjects.isEmpty()) {
                                EmptyProjectsPlaceholder(
                                    isCurrentProjects = false,
                                    onAddClick = {
                                        viewModel.resetProjectForm()
                                        viewModel.updateProjectIsCurrentProject(false)
                                        showAddEditDialog = true
                                    }
                                )
                            } else {
                                LazyColumn(
                                    // Füge ein zusätzliches Padding unten hinzu, um Platz für den FAB zu schaffen
                                    contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
                                ) {
                                    items(portfolioProjects) { project ->
                                        ProjectItem(
                                            project = project,
                                            onEditClick = {
                                                viewModel.prepareEditProject(project)
                                                showAddEditDialog = true
                                            },
                                            onMoveClick = {
                                                viewModel.moveProject(project.id, true)
                                            },
                                            onDeleteClick = {
                                                projectToDelete = project.id
                                                projectNameToDelete = project.name
                                                showDeleteDialog = true
                                            },
                                            showFeatures = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Projekt hinzufügen/bearbeiten Dialog
    if (showAddEditDialog) {
        val isEditMode = currentEditProject != null
        val dialogTitle = if (isEditMode) "Projekt bearbeiten" else "Projekt hinzufügen"

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(dialogTitle) },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProject()
                        showAddEditDialog = false
                    },
                    enabled = projectName.isNotBlank() &&
                            projectImageUrl.isNotBlank() &&
                            projectGithubUrl.isNotBlank() &&
                            projectDescription.isNotBlank() &&
                            (projectIsCurrentProject || projectEndDate != null)
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDialog = false }) {
                    Text("Abbrechen")
                }
            },
            text = {
                ProjectFormContent(
                    projectName = projectName,
                    projectStartDate = projectStartDate,
                    projectEndDate = projectEndDate,
                    projectImageUrl = projectImageUrl,
                    projectGithubUrl = projectGithubUrl,
                    projectAppUrl = projectAppUrl,
                    projectDescription = projectDescription,
                    projectIsCurrentProject = projectIsCurrentProject,
                    projectFeatures = projectFeatures,
                    featureTitle = featureTitle,
                    featureDescription = featureDescription,
                    onProjectNameChange = viewModel::updateProjectName,
                    onProjectStartDateChange = viewModel::updateProjectStartDate,
                    onProjectEndDateChange = viewModel::updateProjectEndDate,
                    onProjectImageUrlChange = viewModel::updateProjectImageUrl,
                    onProjectGithubUrlChange = viewModel::updateProjectGithubUrl,
                    onProjectAppUrlChange = viewModel::updateProjectAppUrl,
                    onProjectDescriptionChange = viewModel::updateProjectDescription,
                    onProjectIsCurrentProjectChange = viewModel::updateProjectIsCurrentProject,
                    onFeatureTitleChange = viewModel::updateFeatureTitle,
                    onFeatureDescriptionChange = viewModel::updateFeatureDescription,
                    onAddFeature = viewModel::addFeature,
                    onRemoveFeature = viewModel::removeFeature
                )
            }
        )
    }

    // Löschen bestätigen Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Projekt löschen") },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            text = {
                Text("Möchtest du das Projekt \"$projectNameToDelete\" wirklich löschen?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProject(projectToDelete)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

/**
 * Komponente für das Formular zum Hinzufügen/Bearbeiten eines Projekts
 */
@Composable
fun ProjectFormContent(
    projectName: String,
    projectStartDate: Date,
    projectEndDate: Date?,
    projectImageUrl: String,
    projectGithubUrl: String,
    projectAppUrl: String,
    projectDescription: String,
    projectIsCurrentProject: Boolean,
    projectFeatures: List<Feature>,
    featureTitle: String,
    featureDescription: String,
    onProjectNameChange: (String) -> Unit,
    onProjectStartDateChange: (Date) -> Unit,
    onProjectEndDateChange: (Date?) -> Unit,
    onProjectImageUrlChange: (String) -> Unit,
    onProjectGithubUrlChange: (String) -> Unit,
    onProjectAppUrlChange: (String) -> Unit,
    onProjectDescriptionChange: (String) -> Unit,
    onProjectIsCurrentProjectChange: (Boolean) -> Unit,
    onFeatureTitleChange: (String) -> Unit,
    onFeatureDescriptionChange: (String) -> Unit,
    onAddFeature: () -> Unit,
    onRemoveFeature: (Int) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Name
        OutlinedTextField(
            value = projectName,
            onValueChange = onProjectNameChange,
            label = { Text("Name des Projekts") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Startdatum
        OutlinedTextField(
            value = dateFormat.format(projectStartDate),
            onValueChange = { /* Wird durch DatePicker gesteuert */ },
            label = { Text("Startdatum") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        time = projectStartDate
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            onProjectStartDateChange(calendar.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Datum auswählen"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Projekttyp (aktuell oder Portfolio)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = projectIsCurrentProject,
                onCheckedChange = onProjectIsCurrentProjectChange
            )
            Text(
                text = "Aktuelles Projekt (ansonsten Portfolio)",
                color = Color.White
            )
        }

        // Enddatum (nur für Portfolio-Projekte)
        if (!projectIsCurrentProject) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = projectEndDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = { /* Wird durch DatePicker gesteuert */ },
                label = { Text("Abschlussdatum (erforderlich für Portfolio)") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        val calendar = Calendar.getInstance().apply {
                            time = projectEndDate ?: Date()
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                calendar.set(Calendar.YEAR, year)
                                calendar.set(Calendar.MONTH, month)
                                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                onProjectEndDateChange(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Datum auswählen"
                        )
                    }
                }
            )
        } else {
            // Reset Enddatum wenn aktuelles Projekt
            onProjectEndDateChange(null)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bild URL
        OutlinedTextField(
            value = projectImageUrl,
            onValueChange = onProjectImageUrlChange,
            label = { Text("Bild URL") },
            placeholder = { Text("z.B. https://img.icons8.com/nolan/128/javascript.png") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub URL
        OutlinedTextField(
            value = projectGithubUrl,
            onValueChange = onProjectGithubUrlChange,
            label = { Text("GitHub URL") },
            placeholder = { Text("z.B. https://github.com/username/project") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App URL (optional)
        OutlinedTextField(
            value = projectAppUrl,
            onValueChange = onProjectAppUrlChange,
            label = { Text("App URL (optional)") },
            placeholder = { Text("z.B. https://project.web.app") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Beschreibung
        OutlinedTextField(
            value = projectDescription,
            onValueChange = onProjectDescriptionChange,
            label = { Text("Beschreibung") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Features-Bereich
        Text(
            text = "Features",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Features-Liste
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = DarkCard
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                if (projectFeatures.isEmpty()) {
                    Text(
                        text = "Noch keine Features hinzugefügt",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                } else {
                    Column {
                        projectFeatures.forEachIndexed { index, feature ->
                            FeatureItem(
                                feature = feature,
                                onRemoveClick = { onRemoveFeature(index) }
                            )

                            if (index < projectFeatures.size - 1) {
                                Divider(
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Neues Feature hinzufügen
        OutlinedTextField(
            value = featureTitle,
            onValueChange = onFeatureTitleChange,
            label = { Text("Feature-Titel") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = featureDescription,
            onValueChange = onFeatureDescriptionChange,
            label = { Text("Feature-Beschreibung") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onAddFeature,
            enabled = featureTitle.isNotBlank() && featureDescription.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Feature hinzufügen")
        }
    }
}