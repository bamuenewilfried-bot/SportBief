package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SportsDatabase
import com.example.data.model.MatchStatus
import com.example.data.repository.SportsRepository
import com.example.ui.components.SportsBottomNavBar
import com.example.ui.components.SportsTopBar
import com.example.ui.screens.ArticleDetailSheet
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.MatchDetailSheet
import com.example.ui.screens.MercatoScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.RankingsScreen
import com.example.ui.screens.ScoresScreen
import com.example.ui.theme.SportActuTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.SportsViewModel
import com.example.ui.viewmodel.SportsViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = SportsDatabase.getDatabase(applicationContext)
        val repository = SportsRepository(database.sportsDao())
        val factory = SportsViewModelFactory(repository)

        setContent {
            SportActuTheme {
                val viewModel: SportsViewModel = viewModel(factory = factory)
                SportsApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SportsApp(viewModel: SportsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val articles by viewModel.filteredArticles.collectAsState()
    val matches by viewModel.filteredMatches.collectAsState()
    val allMatches by viewModel.matches.collectAsState()
    val mercatoDeals by viewModel.mercatoDeals.collectAsState()
    val polls by viewModel.polls.collectAsState()
    val savedArticles by viewModel.savedArticles.collectAsState()
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()

    val liveMatchesCount = allMatches.count { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALF_TIME }
    val liveMatches = allMatches.filter { it.status == MatchStatus.LIVE || it.status == MatchStatus.HALF_TIME }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SportsTopBar(
                searchQuery = uiState.searchQuery,
                isSearchActive = uiState.isSearchActive,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onToggleSearch = { viewModel.toggleSearchActive(it) },
                notificationsEnabled = uiState.userNotificationAlertsEnabled,
                onToggleNotifications = { viewModel.toggleNotificationAlerts() },
                liveMatchCount = liveMatchesCount
            )
        },
        bottomBar = {
            SportsBottomNavBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { viewModel.selectTab(it) },
                liveMatchCount = liveMatchesCount
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                AppNavTab.NEWS -> {
                    NewsScreen(
                        articles = articles,
                        savedArticles = savedArticles,
                        liveMatches = liveMatches,
                        polls = polls,
                        selectedCategory = uiState.selectedCategory,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onArticleClick = { viewModel.openArticle(it) },
                        onToggleSaveArticle = { viewModel.toggleSaveArticle(it) },
                        onLikeArticle = { viewModel.likeArticle(it) },
                        onMatchClick = { viewModel.openMatch(it) },
                        onVotePoll = { pollId, optId -> viewModel.votePoll(pollId, optId) },
                        onRefresh = { viewModel.refreshData() }
                    )
                }
                AppNavTab.SCORES -> {
                    ScoresScreen(
                        matches = matches,
                        selectedCategory = uiState.selectedCategory,
                        selectedFilter = uiState.matchFilter,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSelectFilter = { viewModel.setMatchFilter(it) },
                        onMatchClick = { viewModel.openMatch(it) }
                    )
                }
                AppNavTab.MERCATO -> {
                    MercatoScreen(
                        deals = mercatoDeals
                    )
                }
                AppNavTab.RANKINGS -> {
                    RankingsScreen(
                        rankings = viewModel.getRankings(),
                        selectedSport = uiState.rankingSport,
                        onSelectSport = { viewModel.setRankingSport(it) }
                    )
                }
                AppNavTab.FAVORITES -> {
                    FavoritesScreen(
                        savedArticles = savedArticles,
                        favoriteTeams = favoriteTeams,
                        onOpenSavedArticle = { viewModel.openArticle(it) },
                        onDeleteSavedArticle = { id ->
                            val art = articles.firstOrNull { it.id == id }
                            if (art != null) viewModel.toggleSaveArticle(art)
                        },
                        onToggleFavoriteTeam = { name, sport, league ->
                            viewModel.toggleFavoriteTeam(name, sport, league)
                        }
                    )
                }
            }

            // Article Detail BottomSheet
            uiState.selectedArticle?.let { article ->
                val isSaved = savedArticles.any { it.id == article.id }
                ArticleDetailSheet(
                    article = article,
                    isSaved = isSaved,
                    onDismiss = { viewModel.openArticle(null) },
                    onToggleSave = { viewModel.toggleSaveArticle(it) },
                    onLike = { viewModel.likeArticle(it) }
                )
            }

            // Match Detail BottomSheet
            uiState.selectedMatch?.let { match ->
                MatchDetailSheet(
                    match = match,
                    onDismiss = { viewModel.openMatch(null) }
                )
            }
        }
    }
}
