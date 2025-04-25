package com.example.mentalhealthemotion.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.Reminder
import com.example.mentalhealthemotion.Data.ReminderDatabase
import com.example.mentalhealthemotion.Data.ReminderRepository
import com.example.mentalhealthemotion.Data.ReminderViewModel
import com.example.mentalhealthemotion.Data.ReminderViewModelFactory
import com.example.mentalhealthemotion.Data.scheduleReminderNotification
import java.util.Calendar
import java.util.Date

import java.text.SimpleDateFormat
import java.util.Locale


val LightBlue = Color(0xFF145DA0)  // Light Blue color

@Composable
fun ReminderScreen(navController: NavController) {
    val reminderDao = ReminderDatabase.getDatabase(LocalContext.current).reminderDao()
    val reminderRepository = ReminderRepository(reminderDao)
    val reminderViewModel: ReminderViewModel = viewModel(factory = ReminderViewModelFactory(reminderRepository))
    val reminders by reminderViewModel.reminders.observeAsState(emptyList())
    var isAscending by remember { mutableStateOf(true) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf(System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    val calendar = Calendar.getInstance()
    val selectedDate = calendar.time
    val current = LocalContext.current

    val timeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    val formattedTime = timeFormat.format(time)

    LaunchedEffect(reminders) {
        val reminder = reminders.lastOrNull()
        reminder?.let {
            scheduleReminderNotification(current, it)
        }
    }

    LaunchedEffect(reminders) {
        val currentTime = System.currentTimeMillis()
        reminders.forEach { reminder ->
            if (reminder.time <= currentTime) {
                reminderToDelete = reminder
            }
        }
    }

    reminderToDelete?.let { reminder ->
        showDeleteReminderDialog(reminder) {
            reminderViewModel.deleteReminder(reminder)
            reminderToDelete = null
        }
    }

    MaterialTheme(
        colors = lightColors(
            primary = LightBlue,
            primaryVariant = LightBlue,
            secondary = LightBlue,
            background = Color.White,
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF4A90E2), Color(0xFF145DA0))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            androidx.compose.material3.Text(
                text = "Self-Care Reminders",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Spacer(modifier = Modifier.height(70.dp))

            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Reminder Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.surface)
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.textFieldColors(backgroundColor = MaterialTheme.colors.surface)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Select Date")
            }

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Select Time")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selected Date & Time: $formattedTime",
                style = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.secondary),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        val reminder = Reminder(title = title, description = description, time = time)
                        reminderViewModel.addReminder(reminder)

                        // Clear input fields
                        title = ""
                        description = ""
                    }
                },
                modifier = Modifier.align(Alignment.End),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(text = "Add Reminder")
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(reminders) { reminder ->
                    ReminderItem(reminder, onDelete = { reminderViewModel.deleteReminder(reminder) })
                }
            }
        }

        // Date Picker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    time = calendar.timeInMillis
                    showDatePicker = false
                },
                initialDate = selectedDate
            )
        }

        // Time Picker Dialog
        if (showTimePicker) {
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },
                onTimeSelected = { hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    time = calendar.timeInMillis
                    showTimePicker = false
                },
                initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = calendar.get(Calendar.MINUTE)
            )
        }
    }
}

// Keep the rest of the code as it is, no changes are needed for the dialog or other components.


@Composable
fun showDeleteReminderDialog(reminder: Reminder, onDelete: () -> Unit) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Reminder expired") },
            text = { Text("Would you like to delete this reminder?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDialog = false
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun ReminderItem(reminder: Reminder, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) // Format for date and time
    val formattedTime = sdf.format(Date(reminder.time))

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = reminder.title, style = MaterialTheme.typography.h6)
            Text(text = reminder.description, style = MaterialTheme.typography.body2)
            Text(text = "Reminder Time: $formattedTime", style = MaterialTheme.typography.body2.copy(color = Color.Gray))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                Text("Delete Reminder")
            }
        }
    }
}



@SuppressLint("RememberReturnType")
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Int, Int, Int) -> Unit,
    initialDate: Date
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { time = initialDate }

    val datePickerDialog = remember {
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> onDateSelected(year, month, dayOfMonth) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Delay dialog display until composable is fully laid out
    LaunchedEffect(Unit) {
        datePickerDialog.show()
    }

    // Call dismiss when the dialog is dismissed
    LaunchedEffect(datePickerDialog) {
        onDismissRequest()
    }
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
    initialHour: Int,
    initialMinute: Int
) {
    val context = LocalContext.current

    val timePickerDialog = remember {
        android.app.TimePickerDialog(
            context,
            { _, hourOfDay, minute -> onTimeSelected(hourOfDay, minute) },
            initialHour,
            initialMinute,
            true
        )
    }

    LaunchedEffect(Unit) {
        timePickerDialog.show()
    }

    LaunchedEffect(timePickerDialog) {
        onDismissRequest()
    }
}

