package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mentalhealthemotion.Data.PSQIVIewModel
import com.example.mentalhealthemotion.Data.UserViewModel

@Composable
fun HistoryScreen(
    psqiViewModel:PSQIVIewModel,
    userViewModel: UserViewModel,
    onNavigate: (String) -> Unit
) {
    val pastResults by psqiViewModel.pastResults.observeAsState(emptyList())
    val user by userViewModel.currentUser.observeAsState()

    LaunchedEffect(user?.userID) {
        user?.userID?.let { userId ->
            psqiViewModel.getAllResults(userId)
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    // Filter results
    val filteredResults = pastResults.filter { result ->
        result.date?.contains(searchQuery, ignoreCase = true) ?: false
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Tests History",
                fontSize = 35.sp,
                color = Color(0xFF2E3E64),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .padding(top = 35.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(
                    text = "Search by Date",
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                ) },
                leadingIcon = {
                   Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .width(300.dp)
                    .padding(vertical = 6.dp)
                    .background(Color.White)
                    .border(
                        width = 1.5.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(20.dp)
                    ),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent
                )
            )


            Spacer(modifier = Modifier.height(20.dp))

            // Test results
           /* LazyColumn {
                itemsIndexed(pastResults) { index, result ->
                    val isLastItem = index == pastResults.lastIndex
                    ResultCard(
                        result.feedback,
                        result.date,
                        result.score,
                        isLastItem = isLastItem
                    )
                }
            }*/
            // If no results match the search, display a message
            if (filteredResults.isEmpty()) {
                Text(
                    text = "No test results found for the entered date.",
                    fontSize = 18.sp,
                    color = Color.LightGray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 40.dp)
                )
            } else {
                // Display filtered results
                LazyColumn {
                    itemsIndexed(filteredResults) { index, result ->
                        val isLastItem = index == filteredResults.lastIndex
                        ResultCard(result.feedback, result.date, result.score, isLastItem)
                    }
                }
            }

        }

        // Bottom Navigation Bar
        BottomNavigationBar(
            onNavigate = onNavigate,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun ResultCard(result: String, date: String?,score: Int, isLastItem: Boolean) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .padding(bottom = if (isLastItem) 90.dp else 0.dp )
            .fillMaxWidth()
            .height(175.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date?: "Unknown Date",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E3E64)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Test Score: $score",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E3E64)
                )
                Text(
                    text = "Test Result: $result",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E3E64),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(15.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(10.dp))
}

