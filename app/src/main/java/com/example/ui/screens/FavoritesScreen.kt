package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FavoriteTeamEntity
import com.example.data.local.SavedArticleEntity
import com.example.data.model.Article
import com.example.data.model.SportCategory
import com.example.ui.theme.SportsGold
import com.example.ui.theme.SportsOrange
import com.example.ui.theme.SportsPitchGreen

@Composable
fun FavoritesScreen(
    savedArticles: List<SavedArticleEntity>,
    favoriteTeams: List<FavoriteTeamEntity>,
    onOpenSavedArticle: (Article) -> Unit,
    onDeleteSavedArticle: (String) -> Unit,
    onToggleFavoriteTeam: (name: String, sport: String, league: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("favorites_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = SportsOrange
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Articles (${savedArticles.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedSubTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_saved_articles")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mes Clubs (${favoriteTeams.size})",
                            fontSize = 13.sp,
                            fontWeight = if (selectedSubTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                modifier = Modifier.testTag("tab_favorite_teams")
            )
        }

        if (selectedSubTab == 0) {
            // Saved Articles List
            if (savedArticles.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📑", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Aucun article enregistré",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cliquez sur l'icône de marque-page d'un article pour le lire plus tard hors-connexion.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedArticles, key = { it.id }) { item ->
                        SavedArticleCard(
                            entity = item,
                            onClick = {
                                val category = try {
                                    SportCategory.valueOf(item.category)
                                } catch (e: Exception) {
                                    SportCategory.FOOTBALL
                                }
                                onOpenSavedArticle(
                                    Article(
                                        id = item.id,
                                        title = item.title,
                                        summary = item.summary,
                                        content = item.content,
                                        category = category,
                                        author = item.author,
                                        date = item.date,
                                        readTimeMinutes = item.readTimeMinutes
                                    )
                                )
                            },
                            onDelete = { onDeleteSavedArticle(item.id) }
                        )
                    }
                }
            }
        } else {
            // Favorite Teams & Athletes selector
            val popularTeams = listOf(
                Triple("Paris Saint-Germain", "Football", "Ligue 1"),
                Triple("Olympique de Marseille", "Football", "Ligue 1"),
                Triple("Real Madrid", "Football", "La Liga"),
                Triple("FC Barcelone", "Football", "La Liga"),
                Triple("San Antonio Spurs (Wemby)", "Basketball", "NBA"),
                Triple("LA Lakers", "Basketball", "NBA"),
                Triple("Scuderia Ferrari", "F1", "Formule 1"),
                Triple("Stade Toulousain", "Rugby", "Top 14"),
                Triple("Carlos Alcaraz", "Tennis", "ATP"),
                Triple("Antoine Dupont", "Rugby", "France / Top 14")
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Sélectionnez vos équipes et athlètes favoris pour recevoir les alertes et les scores prioritaires :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                items(popularTeams) { (name, sport, league) ->
                    val isFav = favoriteTeams.any { it.name == name }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$sport • $league",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isFav) {
                                Button(
                                    onClick = { onToggleFavoriteTeam(name, sport, league) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SportsPitchGreen),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Suivi", fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { onToggleFavoriteTeam(name, sport, league) },
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text("+ Suivre", fontSize = 12.sp, color = SportsOrange)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedArticleCard(
    entity: SavedArticleEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entity.category} • ${entity.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SportsOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entity.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "✍️ ${entity.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.BookmarkRemove,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
