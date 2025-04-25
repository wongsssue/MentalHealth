package com.example.mentalhealthemotion.ui.theme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.CommunityViewModel
import com.example.mentalhealthemotion.Data.MoodEntryViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun AddCommunityScreen(
    communityViewModel: CommunityViewModel,
    userViewModel: UserViewModel,
    onNavigate: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current
    val user by userViewModel.currentUser.observeAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            communityViewModel.loadCommunityEntries(userId) // Load data when page opens
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Add Community", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF388E3C))
                .fillMaxWidth()
                .height(56.dp)
                .clickable {
                    if (groupName.isBlank() || description.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
                        return@clickable
                    }

                    user?.userID?.toString()?.toIntOrNull()?.let { userId ->
                        communityViewModel.saveCommunity(userId, groupName, description) {
                            onNavigate() // Navigate back after saving
                        }
                    } ?: Toast.makeText(context, "Invalid User ID!", Toast.LENGTH_SHORT).show()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SUBMIT",
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}
