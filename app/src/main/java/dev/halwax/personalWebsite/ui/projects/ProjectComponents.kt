package dev.halwax.personalWebsite.ui.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.halwax.personalWebsite.model.Feature
import dev.halwax.personalWebsite.model.Project
import dev.halwax.personalWebsite.ui.theme.DarkCard
import dev.halwax.personalWebsite.ui.theme.Primary
import dev.halwax.personalWebsite.ui.theme.Secondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Komponente zur Darstellung eines einzelnen Projekts
 */
@Composable
fun ProjectItem(
    project: Project,
    onEditClick: () -> Unit,
    onMoveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    showFeatures: Boolean = false
) {
    val dateFormat = SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN)
    val formattedStartDate = dateFormat.format(project.startDate)
    val formattedEndDate = project.endDate?.let { dateFormat.format(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkCard
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 4.dp,
                    color = if (project.isCurrentProject) Primary else Secondary,
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        topEnd = 8.dp,
                        bottomStart = 8.dp,
                        bottomEnd = 8.dp
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Projekt-Bild
                AsyncImage(
                    model = project.imageUrl,
                    contentDescription = project.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Projekt-Informationen
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "Gestartet: $formattedStartDate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )

                    if (!project.isCurrentProject && formattedEndDate != null) {
                        Text(
                            text = "Abgeschlossen: $formattedEndDate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = project.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Links und Features
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Links
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "GitHub: ${project.githubUrl}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    project.appUrl?.let {
                        Text(
                            text = "App: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (project.isCurrentProject)
                                    Primary.copy(alpha = 0.2f)
                                else
                                    Secondary.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (project.isCurrentProject) "Aktuelles Projekt" else "Portfolio",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (project.isCurrentProject) Primary else Secondary
                        )
                    }
                }

                // Features-Info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${project.features.size} Features",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }

            // Features anzeigen, wenn gewünscht
            if (showFeatures && project.features.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Features:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    items(project.features) { feature ->
                        FeatureItem(feature = feature)
                    }
                }
            }

            // Aktions-Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Bearbeiten",
                        tint = Color.White
                    )
                }

                IconButton(onClick = onMoveClick) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = if (project.isCurrentProject)
                            "Zum Portfolio verschieben"
                        else
                            "Zu aktuellen Projekten verschieben",
                        tint = Color.Yellow
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

/**
 * Komponente zur Darstellung eines einzelnen Features
 */
@Composable
fun FeatureItem(
    feature: Feature,
    onRemoveClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray
            )
        }

        // Löschen-Button, falls vorhanden
        onRemoveClick?.let {
            IconButton(
                onClick = it,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Feature entfernen",
                    tint = Color.Red,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Platzhalter-Komponente für einen leeren Projekte-Zustand
 */
@Composable
fun EmptyProjectsPlaceholder(
    isCurrentProjects: Boolean,
    onAddClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCurrentProjects) "Keine aktuellen Projekte vorhanden" else "Keine Portfolio-Projekte vorhanden",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Klicke hier, um ein Projekt hinzuzufügen",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAddClick() }
                    .background(Primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White
            )
        }
    }
}