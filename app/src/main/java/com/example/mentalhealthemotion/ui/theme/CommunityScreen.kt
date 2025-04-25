package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mentalhealthemotion.Data.MoodEntryViewModel
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.R
import com.example.mentalhealthemotion.Data.BottomNavItem
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(navController: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Community",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                CircularButton(
                    iconRes = R.drawable.add,
                    label = "Add",
                    backgroundColor = Color(0xFF9ACDFF),
                    onClick = { navController.navigate("add_community") } // Navigate to Add Community
                )
                CircularButton(
                    iconRes = R.drawable.delete,
                    label = "Delete",
                    backgroundColor = Color(0xFFFFA07A),
                    onClick = {navController.navigate("delete_community") }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                CircularButton(
                    iconRes = R.drawable.view,
                    label = "View",
                    backgroundColor = Color(0xFFD3D3D3),
                    onClick = {  navController.navigate("view_community")}
                )
                CircularButton(
                    iconRes = R.drawable.edit,
                    label = "Edit",
                    backgroundColor = Color(0xFFB0E0A8),
                    onClick = {
                        navController.navigate("edit_community")
                    }
                )

            }
        }
    }
}


@Composable
fun CircularButton(iconRes: Int, label: String, backgroundColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() } // Makes entire component clickable
    ) {
        Box(
            modifier = Modifier
                .size(110.dp) // Outer circle size
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp) // Inner white circle
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    tint = Color.Unspecified, // Keeps original image colors
                    modifier = Modifier.size(70.dp) // Increased icon size
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp)) // Space between button and text
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
