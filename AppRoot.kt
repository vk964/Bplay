package com.bel9ja.boardclub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.data.BoardGame
import com.bel9ja.boardclub.ui.screens.ClubScreen
import com.bel9ja.boardclub.ui.screens.EventDetailScreen
import com.bel9ja.boardclub.ui.screens.GameDetailScreen
import com.bel9ja.boardclub.ui.screens.GamesScreen
import com.bel9ja.boardclub.ui.screens.HomeScreen
import com.bel9ja.boardclub.ui.screens.RegistrationConfirmScreen
import com.bel9ja.boardclub.ui.screens.RegistrationResult
import com.bel9ja.boardclub.ui.screens.RegistrationScreen
import com.bel9ja.boardclub.ui.screens.ScheduleScreen
import com.bel9ja.boardclub.ui.screens.SessionsScreen
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy950
import com.bel9ja.boardclub.ui.theme.Orange

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(BottomTab.HOME) }
    var clubTab by remember { mutableStateOf(ClubTab.ABOUT) }
    var screen by remember { mutableStateOf<Screen>(Screen.Main) }

    fun openEvent(event: BoardEvent, from: BottomTab) {
        screen = Screen.EventDetail(event, from)
    }

    when (val s = screen) {
        is Screen.EventDetail -> {
            TopBarScaffold(title = "Event details", onBack = { screen = Screen.Main }) {
                EventDetailScreen(
                    event = s.event,
                    onJoinSession = { screen = Screen.Registration(s.event, s.returnTab) }
                )
            }
        }
        is Screen.Registration -> {
            TopBarScaffold(title = "Session Registration", onBack = { screen = Screen.EventDetail(s.event, s.returnTab) }) {
                RegistrationScreen(
                    event = s.event,
                    onSubmitted = { result: RegistrationResult ->
                        screen = Screen.RegistrationConfirm(result.event, result.participants, result.reference, s.returnTab)
                    }
                )
            }
        }
        is Screen.RegistrationConfirm -> {
            RegistrationConfirmScreen(
                result = RegistrationResult(s.event, s.participants, s.reference),
                onBackToHome = {
                    tab = BottomTab.HOME
                    screen = Screen.Main
                }
            )
        }
        is Screen.GameDetail -> {
            TopBarScaffold(title = "Game details", onBack = { screen = Screen.Main }) {
                GameDetailScreen(game = s.game)
            }
        }
        Screen.Main -> {
            Scaffold(
                containerColor = Navy950,
                bottomBar = {
                    Bel9jaBottomNav(tab) { newTab ->
                        tab = newTab
                        screen = Screen.Main
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Navy950)
                ) {
                    when (tab) {
                        BottomTab.HOME -> HomeScreen(
                            onViewSessions = { tab = BottomTab.SESSIONS },
                            onJoinClub = { tab = BottomTab.CLUB; clubTab = ClubTab.JOIN },
                            onOpenEvent = { ev -> openEvent(ev, BottomTab.HOME) }
                        )
                        BottomTab.SESSIONS -> SessionsScreen(
                            onOpenEvent = { ev -> openEvent(ev, BottomTab.SESSIONS) }
                        )
                        BottomTab.GAMES -> GamesScreen(
                            onOpenGame = { g: BoardGame -> screen = Screen.GameDetail(g) }
                        )
                        BottomTab.SCHEDULE -> ScheduleScreen(
                            onOpenEvent = { ev -> openEvent(ev, BottomTab.SCHEDULE) }
                        )
                        BottomTab.CLUB -> ClubScreen(
                            clubTab = clubTab,
                            onClubTabChange = { clubTab = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarScaffold(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        containerColor = Navy950,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Navy950)
                    .padding(start = 6.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 36.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Navy950)
        ) {
            content()
        }
    }
}

@Composable
private fun Bel9jaBottomNav(current: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar(containerColor = Navy950, contentColor = MaterialTheme.colorScheme.onBackground) {
        val items = listOf(
            Triple(BottomTab.HOME, "Home", Icons.Default.Home),
            Triple(BottomTab.SESSIONS, "Sessions", Icons.Default.CalendarMonth),
            Triple(BottomTab.GAMES, "Games", Icons.Default.Casino),
            Triple(BottomTab.SCHEDULE, "Schedule", Icons.Default.CalendarViewWeek),
            Triple(BottomTab.CLUB, "Club", Icons.Default.Groups)
        )
        items.forEach { (t, label, icon) ->
            NavigationBarItem(
                selected = current == t,
                onClick = { onSelect(t) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Orange,
                    selectedTextColor = Orange,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Navy950
                )
            )
        }
    }
}
