package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mentalhealthemotion.R

data class AdminOption(val title: String, val routeName: String)

@Composable
fun AdminHomePage(navController: NavController, onSignOutClick: () -> Unit) {
    val options = listOf(
        AdminOption("Educational Library", MentalHeathAppScreen.AdminManageEduPage.name),
        AdminOption("User Account", MentalHeathAppScreen.AdminManageUserPage.name)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .height(65.dp)
                .fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.signout),
                contentDescription = "Sign out icon",
                modifier = Modifier
                    .size(45.dp)
                    .clickable { onSignOutClick() }
            )
        }

        Text(
            text = "Admin Management",
            fontSize = 30.sp,
            color = Color(0xFF2E3E64),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 30.dp)
        )
        options.forEach { option ->
            val backgroundColor = if (options.indexOf(option) % 2 == 0) Color(0xFFBEE4F4) else Color(0xFFA7C7E7)
            AdminOptionComponent(title = option.title, backgroundColor = backgroundColor) {
                navController.navigate(option.routeName)
            }
        }
    }
}

@Composable
fun AdminOptionComponent(
    title: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}
