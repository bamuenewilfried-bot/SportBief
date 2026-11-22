package com.example.data.model

enum class SportCategory(val displayName: String, val emoji: String) {
    ALL("Tous les sports", "🏆"),
    FOOTBALL("Football", "⚽"),
    BASKETBALL("Basketball", "🏀"),
    TENNIS("Tennis", "🎾"),
    F1("Formule 1", "🏎️"),
    RUGBY("Rugby", "🏉"),
    CYCLING("Cyclisme", "🚴"),
    MMA("Combat & MMA", "🥊")
}

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val category: SportCategory,
    val author: String,
    val date: String,
    val readTimeMinutes: Int,
    val isBreaking: Boolean = false,
    val isMercato: Boolean = false,
    val isFeatured: Boolean = false,
    val likesCount: Int = 0,
    val viewsCount: Int = 1200,
    val tags: List<String> = emptyList(),
    val relatedMatchId: String? = null
)

enum class MatchStatus(val label: String, val isLive: Boolean) {
    LIVE("EN DIRECT", true),
    HALF_TIME("MI-TEMPS", true),
    FINISHED("TERMINÉ", false),
    SCHEDULED("À VENIR", false)
}

data class MatchEvent(
    val minute: String,
    val description: String,
    val type: MatchEventType,
    val isHomeTeam: Boolean
)

enum class MatchEventType {
    GOAL, YELLOW_CARD, RED_CARD, PENALTY, BASKET_THREE, OVERTAKE, TRY
}

data class MatchStats(
    val possessionHome: Int = 50,
    val possessionAway: Int = 50,
    val shotsHome: Int = 0,
    val shotsAway: Int = 0,
    val shotsOnTargetHome: Int = 0,
    val shotsOnTargetAway: Int = 0,
    val foulsHome: Int = 0,
    val foulsAway: Int = 0
)

data class Match(
    val id: String,
    val category: SportCategory,
    val competition: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: MatchStatus,
    val currentMinute: String,
    val timeOrDate: String,
    val venue: String,
    val homeScorers: List<String> = emptyList(),
    val awayScorers: List<String> = emptyList(),
    val events: List<MatchEvent> = emptyList(),
    val stats: MatchStats = MatchStats()
)

data class MercatoDeal(
    val id: String,
    val playerName: String,
    val position: String,
    val fromClub: String,
    val toClub: String,
    val transferFee: String,
    val status: String, // "OFFICIEL", "RUMEUR CHAUDE", "ACCORD TOTAL", "PROLONGATION"
    val dateFormatted: String,
    val details: String,
    val category: SportCategory = SportCategory.FOOTBALL
)

data class RankingItem(
    val rank: Int,
    val name: String,
    val teamOrCountry: String = "",
    val playedOrRaces: Int,
    val wonOrWins: Int,
    val drawnOrPodiums: Int = 0,
    val lostOrPoints: Int = 0,
    val points: Int,
    val goalDifference: String = "+0",
    val recentForm: List<String> = emptyList() // "V", "N", "D"
)

data class PollOption(
    val id: Int,
    val text: String,
    val votes: Int
)

data class SportPoll(
    val id: String,
    val question: String,
    val category: SportCategory,
    val options: List<PollOption>,
    val totalVotes: Int,
    val userVotedOptionId: Int? = null
)
