package com.bel9ja.boardclub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bel9ja.boardclub.data.SampleData
import com.bel9ja.boardclub.ui.ClubTab
import com.bel9ja.boardclub.ui.components.ChoiceChip
import com.bel9ja.boardclub.ui.components.InfoRow
import com.bel9ja.boardclub.ui.components.SectionTitle
import com.bel9ja.boardclub.ui.theme.Green
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy900
import com.bel9ja.boardclub.ui.theme.Orange
import com.bel9ja.boardclub.ui.theme.RedSoft
import com.bel9ja.boardclub.ui.theme.Yellow

@Composable
fun ClubScreen(clubTab: ClubTab, onClubTabChange: (ClubTab) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ClubTab.values().toList()) { t ->
                ChoiceChip(label = t.label(), selected = clubTab == t) { onClubTabChange(t) }
            }
        }
        when (clubTab) {
            ClubTab.ABOUT -> AboutPanel()
            ClubTab.JOIN -> JoinPanel()
            ClubTab.FAQ -> FaqPanel()
            ClubTab.CONTACT -> ContactPanel()
        }
    }
}

private fun ClubTab.label(): String = when (this) {
    ClubTab.ABOUT -> "About"
    ClubTab.JOIN -> "Join"
    ClubTab.FAQ -> "FAQ"
    ClubTab.CONTACT -> "Contact"
}

