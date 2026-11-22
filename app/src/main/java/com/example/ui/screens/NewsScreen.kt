package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.SavedArticleEntity
import com.example.data.model.Article
import com.example.data.model.Match
import com.example.data.model.SportCategory
import com.example.data.model.SportPoll
import com.example.ui.components.ArticleCard
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.MatchScoreCard
import com.example.ui.components.PollCard
import com.example.ui.theme.SportsLiveRed
import com.example.ui.theme.SportsOrange

@Composable
fun NewsScreen(
    articles: List<Article>,
    savedArticles: List<SavedArticleEntity>,
    liveMatches: List<Match>,
    polls: List<SportPoll>,
    selectedCategory: SportCategory,
    onSelectCategory: (SportCategory) -> Unit,
    onArticleClick: (Article) -> Unit,
    onToggleSaveArticle: (Article) -> Unit,
    onLikeArticle: (String) -> Unit,
    onMatchClick: (Match) -> Unit,
    onVotePoll: (String, Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredArticle = articles.firstOrNull { it.isFeatured } ?: articles.firstOrNull()
    val otherArticles = if (featuredArticle != null) articles.filter { it.id != featuredArticle.id } else articles
    val firstPoll = polls.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("news_feed_list"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Category filters
        item {
            CategoryFilterRow(
                selectedCategory = selectedCategory,
                onSelectCategory = onSelectCategory
            )
        }

        // Live Ticker Strip if any matches are live
        if (liveMatches.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "En direct",
                                tint = SportsLiveRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MATCHS EN DIRECT",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = SportsLiveRed
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.size(28.dp).testTag("refresh_live_ticker")
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Actualiser",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    MatchScoreCard(
                        match = liveMatches.first(),
                        onClick = onMatchClick
                    )
                }
            }
        }

        // Hero Featured Article Banner
        if (featuredArticle != null) {
            item {
                HeroFeaturedArticle(
                    article = featuredArticle,
                    onClick = { onArticleClick(featuredArticle) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Breaking Ticker Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SportsOrange.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Flash",
                    tint = SportsOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FLASH : Mercato estival en ébullition, 12 transferts majeurs confirmés aujourd'hui !",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Section Title
        item {
            Text(
                text = "Toutes les actualités",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
        }

        // List of Articles
        items(otherArticles, key = { it.id }) { article ->
            val isSaved = savedArticles.any { it.id == article.id }
            ArticleCard(
                article = article,
                isSaved = isSaved,
                onArticleClick = onArticleClick,
                onToggleSave = onToggleSaveArticle,
                onLike = onLikeArticle,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Daily Sports Poll
        if (firstPoll != null) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                PollCard(
                    poll = firstPoll,
                    onVote = onVotePoll,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun HeroFeaturedArticle(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onClick() }
            .testTag("hero_featured_article"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Hero Image
            Image(
                painter = painterResource(id = R.drawable.img_sports_hero),
                contentDescription = "Image de fond sportive",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Overlay Text Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SportsOrange)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⭐ À LA UNE • ${article.category.displayName.uppercase()}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Text(
                        text = article.date,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Bottom Titles
                Column {
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            lineHeight = 24.sp
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lire l'article complet ➔",
                            color = SportsOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
