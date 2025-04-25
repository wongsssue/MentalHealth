package com.example.mentalhealthemotion.ui.theme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.Community
import com.example.mentalhealthemotion.Data.CommunityViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ViewCommunityPage(
    userViewModel: UserViewModel,
    communityViewModel: CommunityViewModel,
    navController: NavController
) {
    LaunchedEffect(Unit) {
        communityViewModel.fetchAllCommunities()
    }
    val user by userViewModel.currentUser.observeAsState()

    val scrollState = rememberScrollState()

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            communityViewModel.loadCommunityEntries(userId) // Load data when page opens
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = All, 1 = Joined

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(
            selectedTabIndex = selectedTab,
            backgroundColor = Color.LightGray
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("All Communities") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Joined Communities") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> ViewAllCommunities(userViewModel, communityViewModel, navController)
            1 -> {
                LaunchedEffect(Unit) {
                    userViewModel.currentUser.value?.userID?.let {
                        communityViewModel.fetchJoinedCommunities(it.toString())
                    }
                }
                ViewJoinedCommunities(userViewModel, communityViewModel, navController)
            }
        }

    }
}

@Composable
fun ViewAllCommunities(
    userViewModel: UserViewModel,
    communityViewModel: CommunityViewModel,
    navController: NavController
) {
    val allCommunities by communityViewModel.allCommunities.observeAsState(emptyList())
    val joinedCommunities by communityViewModel.joinedCommunities.observeAsState(emptyList())
    val user by userViewModel.currentUser.observeAsState()

    // 🔥 Filter out joined communities
    val filteredCommunities = allCommunities.filter { community ->
        !joinedCommunities.any { it.id == community.id }
    }

    LazyColumn {
        items(filteredCommunities) { community ->
            CommunityItem(community, user?.userID.toString(), navController, communityViewModel)
        }
    }
}


@Composable
fun ViewJoinedCommunities(
    userViewModel: UserViewModel,
    communityViewModel: CommunityViewModel,
    navController: NavController
) {
    val joinedCommunities by communityViewModel.joinedCommunities.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        userViewModel.currentUser.value?.userID?.let {
            communityViewModel.fetchJoinedCommunities(it.toString())
        }
    }

    if (joinedCommunities.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("You haven’t joined any communities yet!", fontSize = 18.sp)
        }
    } else {
        LazyColumn {
            items(joinedCommunities) { community ->
                CommunityItem(community,
                    userViewModel.currentUser.value?.userID.toString(), navController, communityViewModel)
            }
        }
    }
}


@Composable
fun CommunityItem(
    community: Community,
    userId: String?,
    navController: NavController,
    communityViewModel: CommunityViewModel
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { isExpanded = !isExpanded },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = community.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = community.description, fontSize = 14.sp, color = Color.Gray)

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                if (userId != null && !community.members.contains(userId)) {
                    Button(
                        onClick = { communityViewModel.joinCommunity(userId, community.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Join")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Join")
                    }
                } else {
                    Button(
                        onClick = {
                            if (community.id.isNotEmpty()) {
                                navController.navigate("chat/${community.id}/$userId")
                            } else {
                                Log.e("Navigation", "Community ID is empty")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Chat")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat", color = Color.White)
                    }

                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { shareCommunityLink(context, community.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share")
            }
        }
    }
}



fun shareCommunityLink(context: Context, communityId: String) {
    val shareLink = "https://MentalHealthApp/community/$communityId"
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Community Link", shareLink)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Join this community: $shareLink")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Community Link"))
}
