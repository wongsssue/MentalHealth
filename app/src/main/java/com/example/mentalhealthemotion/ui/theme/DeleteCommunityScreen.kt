package com.example.mentalhealthemotion.ui.theme

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.Community
import com.example.mentalhealthemotion.Data.CommunityViewModel
import com.example.mentalhealthemotion.Data.User
import com.example.mentalhealthemotion.Data.UserRepository
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.Data.UserViewModelFactory

@Composable
fun DeleteCommunityScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    // Observing the current user and all communities
    val user by userViewModel.currentUser.observeAsState()
    val allCommunities by communityViewModel.allCommunities.observeAsState(emptyList())
    val context = LocalContext.current

    // Fetching communities once the user is available
    LaunchedEffect(user?.userID) {
        user?.userID?.let { nonNullUserId ->
            println("📢 Fetching communities for User ID: $nonNullUserId")
            communityViewModel.refreshCommunities(nonNullUserId.toString())
        }
    }

    // Filtering communities to show only those that can be deleted by the user
    val deletableCommunities = remember(allCommunities, user?.userID) {
        allCommunities.filter { community ->
            val userIdString = user?.userID?.toString()
            val creatorId = community.members.getOrNull(0) // Assuming the first member is the creator

            // Check if the current user is the creator of the community
            creatorId == userIdString
        }
    }

    // Composing the UI
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Manage Your Communities",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Show communities that the user can manage
        if (deletableCommunities.isNotEmpty()) {
            LazyColumn {
                items(deletableCommunities) { community ->
                    DeleteCommunityItem(
                        community = community,
                        onDeleteCommunity = {
                            // Delete the community and show a Toast
                            communityViewModel.deleteCommunity(community.id)
                            Toast.makeText(context, "Community deleted!", Toast.LENGTH_SHORT).show()
                        },
                        onRemoveMember = { memberId ->
                            // Update community to remove member
                            val updatedMembers = community.members.toMutableList().apply { remove(memberId) }
                            val updatedCommunity = community.copy(members = updatedMembers)
                            communityViewModel.updateCommunity(updatedCommunity)
                            Toast.makeText(context, "Member removed!", Toast.LENGTH_SHORT).show()
                        },
                        userRepository = userViewModel.userRepository
                    )
                }
            }
        } else {
            Text(
                text = "You have no communities to manage.",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun DeleteCommunityItem(
    community: Community,
    onDeleteCommunity: () -> Unit,
    onRemoveMember: (String) -> Unit,
    userRepository: UserRepository // Pass the UserRepository down here
) {
    var showDeleteCommunityDialog by remember { mutableStateOf(false) }
    var showRemoveMemberDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Community: ${community.name}", fontWeight = FontWeight.Bold)
            Text(text = "Members: ${community.members.size}", fontWeight = FontWeight.Light)

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { showDeleteCommunityDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Delete Community", color = Color.White)
            }

            Button(
                onClick = { showRemoveMemberDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove a Member")
            }
        }
    }

    if (showDeleteCommunityDialog) {
        ConfirmDialog(
            title = "Delete Community",
            message = "Are you sure you want to delete this community?",
            onConfirm = {
                onDeleteCommunity()
                showDeleteCommunityDialog = false
            },
            onDismiss = { showDeleteCommunityDialog = false }
        )
    }

    if (showRemoveMemberDialog) {
        RemoveMemberDialog(
            community = community,
            onRemoveMember = { memberId ->
                onRemoveMember(memberId)
                showRemoveMemberDialog = false
            },
            onDismiss = { showRemoveMemberDialog = false },
            userRepository = userRepository // Pass the userRepository here
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RemoveMemberDialog(
    community: Community,
    onRemoveMember: (String) -> Unit,
    onDismiss: () -> Unit,
    userRepository: UserRepository // Now properly passed here
) {
    val userViewModel: UserViewModel = viewModel(factory = UserViewModelFactory(userRepository))  // Use custom factory

    // Track members and the selected member
    var members by remember { mutableStateOf<List<User>>(emptyList()) }
    var selectedMember by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current

    // Use LaunchedEffect to load members once the community changes
    LaunchedEffect(community.members) {
        val memberList = mutableListOf<User>()
        community.members.drop(1).forEach { memberId -> // Exclude creator
            userViewModel.getUserById(memberId) { user ->
                user?.let {
                    memberList.add(it)
                    members = memberList.toList() // Update members list
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Remove Member") },
        text = {
            Column {
                Text("Select a member to remove:")
                members.forEach { member ->
                    TextButton(onClick = { selectedMember = member }) {
                        Text("${member.userName} (${member.userID})")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedMember?.let {
                        onRemoveMember(it.userID.toString())
                        Toast.makeText(context, "${it.userName} removed!", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                },
                enabled = selectedMember != null
            ) {
                Text("Remove")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
