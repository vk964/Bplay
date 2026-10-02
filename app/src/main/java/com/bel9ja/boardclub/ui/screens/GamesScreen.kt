package com.bel9ja.boardclub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.BoardGame
import com.bel9ja.boardclub.data.SampleData
import com.bel9ja.boardclub.ui.components.ChoiceChip
import com.bel9ja.boardclub.ui.components.InfoRow
import com.bel9ja.boardclub.ui.components.PillTag
import com.bel9ja.boardclub.ui.components.SectionTitle
import com.bel9ja.boardclub.ui.theme.Green
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy800
import com.bel9ja.boardclub.ui.theme.Navy900
import com.bel9ja.boardclub.ui.theme.Yellow

@Composable
fun GamesScreen(onOpenGame: (BoardGame) -> Unit) {
    var category by remember { mutableStateOf("All") }
    val categories = listOf("All") + SampleData.games.map { it.category }.distinct()
    val shown = if (category == "All") SampleData.games else SampleData.games.filter { it.category == category }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { c ->
                ChoiceChip(c, category == c) { category = c }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(20.dp, 0.dp, 20.dp, 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shown) { game ->
                Card(
                    onClick = { onOpenGame(game) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Navy700),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Navy800, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, tint = Yellow, modifier = Modifier.size(18.dp))
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(game.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                        Text("${game.players} players \u00b7 ${game.duration}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (game.beginnerFriendly) {
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(6.dp))
                            PillTag("Beginner friendly", Green)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameDetailScreen(game: BoardGame) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Column(modifier = Modifier.padding(20.dp, 22.dp, 20.dp, 12.dp)) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Navy800, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Casino, contentDescription = null, tint = Yellow)
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(10.dp))
                Text(game.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(game.category, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        item {
            Column(
                modifier = Modifier.padding(20.dp, 4.dp, 20.dp, 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InfoRow(Icons.Default.Groups, "Players", game.players)
                InfoRow(Icons.Default.Schedule, "Average duration", game.duration)
                InfoRow(Icons.Default.Star, "Difficulty", game.difficulty)
                InfoRow(
                    Icons.Default.VerifiedUser,
                    "Beginner sessions",
                    if (game.beginnerFriendly) "Available" else "Not currently scheduled"
                )
            }
        }
        item {
            Column(modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 4.dp)) {
                SectionTitle("About this game")
                Text(game.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column(modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 4.dp)) {
                SectionTitle("Play it at the club")
                Text(
                    "Check the Sessions and Schedule screens for the next table hosting ${game.name}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
