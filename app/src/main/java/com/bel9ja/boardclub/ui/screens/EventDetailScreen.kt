package com.bel9ja.boardclub.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.ui.components.InfoRow
import com.bel9ja.boardclub.ui.components.SectionTitle
import com.bel9ja.boardclub.ui.theme.Green
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy800
import com.bel9ja.boardclub.ui.theme.Navy900
import com.bel9ja.boardclub.ui.theme.Orange
import com.bel9ja.boardclub.ui.theme.Yellow

@Composable
fun EventDetailScreen(event: BoardEvent, onJoinSession: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(20.dp, 22.dp, 20.dp, 12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Navy800, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(Icons.Default.Star, contentDescription = null, tint = Yellow)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(event.name, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                    Text(event.game, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(20.dp, 4.dp, 20.dp, 6.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)
                ) {
                    InfoRow(Icons.Default.CalendarMonth, "Date", "${event.day}, ${event.date}")
                    InfoRow(Icons.Default.Schedule, "Time & duration", "${event.time} \u00b7 ${event.duration}")
                    InfoRow(Icons.Default.Place, "Location", "Bel9ja Board Club \u2014 Main Hall")
                    InfoRow(Icons.Default.Groups, "Format", if (event.format == "Team") "Team (2\u20134 players)" else "Individual sign-up")
                    InfoRow(Icons.Default.Star, "Skill level", event.difficulty)
                    InfoRow(Icons.Default.VerifiedUser, "Entry", event.price)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .padding(20.dp, 10.dp, 20.dp, 4.dp)
                        .fillMaxWidth()
                        .background(Navy900, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        "${event.placesLeft} of ${event.places} places still open",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val fraction = 1f - (event.placesLeft.toFloat() / event.places.toFloat())
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(Navy700, RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                                .height(6.dp)
                                .background(Green, RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(20.dp, 16.dp, 20.dp, 4.dp)) {
                    SectionTitle("About this session")
                    Text(event.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Column(modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 4.dp)) {
                    SectionTitle("Basic rules")
                    Text(event.rules, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            item {
                Column(modifier = Modifier.padding(20.dp, 12.dp, 20.dp, 4.dp)) {
                    SectionTitle("What to bring")
                    Text(event.bring, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp, 12.dp, 20.dp, 18.dp)
        ) {
            Button(
                onClick = onJoinSession,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF16223F))
            ) {
                Text("Join This Session", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}
