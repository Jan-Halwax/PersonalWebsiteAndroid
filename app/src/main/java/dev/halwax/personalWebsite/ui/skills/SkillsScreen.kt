package dev.halwax.personalWebsite.ui.skills

import android.app.DatePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import dev.halwax.personalWebsite.ui.theme.DarkBackground
import dev.halwax.personalWebsite.ui.theme.DarkSurface
import dev.halwax.personalWebsite.ui.theme.Primary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Hauptbildschirm für die Skills-Verwaltung
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillsScreen(
    viewModel: SkillsViewModel = viewModel(),
    onNavigateToProjects: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val skillName by viewModel.skillName.collectAsState()
    val skillDate by viewModel.skillDate.collectAsState()
    val skillIconUrl by viewModel.skillIconUrl.collectAsState()
    val skillIsLearning by viewModel.skillIsLearning.collectAsState()
    val currentEditSkill by viewModel.currentEditSkill.collectAsState()

    // Dialog-Zustände
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var skillToDelete by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Skills verwalten") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = Color.White
                ),
                actions = {
                    TextButton(onClick = onNavigateToProjects) {
                        Text("Projekte", color = Primary)
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
                    viewModel.resetSkillForm()
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Hinzufügen") },
                text = { Text("Skill hinzufügen") },
                containerColor = Primary
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    top = padding.calculateTopPadding(),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = padding.calculateBottomPadding()
                )
                .background(DarkBackground)
                .padding(horizontal = 16.dp)
        ) {
            when (uiState) {
                is SkillsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary)
                    }
                }

                is SkillsUiState.Empty -> {
                    EmptySkillsPlaceholder(
                        onAddClick = {
                            viewModel.resetSkillForm()
                            showAddEditDialog = true
                        }
                    )
                }

                is SkillsUiState.Success -> {
                    val skills = (uiState as SkillsUiState.Success).skills

                    LazyColumn(
                        // Füge ein zusätzliches Padding unten hinzu, um Platz für den FAB zu schaffen
                        contentPadding = PaddingValues(bottom = 88.dp, top = 8.dp)
                    ) {
                        items(skills) { skill ->
                            SkillItem(
                                skill = skill,
                                onEditClick = {
                                    viewModel.prepareEditSkill(skill)
                                    showAddEditDialog = true
                                },
                                onDeleteClick = {
                                    skillToDelete = skill.id
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                is SkillsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fehler: ${(uiState as SkillsUiState.Error).message}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Skill hinzufügen/bearbeiten Dialog
    if (showAddEditDialog) {
        val isEditMode = currentEditSkill != null
        val dialogTitle = if (isEditMode) "Skill bearbeiten" else "Skill hinzufügen"

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(dialogTitle) },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveSkill()
                        showAddEditDialog = false
                    },
                    enabled = skillName.isNotBlank() && skillIconUrl.isNotBlank()
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
                SkillFormContent(
                    skillName = skillName,
                    skillDate = skillDate,
                    skillIconUrl = skillIconUrl,
                    skillIsLearning = skillIsLearning,
                    onSkillNameChange = { viewModel.updateSkillName(it) },
                    onSkillDateChange = { viewModel.updateSkillDate(it) },
                    onSkillIconUrlChange = { viewModel.updateSkillIconUrl(it) },
                    onSkillIsLearningChange = { viewModel.updateSkillIsLearning(it) }
                )
            }
        )
    }

    // Löschen bestätigen Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Skill löschen") },
            containerColor = DarkSurface,
            titleContentColor = Color.White,
            textContentColor = Color.White,
            text = { Text("Möchtest du diesen Skill wirklich löschen?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSkill(skillToDelete)
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
 * Komponente für das Formular zum Hinzufügen/Bearbeiten eines Skills
 */
@Composable
fun SkillFormContent(
    skillName: String,
    skillDate: Date,
    skillIconUrl: String,
    skillIsLearning: Boolean,
    onSkillNameChange: (String) -> Unit,
    onSkillDateChange: (Date) -> Unit,
    onSkillIconUrlChange: (String) -> Unit,
    onSkillIsLearningChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

    Column {
        // Name
        OutlinedTextField(
            value = skillName,
            onValueChange = onSkillNameChange,
            label = { Text("Name des Skills") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Datum
        OutlinedTextField(
            value = dateFormat.format(skillDate),
            onValueChange = { /* Wird durch DatePicker gesteuert */ },
            label = { Text("Datum") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    val calendar = Calendar.getInstance().apply {
                        time = skillDate
                    }

                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(Calendar.YEAR, year)
                            calendar.set(Calendar.MONTH, month)
                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            onSkillDateChange(calendar.time)
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

        // Icon URL
        OutlinedTextField(
            value = skillIconUrl,
            onValueChange = onSkillIconUrlChange,
            label = { Text("Icon URL") },
            placeholder = { Text("z.B. https://img.icons8.com/nolan/128/javascript.png") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Wird noch gelernt
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = skillIsLearning,
                onCheckedChange = onSkillIsLearningChange
            )
            Text(
                text = "Wird noch gelernt",
                color = Color.White
            )
        }
    }
}