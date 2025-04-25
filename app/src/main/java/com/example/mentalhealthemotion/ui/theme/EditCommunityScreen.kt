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
import com.example.mentalhealthemotion.Data.UserViewModel

@Composable
fun EditCommunityScreen(
    navController: NavController,
    communityViewModel: CommunityViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val user by userViewModel.currentUser.observeAsState()
    val allCommunities by communityViewModel.allCommunities.observeAsState(emptyList())
    val context = LocalContext.current


    LaunchedEffect(user?.userID) {
        user?.userID?.let { nonNullUserId ->
            println("📢 Fetching communities for User ID: $nonNullUserId")
            communityViewModel.refreshCommunities(nonNullUserId.toString())
        }
    }


    val userCommunities = remember(allCommunities, user?.userID) {
        allCommunities.filter { community ->
            println("🔍 Checking Community: ${community.id}")
            println("➡ Members List: ${community.members}")

            val userIdString = user?.userID?.toString()
            val firstMember = community.members.getOrNull(0) // Avoid IndexOutOfBoundsException

            println("🔍 First Member: $firstMember | User ID: $userIdString")

            val isEditable = firstMember == userIdString
            println("✅ Is Editable: $isEditable for User ID: $userIdString")

            isEditable
        }
    }




    // Debugging community list
    LaunchedEffect(allCommunities) {
        println("✅ Updated All Communities Count: ${allCommunities.size}")
        println("✅ Editable Communities: ${userCommunities.map { it.id }}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Edit Communities", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (userCommunities.isNotEmpty()) {
            LazyColumn {
                items(userCommunities) { community ->
                    EditableCommunityItem(
                        community = community,
                        onSave = { updatedName, updatedDescription ->
                            val updatedCommunity = community.copy(
                                name = updatedName,
                                description = updatedDescription
                            )
                            communityViewModel.updateCommunity(updatedCommunity)
                            Toast.makeText(context, "Community updated!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        } else {
            Text(
                text = "You have no communities to edit.",
                color = Color.Red,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}


@Composable
fun EditableCommunityItem(community: Community, onSave: (String, String) -> Unit) {
    var updatedName by remember { mutableStateOf(community.name) }
    var updatedDescription by remember { mutableStateOf(community.description) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray),
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Community : ${community.name}", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = updatedName,
                onValueChange = { updatedName = it },
                label = { Text("Community Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = updatedDescription,
                onValueChange = { updatedDescription = it },
                label = { Text("Community Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onSave(updatedName, updatedDescription) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}
