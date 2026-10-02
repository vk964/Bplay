package com.bel9ja.boardclub.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.data.SampleData
import com.bel9ja.boardclub.data.SessionFilters
import com.bel9ja.boardclub.ui.components.ChoiceChip
import com.bel9ja.boardclub.ui.components.EventCard
import com.bel9ja.boardclub.ui.theme.Orange

@Composable
fun SessionsScreen(onOpenEvent: (BoardEvent) -> Unit) {
    var filters by remember { mutableStateOf(SessionFilters()) }
    var showFilters by remember { mutableStateOf(false) }

    val days = listOf("All") + SampleData.events.map { it.day }.distinct()
    val categories = listOf("All") + SampleData.events.map { it.category }.distinct()
    val difficulties = listOf("All") + SampleData.events.map { it.difficulty }.distinct()

    val filtered = SampleData.events.filter { ev ->
        (filters.day == "All" || ev.day == filters.day) &&
            (filters.category == "All" || ev.category == filters.category) &&
            (filters.difficulty == "All" || ev.difficulty == filters.difficulty) &&
            (filters.format == "All" || ev.format == filters.format) &&
            (filters.price == "All" || (if (filters.price == "Free") ev.isFree else !ev.isFree))
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showFilters = !showFilters }) {
                    Text(
                        if (filters.activeCount > 0) "Filters \u00b7 ${filters.activeCount}" else "Filters",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
                if (filters.activeCount > 0) {
                    TextButton(onClick = { filters = SessionFilters() }) {
                        Text("Clear", color = Orange, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
        }

        if (showFilters) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    FilterGroup("Date", days, filters.day) { filters = filters.copy(day = it) }
                    FilterGroup("Game type", categories, filters.category) { filters = filters.copy(category = it) }
                    FilterGroup("Difficulty", difficulties, filters.difficulty) { filters = filters.copy(difficulty = it) }
                    FilterGroup("Format", listOf("All", "Individual", "Team"), filters.format) { filters = filters.copy(format = it) }
                    FilterGroup("Price", listOf("All", "Free", "Paid"), filters.price) { filters = filters.copy(price = it) }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No sessions match those filters. Try clearing a filter to see more.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(filtered) { ev ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    EventCard(event = ev, onClick = { onOpenEvent(ev) })
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(label: String, options: List<String>, value: String, onChange: (String) -> Unit) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { opt ->
                ChoiceChip(label = opt, selected = value == opt, onClick = { onChange(opt) })
            }
        }
    }
}
