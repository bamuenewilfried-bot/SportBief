package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.MatchEventType
import com.example.data.model.MatchStatus
import com.example.ui.theme.SportsCyan
import com.example.ui.theme.SportsGold
import com.example.ui.theme.SportsLiveRed
import com.example.ui.theme.SportsOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailSheet(
    match: Match,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .testTag("match_detail_sheet")
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = match.competition,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "📍 ${match.venue}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_match_detail_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Fermer")
                }
            }

            // Big Scoreboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (match.status.isLive) SportsLiveRed.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (match.status.isLive) "🔴 ${match.status.label} • ${match.currentMinute}" else match.status.label,
                            color = if (match.status.isLive) SportsLiveRed else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Teams & Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Home
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SportsOrange.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = match.homeTeam.take(2).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = SportsOrange
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.homeTeam,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Score
                        Text(
                            text = "${match.homeScore} - ${match.awayScore}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 36.sp
                            ),
                            color = if (match.status.isLive) SportsLiveRed else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Away
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(SportsCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = match.awayTeam.take(2).uppercase(),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = SportsCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.awayTeam,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Scorers
                    if (match.homeScorers.isNotEmpty() || match.awayScorers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                match.homeScorers.forEach { s ->
                                    Text(
                                        text = "⚽ $s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                match.awayScorers.forEach { s ->
                                    Text(
                                        text = "$s ⚽",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Statistics Section
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Statistiques de la rencontre",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StatComparisonRow(
                        label = "Possession de balle",
                        homeValue = "${match.stats.possessionHome}%",
                        awayValue = "${match.stats.possessionAway}%",
                        homeRatio = match.stats.possessionHome / 100f
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StatComparisonRow(
                        label = "Tirs au but",
                        homeValue = "${match.stats.shotsHome}",
                        awayValue = "${match.stats.shotsAway}",
                        homeRatio = if (match.stats.shotsHome + match.stats.shotsAway > 0)
                            match.stats.shotsHome.toFloat() / (match.stats.shotsHome + match.stats.shotsAway)
                        else 0.5f
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StatComparisonRow(
                        label = "Tirs cadrés",
                        homeValue = "${match.stats.shotsOnTargetHome}",
                        awayValue = "${match.stats.shotsOnTargetAway}",
                        homeRatio = if (match.stats.shotsOnTargetHome + match.stats.shotsOnTargetAway > 0)
                            match.stats.shotsOnTargetHome.toFloat() / (match.stats.shotsOnTargetHome + match.stats.shotsOnTargetAway)
                        else 0.5f
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StatComparisonRow(
                        label = "Fautes commises",
                        homeValue = "${match.stats.foulsHome}",
                        awayValue = "${match.stats.foulsAway}",
                        homeRatio = if (match.stats.foulsHome + match.stats.foulsAway > 0)
                            match.stats.foulsHome.toFloat() / (match.stats.foulsHome + match.stats.foulsAway)
                        else 0.5f
                    )
                }
            }

            // Timeline of Events
            if (match.events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fil du match (Temps forts)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        match.events.forEachIndexed { i, event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SportsOrange.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = event.minute,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = SportsOrange
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                val iconEmoji = when (event.type) {
                                    MatchEventType.GOAL -> "⚽"
                                    MatchEventType.YELLOW_CARD -> "🟨"
                                    MatchEventType.RED_CARD -> "🟥"
                                    MatchEventType.PENALTY -> "🎯"
                                    MatchEventType.BASKET_THREE -> "🏀"
                                    MatchEventType.TRY -> "🏉"
                                    MatchEventType.OVERTAKE -> "🏎️"
                                }

                                Text(text = iconEmoji, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = event.description,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (event.type == MatchEventType.GOAL) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (i < match.events.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatComparisonRow(
    label: String,
    homeValue: String,
    awayValue: String,
    homeRatio: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = homeValue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SportsOrange
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = awayValue,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = SportsCyan
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { homeRatio.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = SportsOrange,
            trackColor = SportsCyan
        )
    }
}
