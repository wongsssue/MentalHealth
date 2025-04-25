package com.example.mentalhealthemotion.ui.theme

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mentalhealthemotion.Data.EmergencyContact
import com.example.mentalhealthemotion.Data.EmergencyContactViewModel

@SuppressLint("Range")
@Composable
fun EmergencyScreen(
    viewModel: EmergencyContactViewModel
) {
    val emergencyContacts by viewModel.emergencyContacts.observeAsState(emptyList())
    val context = LocalContext.current

    // Register ActivityResultLauncher to pick a contact
    val pickContactLauncher: ActivityResultLauncher<Intent> =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { contactUri ->
                    val projection = arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    )

                    val cursor = context.contentResolver.query(contactUri, projection, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                            val phoneNumber = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            // Save the contact in ViewModel
                            viewModel.saveContact(name, phoneNumber)
                        }
                    }
                }
            }
        }

    // Handle permissions and pick a contact
    fun checkPermissionsAndPickContact() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
            pickContactLauncher.launch(intent)
        } else {
            // Request permission here
            ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        }
    }

    // Function to send a distress message
    fun sendDistressMessage(contact: EmergencyContact) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(contact.phoneNumber, null, "I need help! Please assist me.", null, null)
        } else {
            Toast.makeText(context, "Permission to send SMS not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to make an emergency call
    fun makeEmergencyCall(contact: EmergencyContact) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:${contact.phoneNumber}")
            context.startActivity(callIntent)
        } else {
            Toast.makeText(context, "Permission to make calls not granted", Toast.LENGTH_SHORT).show()
        }
    }

    // Call the delete function from the ViewModel when the user presses the delete button
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Add Emergency Contact", style = MaterialTheme.typography.h6)

        EmergencyContactForm { name, phoneNumber ->
            viewModel.saveContact(name, phoneNumber)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { checkPermissionsAndPickContact() },
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFADD8E6))
        ) {
            Text("Pick Contact from Contacts")
        }


        Spacer(modifier = Modifier.height(16.dp))

        EmergencyContactsList(
            emergencyContacts = emergencyContacts,
            onSendDistressMessage = { contact -> sendDistressMessage(contact) },
            onMakeEmergencyCall = { contact -> makeEmergencyCall(contact) },
            onDeleteContact = { contact -> viewModel.deleteContact(contact) } // Pass delete function here
        )
    }
}
