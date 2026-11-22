package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteTeamEntity
import com.example.data.local.SavedArticleEntity
import com.example.data.model.Article
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.data.model.MercatoDeal
import com.example.data.model.RankingItem
import com.example.data.model.SportCategory
import com.example.data.model.SportPoll
import com.example.data.repository.SportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    NEWS("À la Une"),
    SCORES("Direct"),
    MERCATO("Mercato"),
    RANKINGS("Classements"),
    FAVORITES("Favoris")
}

enum class MatchFilter(val label: String) {
    ALL("Tous"),
    LIVE("En Direct 🔴"),
    FINISHED("Terminés"),
    UPCOMING("À Venir")
}

data class SportsUiState(
    val selectedTab: AppNavTab = AppNavTab.NEWS,
    val selectedCategory: SportCategory = SportCategory.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val matchFilter: MatchFilter = MatchFilter.ALL,
    val rankingSport: SportCategory = SportCategory.FOOTBALL,
    val selectedArticle: Article? = null,
    val selectedMatch: Match? = null,
    val isRefreshing: Boolean = false,
    val userNotificationAlertsEnabled: Boolean = true
)

class SportsViewModel(
    private val repository: SportsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SportsUiState())
    val uiState: StateFlow<SportsUiState> = _uiState.asStateFlow()

    val articles: StateFlow<List<Article>> = repository.articles
    val matches: StateFlow<List<Match>> = repository.matches
    val mercatoDeals: StateFlow<List<MercatoDeal>> = repository.mercatoDeals
    val polls: StateFlow<List<SportPoll>> = repository.polls

    val savedArticles: StateFlow<List<SavedArticleEntity>> = repository.savedArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTeams: StateFlow<List<FavoriteTeamEntity>> = repository.favoriteTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered articles based on Category and Search Query
    val filteredArticles: StateFlow<List<Article>> = combine(
        articles,
        _uiState
    ) { artList, state ->
        artList.filter { article ->
            val matchesCategory = (state.selectedCategory == SportCategory.ALL || article.category == state.selectedCategory)
            val matchesSearch = if (state.searchQuery.isBlank()) true else {
                article.title.contains(state.searchQuery, ignoreCase = true) ||
                article.summary.contains(state.searchQuery, ignoreCase = true) ||
                article.tags.any { it.contains(state.searchQuery, ignoreCase = true) }
            }
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered matches based on Category and MatchFilter
    val filteredMatches: StateFlow<List<Match>> = combine(
        matches,
        _uiState
    ) { matchList, state ->
        matchList.filter { match ->
            val matchesCategory = (state.selectedCategory == SportCategory.ALL || match.category == state.selectedCategory)
            val matchesFilter = when (state.matchFilter) {
                MatchFilter.ALL -> true
                MatchFilter.LIVE -> match.status == MatchStatus.LIVE || match.status == MatchStatus.HALF_TIME
                MatchFilter.FINISHED -> match.status == MatchStatus.FINISHED
                MatchFilter.UPCOMING -> match.status == MatchStatus.SCHEDULED
            }
            val matchesSearch = if (state.searchQuery.isBlank()) true else {
                match.homeTeam.contains(state.searchQuery, ignoreCase = true) ||
                match.awayTeam.contains(state.searchQuery, ignoreCase = true) ||
                match.competition.contains(state.searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesFilter && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AppNavTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectCategory(category: SportCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleSearchActive(active: Boolean) {
        _uiState.value = _uiState.value.copy(
            isSearchActive = active,
            searchQuery = if (!active) "" else _uiState.value.searchQuery
        )
    }

    fun setMatchFilter(filter: MatchFilter) {
        _uiState.value = _uiState.value.copy(matchFilter = filter)
    }

    fun setRankingSport(category: SportCategory) {
        _uiState.value = _uiState.value.copy(rankingSport = category)
    }

    fun openArticle(article: Article?) {
        _uiState.value = _uiState.value.copy(selectedArticle = article)
    }

    fun openMatch(match: Match?) {
        _uiState.value = _uiState.value.copy(selectedMatch = match)
    }

    fun toggleSaveArticle(article: Article) {
        viewModelScope.launch {
            val isSaved = savedArticles.value.any { it.id == article.id }
            repository.toggleSaveArticle(article, isSaved)
        }
    }

    fun likeArticle(articleId: String) {
        repository.likeArticle(articleId)
        // Also update selected article state if currently open
        val current = _uiState.value.selectedArticle
        if (current?.id == articleId) {
            _uiState.value = _uiState.value.copy(
                selectedArticle = current.copy(likesCount = current.likesCount + 1)
            )
        }
    }

    fun votePoll(pollId: String, optionId: Int) {
        viewModelScope.launch {
            repository.voteOnPoll(pollId, optionId)
        }
    }

    fun toggleFavoriteTeam(name: String, sport: String, league: String) {
        viewModelScope.launch {
            val isFav = favoriteTeams.value.any { it.name == name }
            repository.toggleFavoriteTeam(name, sport, league, isFav)
        }
    }

    fun toggleNotificationAlerts() {
        _uiState.value = _uiState.value.copy(
            userNotificationAlertsEnabled = !_uiState.value.userNotificationAlertsEnabled
        )
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            kotlinx.coroutines.delay(800)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun getRankings(): List<RankingItem> {
        return repository.getRankingsFor(_uiState.value.rankingSport)
    }
}

class SportsViewModelFactory(
    private val repository: SportsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SportsViewModel::class.java)) {
            return SportsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
