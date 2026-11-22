package com.example.data.repository

import com.example.data.local.FavoriteTeamEntity
import com.example.data.local.PollVoteEntity
import com.example.data.local.SavedArticleEntity
import com.example.data.local.SportsDao
import com.example.data.model.Article
import com.example.data.model.Match
import com.example.data.model.MatchEvent
import com.example.data.model.MatchEventType
import com.example.data.model.MatchStats
import com.example.data.model.MatchStatus
import com.example.data.model.MercatoDeal
import com.example.data.model.PollOption
import com.example.data.model.RankingItem
import com.example.data.model.SportCategory
import com.example.data.model.SportPoll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SportsRepository(
    private val sportsDao: SportsDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    private val _articles = MutableStateFlow<List<Article>>(initialArticles)
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(initialMatches)
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _mercatoDeals = MutableStateFlow<List<MercatoDeal>>(initialMercato)
    val mercatoDeals: StateFlow<List<MercatoDeal>> = _mercatoDeals.asStateFlow()

    private val _polls = MutableStateFlow<List<SportPoll>>(initialPolls)
    val polls: StateFlow<List<SportPoll>> = _polls.asStateFlow()

    val savedArticles: Flow<List<SavedArticleEntity>> = sportsDao.getAllSavedArticles()
    val favoriteTeams: Flow<List<FavoriteTeamEntity>> = sportsDao.getFavoriteTeams()
    val pollVotes: Flow<List<PollVoteEntity>> = sportsDao.getAllPollVotes()

    init {
        // Start live ticker timer to animate live sports matches (minute increments)
        scope.launch {
            while (true) {
                delay(12000)
                updateLiveMatchesTicker()
            }
        }
    }

    private fun updateLiveMatchesTicker() {
        val updated = _matches.value.map { match ->
            if (match.status == MatchStatus.LIVE) {
                val currentMinNum = match.currentMinute.replace("'", "").toIntOrNull()
                if (currentMinNum != null && currentMinNum < 90) {
                    val nextMin = currentMinNum + 1
                    match.copy(currentMinute = "$nextMin'")
                } else match
            } else match
        }
        _matches.value = updated
    }

    fun isArticleSaved(articleId: String): Flow<Boolean> {
        return sportsDao.isArticleSaved(articleId)
    }

    suspend fun toggleSaveArticle(article: Article, isCurrentlySaved: Boolean) {
        if (isCurrentlySaved) {
            sportsDao.deleteSavedArticle(article.id)
        } else {
            sportsDao.saveArticle(
                SavedArticleEntity(
                    id = article.id,
                    title = article.title,
                    summary = article.summary,
                    content = article.content,
                    category = article.category.name,
                    author = article.author,
                    date = article.date,
                    readTimeMinutes = article.readTimeMinutes
                )
            )
        }
    }

    suspend fun toggleFavoriteTeam(teamName: String, sport: String, league: String, isFav: Boolean) {
        val id = "${sport}_${teamName.lowercase().replace(" ", "_")}"
        if (isFav) {
            sportsDao.removeFavoriteTeam(id)
        } else {
            sportsDao.insertFavoriteTeam(
                FavoriteTeamEntity(
                    id = id,
                    name = teamName,
                    sport = sport,
                    league = league
                )
            )
        }
    }

    suspend fun voteOnPoll(pollId: String, optionId: Int) {
        sportsDao.recordPollVote(PollVoteEntity(pollId, optionId))
        _polls.value = _polls.value.map { poll ->
            if (poll.id == pollId && poll.userVotedOptionId == null) {
                val updatedOptions = poll.options.map { opt ->
                    if (opt.id == optionId) opt.copy(votes = opt.votes + 1) else opt
                }
                poll.copy(
                    options = updatedOptions,
                    totalVotes = poll.totalVotes + 1,
                    userVotedOptionId = optionId
                )
            } else poll
        }
    }

    fun likeArticle(articleId: String) {
        _articles.value = _articles.value.map { art ->
            if (art.id == articleId) art.copy(likesCount = art.likesCount + 1) else art
        }
    }

    fun getRankingsFor(category: SportCategory): List<RankingItem> {
        return when (category) {
            SportCategory.FOOTBALL, SportCategory.ALL -> ligue1Rankings
            SportCategory.BASKETBALL -> nbaRankings
            SportCategory.F1 -> f1Rankings
            SportCategory.RUGBY -> top14Rankings
            else -> ligue1Rankings
        }
    }

    companion object {
        val initialArticles = listOf(
            Article(
                id = "art-1",
                title = "Ligue des Champions : Soirée magique au Parc, qualification héroïque en demi-finales !",
                summary = "Grâce à une prestation collective flamboyante et un doublé décisif dans les derniers instants, Paris valide son billet pour le dernier carré européen.",
                content = """
                    Dans une ambiance incandescente au Parc des Princes, le Paris Saint-Germain a signé l'une de ses plus belles épopées européennes. Menés dès le quart d'heure de jeu sur une contre-attaque fulgurante, les Rouge et Bleu ont su faire preuve d'un mental d'acier.
                    
                    Dès le retour des vestiaires, le pressing haut orchestré par le milieu de terrain a asphyxié la défense adverse. L'égalisation est survenue à la 62e minute sur un enchaînement de passes rapides conclu par une frappe en pleine lucarne.
                    
                    À la 88e minute, sur un corner magistralement botté, la tête rageuse a propulsé le stade dans une liesse indescriptible. Avec cette victoire 2-1 (score cumulé 4-3), le club francilien rejoint les demi-finales de la prestigieuse C1, où il affrontera le vainqueur du duel Manchester City - Real Madrid.
                    
                    "Nous avons joué avec notre cœur et le soutien incroyable de nos supporters. C'est pour des moments comme ceux-ci que nous vivons le football", a confié l'entraîneur en conférence de presse d'après-match.
                """.trimIndent(),
                category = SportCategory.FOOTBALL,
                author = "Marc Lambert, envoyé spécial",
                date = "Il y a 15 min",
                readTimeMinutes = 4,
                isBreaking = true,
                isFeatured = true,
                likesCount = 284,
                viewsCount = 14200,
                tags = listOf("Ligue des Champions", "PSG", "Europe", "Football")
            ),
            Article(
                id = "art-2",
                title = "NBA : Victor Wembanyama réalise un triple-double historique face aux Celtics !",
                summary = "Le phénomène français des Spurs a encore repoussé les limites du possible en signant 32 points, 14 rebonds et 10 contres.",
                content = """
                    Boston n'a rien pu faire face à l'ouragan tricolore. Dans un TD Garden pourtant réputé imprenable, Victor Wembanyama a livré une copie tout simplement stratosphérique.
                    
                    Avec 32 points au compteur, 14 rebonds défensifs et 10 contres majuscules, le géant français devient le plus jeune joueur de l'histoire de la NBA à réaliser un triple-double avec contres. Les extérieurs de Boston ont été écœurés par son envergure infranchissable dans la raquette.
                    
                    En attaque, "Wemby" a également fait parler son adresse extérieure avec 4 tirs primés assassins en tête de raquette. Une performance ovationnée même par le public adverse, confirmant son statut d'icône mondiale du basketball.
                """.trimIndent(),
                category = SportCategory.BASKETBALL,
                author = "Thibault Renaud",
                date = "Il y a 45 min",
                readTimeMinutes = 3,
                isBreaking = false,
                isFeatured = true,
                likesCount = 491,
                viewsCount = 19500,
                tags = listOf("NBA", "Wembanyama", "Spurs", "Basketball")
            ),
            Article(
                id = "art-3",
                title = "Mercato : Accord total pour le transfert de l'attaquant star à 95M€ !",
                summary = "Après plusieurs semaines d'âpres négociations, le contrat est paraphé : la pépite internationale débarque dans son nouveau club jusqu'en 2030.",
                content = """
                    Le feuilleton de la semaine a trouvé son dénouement. Les directions des deux clubs ont enfin trouvé un accord définitif portant sur une indemnité de transfert de 95 millions d'euros assortie de bonus pouvant atteindre 15 millions supplémentaires.
                    
                    Le joueur a passé avec succès sa visite médicale ce matin dans les installations médicales du club. Il signera un contrat de 5 saisons avec un salaire annuel estimé à 12 millions d'euros nets.
                    
                    Présentation officielle prévue ce vendredi devant les médias et supporters au stade.
                """.trimIndent(),
                category = SportCategory.FOOTBALL,
                author = "Julien Soria",
                date = "Il y a 1h",
                readTimeMinutes = 2,
                isBreaking = false,
                isMercato = true,
                likesCount = 189,
                viewsCount = 8900,
                tags = listOf("Mercato", "Transfert", "Officiel")
            ),
            Article(
                id = "art-4",
                title = "Roland-Garros : Carlos Alcaraz et Jannik Sinner qualifiés pour une finale de rêve",
                summary = "Les deux nouveaux maîtres du circuit mondial s'affronteront dimanche sur le court Philippe-Chatrier pour soulever la Coupe des Mousquetaires.",
                content = """
                    Le public de la Porte d'Auteuil ne pouvait rêver d'une plus belle affiche. Tombeurs respectifs de leurs adversaires en demi-finales au terme de batailles homériques en cinq sets, Carlos Alcaraz et Jannik Sinner se retrouveront pour un duel d'anthologie.
                    
                    L'Espagnol a démontré une condition physique époustouflante, tandis que l'Italien a dicté le rythme grâce à sa cadence de frappe hallucinante des deux côtés.
                    
                    Rendez-vous dimanche à 15h pour le sacre d'un nouveau roi de la terre battue parisienne.
                """.trimIndent(),
                category = SportCategory.TENNIS,
                author = "Camille Vasseur",
                date = "Il y a 2h",
                readTimeMinutes = 3,
                isBreaking = false,
                likesCount = 156,
                viewsCount = 7400,
                tags = listOf("Roland-Garros", "Alcaraz", "Sinner", "Tennis")
            ),
            Article(
                id = "art-5",
                title = "F1 : Pole position sensationnelle de Leclerc à Monaco sous une pluie battante",
                summary = "Le pilote de la Scuderia Ferrari a signé un tour d'anthologie dans les rues de la Principauté pour devancer Verstappen de 42 millièmes.",
                content = """
                    Des conditions extrêmes, des rails effleurés à plus de 280 km/h et un public en transe : Charles Leclerc a dompté la tempête monégasque lors d'une séance de qualification à rebondissements.
                    
                    Alors que la piste s'asséchait progressivement dans les ultimes secondes de la Q3, le Monégasque a extrait le potentiel maximal de sa monoplace rouge pour s'emparer de la pole position.
                    
                    Max Verstappen partira à ses côtés en première ligne, promettant un départ ultra-tendu au premier virage de Sainte-Dévote.
                """.trimIndent(),
                category = SportCategory.F1,
                author = "Frédéric Arnaud",
                date = "Il y a 3h",
                readTimeMinutes = 3,
                isBreaking = false,
                likesCount = 212,
                viewsCount = 11300,
                tags = listOf("F1", "Ferrari", "Leclerc", "Monaco")
            ),
            Article(
                id = "art-6",
                title = "Top 14 : Le Stade Toulousain s'impose avec le bonus offensif à l'issue d'un festival d'essais",
                summary = "Emmenés par un Antoine Dupont intenable, les Rouge et Noir confortent leur fauteuil de leader avec 6 essais inscrits.",
                content = """
                    Du grand spectacle à Ernest-Wallon ! Le Stade Toulousain a régalé son public en surclassant son dauphin au terme d'une rencontre rythmée par la virtuosité de ses lignes arrière.
                    
                    Avec un Antoine Dupont inspiré dans toutes ses prises d'initiatives au pied et à la main, Toulouse a fait la différence dès la première demi-heure avec trois essais de grande classe.
                    
                    Le point de bonus offensif vient couronner une prestation exemplaire avant la trêve internationale.
                """.trimIndent(),
                category = SportCategory.RUGBY,
                author = "Guillaume Cazaux",
                date = "Il y a 4h",
                readTimeMinutes = 2,
                likesCount = 177,
                viewsCount = 6200,
                tags = listOf("Top 14", "Toulouse", "Dupont", "Rugby")
            ),
            Article(
                id = "art-7",
                title = "UFC : Benoît Saint Denis enflamme l'Accor Arena avec un KO au 1er round !",
                summary = "Le guerrier français 'God of War' a foudroyé son rival d'un high kick dévastateur en moins de trois minutes de combat.",
                content = """
                    Une ambiance de folie a secoué Bercy hier soir. Pour le main event de l'UFC Paris, Benoît Saint Denis a envoyé un message éclatant à toute la division lightweight.
                    
                    Imposant une intensité irrespirable dès le coup de cloche initial, BSD a acculé son adversaire contre la cage avant de décocher un coup de pied magistral à la tempe. L'arbitre n'a eu d'autre choix que d'arrêter le combat immédiatement.
                    
                    Dans la foulée, le Français a réclamé un combat pour la ceinture mondiale en fin d'année.
                """.trimIndent(),
                category = SportCategory.MMA,
                author = "Sofiane Benali",
                date = "Il y a 5h",
                readTimeMinutes = 3,
                likesCount = 330,
                viewsCount = 15800,
                tags = listOf("UFC", "MMA", "Saint Denis", "Combat")
            ),
            Article(
                id = "art-8",
                title = "Cyclisme : Tadej Pogacar écrase la concurrence sur le Tour des Flandres",
                summary = "Une attaque solitaire à plus de 45 kilomètres de l'arrivée dans le Vieux Quaremont a laissé ses rivaux sans réponse.",
                content = """
                    Le cannibale slovène a encore frappé. Dans les pavés et les monts flandriens balayés par le vent, Tadej Pogacar a produit une accélération fulgurante à laquelle personne n'a pu répondre.
                    
                    Creusant un écart d'une minute en moins de dix bornes, il s'est présenté seul sur la ligne droite d'arrivée à Audenarde, savourant un nouveau Monument dans sa légende personnelle.
                """.trimIndent(),
                category = SportCategory.CYCLING,
                author = "Benoît Vande",
                date = "Il y a 6h",
                readTimeMinutes = 2,
                likesCount = 142,
                viewsCount = 5900,
                tags = listOf("Cyclisme", "Pogacar", "Flandres")
            )
        )

        val initialMatches = listOf(
            Match(
                id = "match-1",
                category = SportCategory.FOOTBALL,
                competition = "Ligue 1 - 28e journée",
                homeTeam = "Paris SG",
                awayTeam = "Olympique de Marseille",
                homeScore = 2,
                awayScore = 1,
                status = MatchStatus.LIVE,
                currentMinute = "74'",
                timeOrDate = "En direct",
                venue = "Parc des Princes",
                homeScorers = listOf("Dembélé 28'", "Barcola 64'"),
                awayScorers = listOf("Greenwood 42'"),
                events = listOf(
                    MatchEvent("28'", "But exceptionnel de Dembélé du pied gauche", MatchEventType.GOAL, true),
                    MatchEvent("39'", "Carton jaune pour Balerdi après un tacle appuyé", MatchEventType.YELLOW_CARD, false),
                    MatchEvent("42'", "Égalisation marseillaise sur contre rapide de Greenwood", MatchEventType.GOAL, false),
                    MatchEvent("64'", "Barcola redonne l'avantage sur une passe en profondeur lumineuse", MatchEventType.GOAL, true)
                ),
                stats = MatchStats(
                    possessionHome = 62,
                    possessionAway = 38,
                    shotsHome = 14,
                    shotsAway = 6,
                    shotsOnTargetHome = 7,
                    shotsOnTargetAway = 3,
                    foulsHome = 8,
                    foulsAway = 12
                )
            ),
            Match(
                id = "match-2",
                category = SportCategory.FOOTBALL,
                competition = "Ligue des Champions - 1/4 retour",
                homeTeam = "Real Madrid",
                awayTeam = "Manchester City",
                homeScore = 1,
                awayScore = 1,
                status = MatchStatus.LIVE,
                currentMinute = "62'",
                timeOrDate = "En direct",
                venue = "Santiago Bernabéu",
                homeScorers = listOf("Vinicius Jr 18'"),
                awayScorers = listOf("Haaland 53' (pen)"),
                events = listOf(
                    MatchEvent("18'", "Frappe enroulée de Vinicius Jr en pleine lucarne", MatchEventType.GOAL, true),
                    MatchEvent("53'", "Penalty transformé avec sang-froid par Haaland", MatchEventType.PENALTY, false)
                ),
                stats = MatchStats(
                    possessionHome = 45,
                    possessionAway = 55,
                    shotsHome = 9,
                    shotsAway = 11,
                    shotsOnTargetHome = 4,
                    shotsOnTargetAway = 5,
                    foulsHome = 10,
                    foulsAway = 9
                )
            ),
            Match(
                id = "match-3",
                category = SportCategory.BASKETBALL,
                competition = "NBA - Saison Régulière",
                homeTeam = "LA Lakers",
                awayTeam = "Golden State Warriors",
                homeScore = 98,
                awayScore = 94,
                status = MatchStatus.LIVE,
                currentMinute = "Q4 - 3:12",
                timeOrDate = "En direct",
                venue = "Crypto.com Arena",
                homeScorers = listOf("James 26 pts", "Davis 22 pts"),
                awayScorers = listOf("Curry 31 pts", "Thompson 18 pts"),
                events = listOf(
                    MatchEvent("Q4 5:20", "Tir à 3 points clutch de Stephen Curry", MatchEventType.BASKET_THREE, false),
                    MatchEvent("Q4 4:10", "Dunk puissant de LeBron James sur alley-oop", MatchEventType.GOAL, true)
                ),
                stats = MatchStats(possessionHome = 51, possessionAway = 49, shotsHome = 78, shotsAway = 74)
            ),
            Match(
                id = "match-4",
                category = SportCategory.FOOTBALL,
                competition = "Ligue 1 - 28e journée",
                homeTeam = "AS Monaco",
                awayTeam = "Olympique Lyonnais",
                homeScore = 3,
                awayScore = 2,
                status = MatchStatus.FINISHED,
                currentMinute = "FT",
                timeOrDate = "Terminé (Hier)",
                venue = "Stade Louis II",
                homeScorers = listOf("Ben Seghir 12'", "Balogun 45'", "Akliouche 88'"),
                awayScorers = listOf("Lacazette 33'", "Cherki 76'"),
                stats = MatchStats(possessionHome = 54, possessionAway = 46, shotsHome = 16, shotsAway = 13)
            ),
            Match(
                id = "match-5",
                category = SportCategory.RUGBY,
                competition = "Top 14 - 21e journée",
                homeTeam = "Stade Toulousain",
                awayTeam = "Stade Rochelais",
                homeScore = 31,
                awayScore = 17,
                status = MatchStatus.FINISHED,
                currentMinute = "FT",
                timeOrDate = "Terminé (Hier)",
                venue = "Stade Ernest-Wallon",
                homeScorers = listOf("Dupont 14' (Essai)", "Ramos 38' (Essai)", "Lebel 71' (Essai)"),
                awayScorers = listOf("Alldritt 49' (Essai)"),
                stats = MatchStats(possessionHome = 58, possessionAway = 42, foulsHome = 6, foulsAway = 11)
            ),
            Match(
                id = "match-6",
                category = SportCategory.TENNIS,
                competition = "ATP Masters 1000",
                homeTeam = "C. Alcaraz",
                awayTeam = "J. Sinner",
                homeScore = 0,
                awayScore = 0,
                status = MatchStatus.SCHEDULED,
                currentMinute = "Prévu",
                timeOrDate = "Demain 15:00",
                venue = "Court Central",
                stats = MatchStats()
            ),
            Match(
                id = "match-7",
                category = SportCategory.F1,
                competition = "Grand Prix de Monaco",
                homeTeam = "C. Leclerc (FER)",
                awayTeam = "M. Verstappen (RBR)",
                homeScore = 1,
                awayScore = 2,
                status = MatchStatus.SCHEDULED,
                currentMinute = "Course",
                timeOrDate = "Dimanche 15:00",
                venue = "Circuit de Monaco",
                stats = MatchStats()
            )
        )

        val initialMercato = listOf(
            MercatoDeal(
                id = "m-1",
                playerName = "Florian Wirtz",
                position = "Milieu Offensif",
                fromClub = "Bayer Leverkusen",
                toClub = "Real Madrid",
                transferFee = "125 M€",
                status = "OFFICIEL",
                dateFormatted = "Aujourd'hui 14:30",
                details = "Signature d'un bail de 6 ans. Présentation officielle vendredi au Bernabéu.",
                category = SportCategory.FOOTBALL
            ),
            MercatoDeal(
                id = "m-2",
                playerName = "Khvicha Kvaratskhelia",
                position = "Ailier Gauche",
                fromClub = "Naples",
                toClub = "Paris Saint-Germain",
                transferFee = "85 M€",
                status = "ACCORD TOTAL",
                dateFormatted = "Aujourd'hui 11:15",
                details = "Derniers détails réglés entre les dirigeants parisiens et napolitains. Visite médicale imminente.",
                category = SportCategory.FOOTBALL
            ),
            MercatoDeal(
                id = "m-3",
                playerName = "Trent Alexander-Arnold",
                position = "Latéral Droit",
                fromClub = "Liverpool",
                toClub = "Real Madrid",
                transferFee = "Libre (Fin de contrat)",
                status = "RUMEUR CHAUDE",
                dateFormatted = "Hier 19:40",
                details = "Le joueur anglais a refusé une nouvelle offre de prolongation de Liverpool.",
                category = SportCategory.FOOTBALL
            ),
            MercatoDeal(
                id = "m-4",
                playerName = "Rayan Cherki",
                position = "Meneur de jeu",
                fromClub = "Lyon",
                toClub = "Borussia Dortmund",
                transferFee = "28 M€",
                status = "NÉGOCIATIONS",
                dateFormatted = "Hier 16:00",
                details = "Le club allemand a formulé une première offre officielle acceptée en principe par l'OL.",
                category = SportCategory.FOOTBALL
            ),
            MercatoDeal(
                id = "m-5",
                playerName = "Lamine Yamal",
                position = "Ailier Droit",
                fromClub = "FC Barcelone",
                toClub = "FC Barcelone",
                transferFee = "Clause 1 Milliard d'€",
                status = "PROLONGATION",
                dateFormatted = "Il y a 2 jours",
                details = "La pépite espagnole a paraphé un nouveau bail le liant au club catalan jusqu'en 2031.",
                category = SportCategory.FOOTBALL
            )
        )

        val initialPolls = listOf(
            SportPoll(
                id = "poll-1",
                question = "Qui remportera la Ligue des Champions cette saison ?",
                category = SportCategory.FOOTBALL,
                options = listOf(
                    PollOption(1, "Paris Saint-Germain", 412),
                    PollOption(2, "Real Madrid", 583),
                    PollOption(3, "Manchester City", 389),
                    PollOption(4, "Bayern Munich / Autre", 145)
                ),
                totalVotes = 1529
            ),
            SportPoll(
                id = "poll-2",
                question = "Victor Wembanyama sera-t-il All-Star et Défenseur de l'Année ?",
                category = SportCategory.BASKETBALL,
                options = listOf(
                    PollOption(1, "Oui, sans aucun doute !", 840),
                    PollOption(2, "Seulement All-Star", 210),
                    PollOption(3, "Encore un an à attendre", 65)
                ),
                totalVotes = 1115
            )
        )

        val ligue1Rankings = listOf(
            RankingItem(1, "Paris Saint-Germain", "Paris", 27, 20, 5, 2, 65, "+44", listOf("V","V","N","V","V")),
            RankingItem(2, "AS Monaco", "Monaco", 27, 16, 5, 6, 53, "+21", listOf("V","D","V","V","V")),
            RankingItem(3, "Olympique de Marseille", "Marseille", 27, 15, 6, 6, 51, "+18", listOf("D","V","V","N","V")),
            RankingItem(4, "Lille OSC", "Lille", 27, 14, 7, 6, 49, "+16", listOf("V","N","V","D","V")),
            RankingItem(5, "OGC Nice", "Nice", 27, 13, 8, 6, 47, "+12", listOf("N","V","D","V","D")),
            RankingItem(6, "Olympique Lyonnais", "Lyon", 27, 13, 6, 8, 45, "+8", listOf("D","V","V","V","D")),
            RankingItem(7, "RC Lens", "Lens", 27, 12, 7, 8, 43, "+7", listOf("V","D","N","V","N")),
            RankingItem(8, "Stade Rennais", "Rennes", 27, 11, 7, 9, 40, "+3", listOf("D","N","V","D","V"))
        )

        val nbaRankings = listOf(
            RankingItem(1, "Boston Celtics", "Conférence Est", 72, 57, 0, 15, 114, "+11.2", listOf("V","V","V","D","V")),
            RankingItem(2, "Denver Nuggets", "Conférence Ouest", 73, 51, 0, 22, 102, "+7.5", listOf("V","V","D","V","V")),
            RankingItem(3, "Oklahoma City Thunder", "Conférence Ouest", 72, 50, 0, 22, 100, "+7.3", listOf("D","V","V","V","D")),
            RankingItem(4, "Minnesota Timberwolves", "Conférence Ouest", 73, 49, 0, 24, 98, "+6.8", listOf("V","D","V","V","V")),
            RankingItem(5, "Milwaukee Bucks", "Conférence Est", 72, 46, 0, 26, 92, "+4.1", listOf("D","V","D","V","D")),
            RankingItem(6, "LA Lakers", "Conférence Ouest", 73, 43, 0, 30, 86, "+1.8", listOf("V","V","V","D","V")),
            RankingItem(7, "San Antonio Spurs", "Conférence Ouest", 73, 31, 0, 42, 62, "-4.2", listOf("V","V","D","V","D"))
        )

        val f1Rankings = listOf(
            RankingItem(1, "Max Verstappen", "Red Bull Racing", 6, 4, 5, 0, 136, "Leader", listOf("1er","1er","2e","1er")),
            RankingItem(2, "Charles Leclerc", "Scuderia Ferrari", 6, 1, 4, 0, 112, "-24 pts", listOf("3e","2e","1er","3e")),
            RankingItem(3, "Lando Norris", "McLaren", 6, 1, 3, 0, 98, "-38 pts", listOf("2e","1er","4e","2e")),
            RankingItem(4, "Carlos Sainz", "Scuderia Ferrari", 6, 0, 3, 0, 85, "-51 pts", listOf("4e","3e","3e","5e")),
            RankingItem(5, "Lewis Hamilton", "Mercedes", 6, 0, 1, 0, 54, "-82 pts", listOf("5e","6e","5e","4e")),
            RankingItem(6, "Oscar Piastri", "McLaren", 6, 0, 1, 0, 48, "-88 pts", listOf("6e","4e","6e","6e"))
        )

        val top14Rankings = listOf(
            RankingItem(1, "Stade Toulousain", "Toulouse", 20, 15, 0, 5, 68, "+180", listOf("V","V","V","D","V")),
            RankingItem(2, "Stade Français", "Paris", 20, 14, 1, 5, 63, "+95", listOf("V","D","V","V","N")),
            RankingItem(3, "Bordeaux-Bègles", "Bordeaux", 20, 13, 0, 7, 59, "+110", listOf("D","V","V","V","D")),
            RankingItem(4, "Stade Rochelais", "La Rochelle", 20, 12, 0, 8, 54, "+60", listOf("D","V","D","V","V")),
            RankingItem(5, "Racing 92", "Hauts-de-Seine", 20, 11, 0, 9, 52, "+45", listOf("V","D","D","V","D"))
        )
    }
}
