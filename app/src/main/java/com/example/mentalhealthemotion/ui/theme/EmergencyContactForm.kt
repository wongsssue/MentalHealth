package com.example.mentalhealthemotion.ui.theme

import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.mentalhealthemotion.Data.EmergencyContact

@Composable
fun EmergencyContactForm(
    onSaveContact: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Contact Name") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Button(
            onClick = {
                // Save the contact
                onSaveContact(name, phoneNumber)

                // Clear the text fields after saving the contact
                name = ""
                phoneNumber = ""
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Save Contact")
        }
    }
}


@Composable
fun EmergencyContactsList(
    emergencyContacts: List<EmergencyContact>,
    onSendDistressMessage: (EmergencyContact) -> Unit,
    onMakeEmergencyCall: (EmergencyContact) -> Unit,
    onDeleteContact: (EmergencyContact) -> Unit // Add the delete callback
) {
    LazyColumn {
        items(emergencyContacts) { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colors.surface)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { onMakeEmergencyCall(contact) }) {
                    Icon(Icons.Default.Call, contentDescription = "Call")
                }
                IconButton(onClick = { onSendDistressMessage(contact) }) {
                    Icon(Icons.Default.Message, contentDescription = "Send Message")
                }
                // Add a delete button
                IconButton(onClick = { onDeleteContact(contact) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Contact")
                }
            }
        }
    }
}


@Composable
fun EmergencyContactItem(
    contact: EmergencyContact,
    onSendMessage: () -> Unit,
    onCall: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = contact.name, style = MaterialTheme.typography.h6)
            Text(text = contact.phoneNumber)
        }

        Column {
            IconButton(onClick = onSendMessage) {
                Icon(Icons.Default.Message, contentDescription = "Send Distress Message")
            }
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Call, contentDescription = "Call")
            }
        }
    }
}
