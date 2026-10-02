package com.bel9ja.boardclub.data

object SampleData {

    val events = listOf(
        BoardEvent(
            id = "catan-fri",
            name = "Catan Strategy Night",
            game = "Catan",
            day = "Friday",
            date = "Sep 12",
            time = "7:00 PM",
            duration = "2.5 hrs",
            difficulty = "Beginner & Intermediate",
            difficultyLevel = 2,
            format = "Individual",
            places = 12,
            placesLeft = 5,
            price = "Free",
            category = "Strategy",
            description = "Trade, build, and settle your way across the island of Catan. A relaxed table for players who know the basics and a couple of first-timers who want to learn on the spot.",
            rules = "Standard Catan rules, 3-4 players per board. New players get a five-minute walkthrough before the first round starts.",
            bring = "Nothing required - boards, pieces, and scorepads are provided."
        ),
        BoardEvent(
            id = "ttr-sat",
            name = "Ticket to Ride Social Evening",
            game = "Ticket to Ride",
            day = "Saturday",
            date = "Sep 13",
            time = "6:00 PM",
            duration = "2 hrs",
            difficulty = "Beginner-friendly",
            difficultyLevel = 1,
            format = "Individual",
            places = 16,
            placesLeft = 11,
            price = "Free",
            category = "Family",
            description = "Claim railway routes across the map in this easygoing, sociable session built for newcomers. Come solo or bring friends - tables mix freely.",
            rules = "Base US map, standard rules. Games run in short rounds so new tables can start every 30 minutes.",
            bring = "Nothing required. Snacks and drinks are welcome at the table."
        ),
        BoardEvent(
            id = "chess-sun",
            name = "Chess Club Challenge",
            game = "Chess",
            day = "Sunday",
            date = "Sep 14",
            time = "4:00 PM",
            duration = "3 hrs",
            difficulty = "Intermediate",
            difficultyLevel = 2,
            format = "Individual",
            places = 10,
            placesLeft = 2,
            price = "Paid - 50 UAH",
            category = "Competitive",
            description = "A structured Swiss-format afternoon for players comfortable with tournament pacing. Clocks are provided; ratings are for club fun only, not official.",
            rules = "Swiss pairing, 15-minute clocks, standard tournament etiquette. Draws by agreement are allowed.",
            bring = "Your own chess clock if you have one, though club clocks are available."
        ),
        BoardEvent(
            id = "team-tournament-sat",
            name = "Team Board Game Tournament",
            game = "Mixed Games",
            day = "Saturday",
            date = "Sep 20",
            time = "5:00 PM",
            duration = "4 hrs",
            difficulty = "Intermediate & Experienced",
            difficultyLevel = 3,
            format = "Team",
            places = 20,
            placesLeft = 8,
            price = "Paid - 100 UAH per team",
            category = "Team",
            description = "Form a team of two to four and rotate through three games for the club trophy. Teams are seeded randomly, so no need to arrive with a full roster of experts.",
            rules = "Teams of 2-4. Three rounds across three different games, points combined for a final leaderboard.",
            bring = "Just your team. Solo sign-ups are placed into a team on the night."
        ),
        BoardEvent(
            id = "codenames-wed",
            name = "Codenames Word Night",
            game = "Codenames",
            day = "Wednesday",
            date = "Sep 17",
            time = "7:30 PM",
            duration = "1.5 hrs",
            difficulty = "Beginner-friendly",
            difficultyLevel = 1,
            format = "Team",
            places = 18,
            placesLeft = 14,
            price = "Free",
            category = "Party",
            description = "Quick-fire teams, one-word clues, and a lot of arguing over what counts as fair. A loud, friendly midweek session that's easy to jump into.",
            rules = "Teams of 4-8, standard Codenames rules, spymasters rotate each round.",
            bring = "Nothing - just show up ready to think in one word."
        ),
        BoardEvent(
            id = "pandemic-mon",
            name = "Pandemic Co-op Night",
            game = "Pandemic",
            day = "Monday",
            date = "Sep 15",
            time = "7:00 PM",
            duration = "2 hrs",
            difficulty = "Intermediate",
            difficultyLevel = 2,
            format = "Team",
            places = 12,
            placesLeft = 9,
            price = "Free",
            category = "Team",
            description = "Work together to contain four outbreaks before the world falls apart. A fully cooperative evening - everyone wins or loses together.",
            rules = "4-player co-op boards, standard difficulty. Roles are dealt randomly.",
            bring = "Nothing required."
        )
    )

