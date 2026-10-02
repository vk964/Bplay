package com.bel9ja.boardclub.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.data.SampleData
import com.bel9ja.boardclub.ui.components.EventCard
import com.bel9ja.boardclub.ui.components.SectionTitle
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy800
import com.bel9ja.boardclub.ui.theme.Navy900
import com.bel9ja.boardclub.ui.theme.Orange
import com.bel9ja.boardclub.ui.theme.Yellow

private data class Benefit(val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

private val benefits = listOf(
    Benefit(Icons.Default.Groups, "Friendly community"),
    Benefit(Icons.Default.CalendarViewWeek, "Weekly game nights"),
    Benefit(Icons.Default.Star, "Beginner-friendly sessions"),
    Benefit(Icons.Default.EmojiEvents, "Competitive tournaments")
)

@Composable
fun HomeScreen(
    onViewSessions: () -> Unit,
    onJoinClub: () -> Unit,
    onOpenEvent: (BoardEvent) -> Unit
) {
    val featured = SampleData.events.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(20.dp, 24.dp, 20.dp, 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(Orange, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("B9", color = Color(0xFF16223F), fontWeight = FontWeight.Black, fontSize = 17.sp)
                    }
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Text(
                        "Bel9ja Board Club",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    "A tabletop community where beginners and veterans share the same table \u2014 weekly game nights, casual drop-ins, and a friendly tournament scene.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onViewSessions,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF16223F))
                ) {
                    Text("View Upcoming Sessions", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onJoinClub,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Navy700)
                ) {
                    Text("Join the Club", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionTitle("Featured session")
                }
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    EventCard(event = featured, onClick = { onOpenEvent(featured) })
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionTitle("Popular games this month")
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SampleData.games.take(6)) { game ->
                        Row(
                            modifier = Modifier
                                .background(Navy900, RoundedCornerShape(999.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Yellow, CircleShape)
                            )
                            Spacer(modifier = Modifier.padding(start = 7.dp))
                            Text(game.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 4.dp)) {
                SectionTitle("Why players stay")
                val rows = benefits.chunked(2)
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { b ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Navy900, RoundedCornerShape(16.dp))
                                    .padding(14.dp)
                            ) {
                                Icon(b.icon, contentDescription = null, tint = Yellow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(b.label, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
