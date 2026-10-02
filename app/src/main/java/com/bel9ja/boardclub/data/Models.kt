package com.bel9ja.boardclub.data

data class BoardEvent(
    val id: String,
    val name: String,
    val game: String,
    val day: String,
    val date: String,
    val time: String,
    val duration: String,
    val difficulty: String,
    val difficultyLevel: Int,
    val format: String,
    val places: Int,
    val placesLeft: Int,
    val price: String,
    val category: String,
    val description: String,
    val rules: String,
    val bring: String
) {
    val isFree: Boolean get() = price == "Free"
}

data class BoardGame(
    val name: String,
    val category: String,
    val players: String,
    val duration: String,
    val difficulty: String,
    val beginnerFriendly: Boolean,
    val description: String
)

data class ScheduleDay(
    val day: String,
    val activity: String,
    val note: String,
    val eventId: String?
)

data class Faq(val question: String, val answer: String)

data class SessionFilters(
    val day: String = "All",
    val category: String = "All",
    val difficulty: String = "All",
    val format: String = "All",
    val price: String = "All"
) {
    val activeCount: Int
        get() = listOf(
            day != "All",
            category != "All",
            difficulty != "All",
            format != "All",
            price != "All"
        ).count { it }
}
