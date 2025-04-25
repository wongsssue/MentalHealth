package com.example.mentalhealthemotion.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.Community
import com.example.mentalhealthemotion.Data.CommunityViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AdminManageCommunityPage(communityViewModel: CommunityViewModel) {
    val allCommunities by communityViewModel.allCommunities.observeAsState(emptyList())

    // Load communities on page load
    LaunchedEffect(Unit) {
        communityViewModel.loadAllCommunities()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .background(Color(0xFFF5F5F5)) // Light grey background
    ) {
        Text(
            "Manage Communities",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333) // Dark text color for better readability
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (allCommunities.isEmpty()) {
            Text("No communities available.", color = Color(0xFF666666)) // Lighter text color for empty state
        } else {
            LazyColumn {
                items(allCommunities) { community ->
                    CommunityItem(community, communityViewModel)
                }
            }
        }
    }
}

@Composable
fun CommunityItem(community: Community, viewModel: CommunityViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Community: ${community.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2C3E50) // Darker text for title
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Description: ${community.description}",
                color = Color(0xFF7F8C8D) // Greyish text for description
            )
            Text(
                "Creator: ${community.creatorId}",
                color = Color(0xFF7F8C8D)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display members
            Text(
                "Members: ${community.members.joinToString()}",
                color = Color(0xFF7F8C8D)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Delete Community Button
            Button(
                onClick = {
                    viewModel.deleteCommunity(community.id)
                    Log.d("AdminPage", "Community deleted successfully.")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE74C3C), // Red color for delete
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp) // Rounded corners for button
            ) {
                Text("Delete Community")
            }

            // Remove Member Button for each member
            community.members.forEach { memberId ->
                Button(
                    onClick = {
                        viewModel.removeMemberFromCommunity(community.id, memberId)
                        Log.d("AdminPage", "Member removed successfully.")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3498DB), // Blue color for remove member
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Remove $memberId")
                }
            }
        }
    }
}
