package com.bel9ja.boardclub.ui

import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.data.BoardGame

enum class BottomTab { HOME, SESSIONS, GAMES, SCHEDULE, CLUB }

enum class ClubTab { ABOUT, JOIN, FAQ, CONTACT }

sealed class Screen {
    object Main : Screen()
    data class EventDetail(val event: BoardEvent, val returnTab: BottomTab) : Screen()
    data class Registration(val event: BoardEvent, val returnTab: BottomTab) : Screen()
    data class RegistrationConfirm(
        val event: BoardEvent,
        val participants: String,
        val reference: String,
        val returnTab: BottomTab
    ) : Screen()
    data class GameDetail(val game: BoardGame) : Screen()
}