    val games = listOf(
        BoardGame("Catan", "Strategy", "3-4", "60-90 min", "Medium", true, "Trade resources and settle territory in the club's most requested strategy game."),
        BoardGame("Ticket to Ride", "Family", "2-5", "45-60 min", "Easy", true, "Collect cards, claim train routes, and connect the map before your rivals do."),
        BoardGame("Chess", "Competitive", "2", "20-90 min", "Hard", true, "The classic, played casually on weeknights and competitively on Sundays."),
        BoardGame("Carcassonne", "Family", "2-5", "35-45 min", "Easy", true, "Lay tiles to build cities, roads, and fields in this calm, tactical favorite."),
        BoardGame("Azul", "Strategy", "2-4", "30-45 min", "Medium", true, "Draft colorful tiles to decorate the walls of a Portuguese palace."),
        BoardGame("Pandemic", "Team", "2-4", "45 min", "Medium", false, "A fully cooperative race to cure four diseases before time runs out."),
        BoardGame("Codenames", "Party", "4-8", "15-30 min", "Easy", true, "Give one-word clues to help your team find agents on the grid - fast and loud."),
        BoardGame("7 Wonders", "Strategy", "3-7", "30-45 min", "Medium", false, "Draft cards to build an ancient civilization over three fast-moving ages.")
    )

    val schedule = listOf(
        ScheduleDay("Monday", "Casual Game Night", "Drop-in play, no sign-up needed for casual tables.", "pandemic-mon"),
        ScheduleDay("Tuesday", "Club closed for setup", "The hall is closed while we prep next week's tournament boards.", null),
        ScheduleDay("Wednesday", "Strategy Games", "Deeper strategy titles come out for players who want a longer session.", "codenames-wed"),
        ScheduleDay("Thursday", "Open Table Practice", "Informal practice for anyone prepping for the weekend tournament.", null),
        ScheduleDay("Friday", "Catan & Card Games", "Our most popular night - Catan boards and a rotating card-game corner.", "catan-fri"),
        ScheduleDay("Saturday", "Tournaments", "Structured tournament play, both solo and team formats.", "team-tournament-sat"),
        ScheduleDay("Sunday", "Beginner Sessions", "Slower-paced tables with hosts on hand to teach the rules.", "chess-sun")
    )

    val faqs = listOf(
        Faq("Do I need an account to register?", "No. Bel9ja Board Club never asks you to create an account. You fill in a short form with your name and contact details each time you want to join a session or the club."),
        Faq("Can beginners participate?", "Yes. Most sessions welcome beginners, and Sunday sessions are built specifically for new players with hosts on hand to teach the rules."),
        Faq("Can I come alone?", "Absolutely. Most individual-format sessions are designed for solo sign-ups, and you'll be seated with a friendly table."),
        Faq("Do I need to bring my own board game?", "No. All boards, pieces, and scorepads are provided by the club for every listed session."),
        Faq("Are there free events?", "Yes. Several weekly sessions are free to attend - look for the Free tag on the Sessions screen."),
        Faq("Can I register a team?", "Yes. Team-format sessions let you register a full team of two to four players on a single form."),
        Faq("How do I cancel my registration?", "Contact the club through the Contact screen with your registration reference number and we'll release your spot."),
        Faq("How many people can attend one session?", "It varies by session - each event listing shows the total number of places and how many remain.")
    )
}
