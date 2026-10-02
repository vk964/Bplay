package com.bel9ja.boardclub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.data.SampleData
import com.bel9ja.boardclub.data.ScheduleDay
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy900
import com.bel9ja.boardclub.ui.theme.Orange

@Composable
fun ScheduleScreen(onOpenEvent: (BoardEvent) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Text(
                "Tap a day to see what's on and register directly.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(20.dp, 14.dp, 20.dp, 6.dp)
            )
        }
        items(SampleData.schedule) { s ->
            val linked = s.eventId?.let { id -> SampleData.events.find { it.id == id } }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clickable(enabled = linked != null) { linked?.let(onOpenEvent) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900),
                border = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        s.day,
                        color = Orange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        modifier = Modifier.width(64.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.activity, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        Text(s.note, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.5.sp)
                    }
                    if (linked != null) {
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF5B6994))
                    }
                }
            }
        }
    }
}
