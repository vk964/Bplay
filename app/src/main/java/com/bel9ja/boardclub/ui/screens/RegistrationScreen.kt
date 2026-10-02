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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.bel9ja.boardclub.data.BoardEvent
import com.bel9ja.boardclub.ui.components.ChoiceChip
import com.bel9ja.boardclub.ui.components.ConfirmRow
import com.bel9ja.boardclub.ui.theme.Green
import com.bel9ja.boardclub.ui.theme.Navy700
import com.bel9ja.boardclub.ui.theme.Navy800
import com.bel9ja.boardclub.ui.theme.Navy900
import com.bel9ja.boardclub.ui.theme.Orange
import com.bel9ja.boardclub.ui.theme.RedSoft
import com.bel9ja.boardclub.ui.theme.Yellow

data class RegistrationResult(val event: BoardEvent, val participants: String, val reference: String)

fun generateReference(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    val code = (1..6).map { chars.random() }.joinToString("")
    return "BJ-$code"
}

private fun isValidEmail(v: String) = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(v.trim())
private fun isValidPhone(v: String) = v.filter { it.isDigit() }.length >= 7

@Composable
fun RegistrationScreen(event: BoardEvent, onSubmitted: (RegistrationResult) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("1") }
    var level by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var participantsError by remember { mutableStateOf<String?>(null) }
    var levelError by remember { mutableStateOf<String?>(null) }
    var agreeError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        var ok = true
        nameError = if (name.isBlank()) "Enter your full name.".also { ok = false } else null
        emailError = if (!isValidEmail(email)) "Enter a valid email address.".also { ok = false } else null
        phoneError = if (!isValidPhone(phone)) "Enter a valid phone number.".also { ok = false } else null
        val n = participants.toIntOrNull()
        participantsError = if (n == null || n < 1 || n > event.places)
            "Enter a number between 1 and ${event.places}.".also { ok = false } else null
        levelError = if (level.isBlank()) "Choose an experience level.".also { ok = false } else null
        agreeError = if (!agree) "You must agree to the participation rules.".also { ok = false } else null

        if (ok) {
            onSubmitted(RegistrationResult(event, participants, generateReference()))
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Column(
                modifier = Modifier
                    .padding(20.dp, 14.dp, 20.dp, 6.dp)
                    .fillMaxWidth()
                    .background(Navy800, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("Registering for", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(event.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Text("${event.day}, ${event.date} \u00b7 ${event.time}", color = Yellow, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }
        }

        item {
            Column(modifier = Modifier.padding(20.dp, 10.dp, 20.dp, 0.dp)) {
                LabeledField("Full Name", nameError) {
                    AppTextField(name, { name = it }, "Your full name")
                }
                LabeledField("Email", emailError) {
                    AppTextField(email, { email = it }, "you@example.com", KeyboardType.Email)
                }
                LabeledField("Phone Number", phoneError) {
                    AppTextField(phone, { phone = it }, "+380 00 000 0000", KeyboardType.Phone)
                }
                LabeledField("Selected Event", null) {
                    AppTextField(event.name, {}, "", enabled = false)
                }
                LabeledField("Number of Participants", participantsError, hint = "Up to ${event.places} for this session") {
                    AppTextField(participants, { participants = it }, "1", KeyboardType.Number)
                }
                LabeledField("Experience Level", levelError) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Beginner", "Intermediate", "Experienced").forEach { lv ->
                            ChoiceChip(lv, level == lv) { level = lv }
                        }
                    }
                }
                LabeledField("Optional message", null, hint = "Dietary notes, accessibility needs, or anything else.") {
                    AppTextField(message, { message = it }, "Anything we should know...", singleLine = false)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Checkbox(
                        checked = agree,
                        onCheckedChange = { agree = it },
                        colors = CheckboxDefaults.colors(checkedColor = Orange, uncheckedColor = Navy700)
                    )
                    Text("I agree to the club participation rules.", color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp)
                }
                agreeError?.let {
                    Text(it, color = RedSoft, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { validateAndSubmit() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF16223F))
                ) {
                    Text("Submit Registration", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RegistrationConfirmScreen(result: RegistrationResult, onBackToHome: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Green.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Green, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your request has been submitted successfully.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We'll text or email you if anything changes about the session.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Navy900, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConfirmRow("Event", result.event.name)
            ConfirmRow("Date", "${result.event.day}, ${result.event.date}")
            ConfirmRow("Participants", result.participants)
            ConfirmRow("Reference number", result.reference, strong = true)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "No account was created \u2014 keep your reference number for any questions about this booking.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.5.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = Color(0xFF16223F))
        ) {
            Text("Back to Home", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LabeledField(
    label: String,
    error: String?,
    hint: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        content()
        if (error != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(error, color = RedSoft, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
        } else if (hint != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(hint, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.5.sp)
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF5B6994)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Navy900,
            unfocusedContainerColor = Navy900,
            disabledContainerColor = Navy900,
            focusedBorderColor = Orange,
            unfocusedBorderColor = Navy700,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}
