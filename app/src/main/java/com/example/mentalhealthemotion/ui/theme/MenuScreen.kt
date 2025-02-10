package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mentalhealthemotion.R


data class MenuItem(val title: String, val route: String? = null, val icon: Int? = null)

val menuItems = listOf(
    MenuItem("Home Page", MentalHeathAppScreen.HomePage.name),
    MenuItem("Profile Page", MentalHeathAppScreen.ProfilePage.name),
    MenuItem("Sleep Tests History", MentalHeathAppScreen.HistoryPage.name),
)

val menuItemModifier = Modifier
    .fillMaxWidth()
    .border(2.dp, Color(0xFFE5E4E2))
    .padding(4.dp)
    .background(Color.Transparent)

@Composable
fun MenuScreen(
    onMenuItemClick: (String) -> Unit,
    onSignOutClick: () -> Unit,
    navController: NavHostController
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        items(menuItems) { item ->
            Surface(
                modifier = menuItemModifier.clickable {
                    item.route?.let { route ->
                        navController.navigate(route)
                    } ?: onMenuItemClick(item.title)
                }
            ) {
                ListItem(
                    headlineContent = { Text(item.title, fontSize = 18.sp, fontWeight = FontWeight.Medium) },
                    leadingContent = {
                        item.icon?.let {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            }
        }
        item {
            SignOutItem { onSignOutClick() }
        }
    }
}

@Composable
fun SignOutItem(onSignOutClick: () -> Unit) {
    Surface(
        modifier = menuItemModifier.clickable { onSignOutClick() }
    ) {
        ListItem(
            headlineContent = { Text("Sign Out", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.signout),
                    contentDescription = "Sign out icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        )
    }
}