@Composable
private fun AboutPanel() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SectionTitle("About the club")
            Text(
                "Bel9ja Board Club is a real-world tabletop community, not an online game. We run weekly sessions out of our own hall, where members meet in person to learn, play, and compete over board games of every kind.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
        item {
            SectionTitle("Community values")
            BulletList(
                listOf(
                    "Every table is beginner-friendly by default \u2014 no one is turned away for being new.",
                    "Competitive nights are clearly labeled, so casual players always know what to expect.",
                    "No accounts, no pressure to return \u2014 come whenever a session fits your week."
                )
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
        item {
            SectionTitle("Types of activities")
            Text(
                "Casual game nights, structured tournaments, cooperative sessions, and beginner-only tables, spread across the week \u2014 see the Schedule tab for the current rotation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
        item {
            SectionTitle("Club rules")
            BulletList(
                listOf(
                    "Arrive 10 minutes before your session's start time.",
                    "Be patient with new players \u2014 every table was a beginner table once.",
                    "Games and pieces stay at the club; please handle them with care.",
                    "Cancellations should be sent at least 2 hours ahead so a spot can be released."
                )
            )
            Spacer(modifier = Modifier.height(18.dp))
        }
        item {
            SectionTitle("Venue & opening hours")
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoRow(Icons.Default.Place, "Location", "12 Sicheslavska Naberezhna, Dnipro")
                InfoRow(Icons.Default.Schedule, "Opening hours", "Mon\u2013Fri 5:00 PM \u2013 10:00 PM \u00b7 Sat\u2013Sun 2:00 PM \u2013 11:00 PM")
            }
        }
    }
}

@Composable
private fun BulletList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { line ->
            Row {
                Text("\u2022  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun JoinPanel() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var games by remember { mutableStateOf(setOf<String>()) }
    var level by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(setOf<String>()) }
    var message by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var levelError by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    val gameOptions = SampleData.games.map { it.name }
    val dayOptions = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    if (done) {
        SimpleConfirmPanel(
            title = "Your join request has been sent.",
            subtitle = "Someone from the club will reach out by email or phone \u2014 no account was created.",
            buttonLabel = "Send another request"
        ) {
            done = false
            name = ""; email = ""; phone = ""; games = emptySet(); level = ""; days = emptySet(); message = ""
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text(
                "Tell us a little about you and we'll help you find the right table. No account is created.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            LabeledField("Full Name", nameError) { AppTextField(name, { name = it }, "Your full name") }
            LabeledField("Email", emailError) { AppTextField(email, { email = it }, "you@example.com", KeyboardType.Email) }
            LabeledField("Phone Number", phoneError) { AppTextField(phone, { phone = it }, "+380 00 000 0000", KeyboardType.Phone) }

            LabeledField("Preferred Board Games", null) {
                FlowChips(gameOptions, games) { opt ->
                    games = if (games.contains(opt)) games - opt else games + opt
                }
            }
            LabeledField("Experience Level", levelError) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Beginner", "Intermediate", "Experienced").forEach { lv ->
                        ChoiceChip(lv, level == lv) { level = lv }
                    }
                }
            }
            LabeledField("Preferred Days", null) {
                FlowChips(dayOptions, days) { opt ->
                    days = if (days.contains(opt)) days - opt else days + opt
                }
            }
            LabeledField("Optional message", null, hint = "Anything else that helps us seat you well.") {
                AppTextField(message, { message = it }, "I usually play with two friends and prefer evenings...", singleLine = false)
            }

            Button(
                onClick = {
                    var ok = true
                    nameError = if (name.isBlank()) "Enter your full name.".also { ok = false } else null
                    emailError = if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) "Enter a valid email address.".also { ok = false } else null
                    phoneError = if (phone.filter { it.isDigit() }.length < 7) "Enter a valid phone number.".also { ok = false } else null
                    levelError = if (level.isBlank()) "Choose your experience level.".also { ok = false } else null
                    if (ok) done = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF16223F))
            ) {
                Text("Send Join Request", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FaqPanel() {
    var openIndex by remember { mutableStateOf(-1) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Yellow.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Yellow, modifier = Modifier.height(18.dp))
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Text("No account is ever required to use Bel9ja Board Club.", color = Yellow, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        itemsIndexed(SampleData.faqs) { i, faq ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .background(Navy900, RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openIndex = if (openIndex == i) -1 else i }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(faq.question, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, modifier = Modifier.padding(end = 10.dp))
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (openIndex == i) {
                    Text(
                        faq.answer,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(14.dp, 0.dp, 14.dp, 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactPanel() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var subjectError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    if (done) {
        SimpleConfirmPanel(
            title = "Your message has been sent.",
            subtitle = "We usually reply within one business day.",
            buttonLabel = "Send another message"
        ) {
            done = false
            name = ""; email = ""; subject = ""; message = ""
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            SectionTitle("Get in touch")
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoRow(Icons.Default.Mail, "Email", "hello@bel9jaboardclub.com")
                InfoRow(Icons.Default.Phone, "Phone", "+380 50 123 4567")
                InfoRow(Icons.Default.Schedule, "Opening hours", "Mon\u2013Fri 5\u201310 PM \u00b7 Sat\u2013Sun 2\u201311 PM")
                InfoRow(Icons.Default.LocationOn, "Location", "12 Sicheslavska Naberezhna, Dnipro")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Navy900, RoundedCornerShape(14.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Map placeholder \u2014 club location", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle("Send a message")
            LabeledField("Name", nameError) { AppTextField(name, { name = it }, "Your name") }
            LabeledField("Email", emailError) { AppTextField(email, { email = it }, "you@example.com", KeyboardType.Email) }
            LabeledField("Subject", subjectError) { AppTextField(subject, { subject = it }, "What's this about?") }
            LabeledField("Message", messageError) { AppTextField(message, { message = it }, "Write your message...", singleLine = false) }

            Button(
                onClick = {
                    var ok = true
                    nameError = if (name.isBlank()) "Enter your name.".also { ok = false } else null
                    emailError = if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim())) "Enter a valid email address.".also { ok = false } else null
                    subjectError = if (subject.isBlank()) "Add a short subject.".also { ok = false } else null
                    messageError = if (message.isBlank()) "Write a message.".also { ok = false } else null
                    if (ok) done = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF16223F))
            ) {
                Text("Send Message", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SimpleConfirmPanel(title: String, subtitle: String, buttonLabel: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp, 40.dp, 20.dp, 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .background(Green.copy(alpha = 0.18f), androidx.compose.foundation.shape.CircleShape)
                .padding(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Green)
        }
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(6.dp))
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onReset,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Navy900, contentColor = MaterialTheme.colorScheme.onBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, Navy700)
        ) {
            Text(buttonLabel, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { opt ->
            ChoiceChip(opt, selected.contains(opt)) { onToggle(opt) }
        }
    }
}
