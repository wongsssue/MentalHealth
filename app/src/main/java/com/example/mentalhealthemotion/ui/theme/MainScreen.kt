package com.example.mentalhealthemotion.ui.theme

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.mentalhealthemotion.Data.AppDatabase
import com.example.mentalhealthemotion.Data.BottomNavItem
import com.example.mentalhealthemotion.Data.ChatRepository
import com.example.mentalhealthemotion.Data.ChatViewModel
import com.example.mentalhealthemotion.Data.ChatViewModelFactory
import com.example.mentalhealthemotion.Data.ChatbotViewModel
import com.example.mentalhealthemotion.Data.CommunityRepository
import com.example.mentalhealthemotion.Data.CommunityViewModel
import com.example.mentalhealthemotion.Data.CommunityViewModelFactory
import com.example.mentalhealthemotion.Data.EmergencyContactViewModel
import com.example.mentalhealthemotion.Data.EmergencyContactViewModelFactory
import com.example.mentalhealthemotion.Data.MentalHealthViewModel
import com.example.mentalhealthemotion.Data.QuizViewModel
import com.example.mentalhealthemotion.Data.ReminderViewModel
import com.example.mentalhealthemotion.Data.UserRepository
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.Data.UserViewModelFactory
import com.example.mentalhealthemotion.Data.WordSearchRepository
import com.example.mentalhealthemotion.R
import com.google.firebase.firestore.FirebaseFirestore


@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("RememberReturnType")
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val repository = CommunityRepository()
    val communityViewModel: CommunityViewModel = ViewModelProvider(
        LocalContext.current as ViewModelStoreOwner,
        CommunityViewModelFactory(repository)
    ).get(CommunityViewModel::class.java)

    val userViewModel: UserViewModel = ViewModelProvider(
        LocalContext.current as ViewModelStoreOwner
    ).get(UserViewModel::class.java)

    val mentalHealthViewModel = MentalHealthViewModel()
    val quizViewModel = QuizViewModel()
    val emergencyViewModel: EmergencyContactViewModel = viewModel(
        factory = EmergencyContactViewModelFactory(LocalContext.current.applicationContext as Application)
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("assessment", "community", "games","reminder_screen")) {
                BottomNavigationBar(navController)
            }
        },
        floatingActionButton = {
            if (currentRoute in listOf("assessment", "community", "games","reminder_screen")) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 3.dp, vertical = 8.dp) // Add padding to the Box
                ){
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ChatbotFAB(navController = navController, userId = "someUserId")
                        Spacer(modifier = Modifier.height(16.dp))
                        MindMapFAB(navController)
                        Spacer(modifier = Modifier.height(16.dp))
                        EmergencyFAB(navController)
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End // FABs on the end side (right)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "community",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("community") { CommunityScreen(navController) }
            composable("assessment") { QuizSelectionScreen(navController) }
            composable("games") { GamesScreen(navController) }
            composable("reminder_screen") { ReminderScreen(navController) }
            composable("youtube_screen") {MotivationScreen(apiKey = "AIzaSyA1plfJcxoT-FK5UelBzQn_Zdve6meWeq4") }


            // ✅ Add Community Screen
            composable("add_community") {
                AddCommunityScreen(
                    communityViewModel = communityViewModel,
                    userViewModel = userViewModel,
                    onNavigate = { navController.popBackStack() }
                )
            }

            // ✅ View Community Page
            composable("view_community") {
                ViewCommunityPage(userViewModel, communityViewModel, navController)
            }

            // ✅ Chat Screen
            composable("chat/{communityId}/{userId}") { backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId") ?: return@composable
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

                val context = LocalContext.current
                val database = remember { AppDatabase.getDatabase(context) }
                val userDao = remember { database.userDao }

                val chatRepository = remember { ChatRepository(FirebaseFirestore.getInstance()) }
                val chatViewModelFactory = remember { ChatViewModelFactory() }
                val chatViewModel: ChatViewModel = viewModel(factory = chatViewModelFactory)

                val userRepository = remember { UserRepository(userDao, context) } // ✅ Uses Room DB
                val userViewModelFactory = remember { UserViewModelFactory(userRepository) }
                val userViewModel: UserViewModel = viewModel(factory = userViewModelFactory)

                ChatScreen(
                    communityId = communityId,
                    userId = userId,
                    chatViewModel = chatViewModel,
                    userViewModel = userViewModel
                )
            }

            // Add `EditCommunityScreen` route
            composable("edit_community") {
                EditCommunityScreen(
                    navController = navController,
                    communityViewModel = communityViewModel,
                    userViewModel = userViewModel
                )
            }

            // Add `DeleteCommunityScreen` route
            composable("delete_community") {
                DeleteCommunityScreen(
                    navController = navController,
                    communityViewModel = communityViewModel,
                    userViewModel = userViewModel
                )
            }

            composable("word_search") {
                val repository = remember { WordSearchRepository() }
                WordSearchScreen(repository) // ✅ Correct: Directly calling the screen
            }

            composable("colouring") {
                ColoringScreen() // ✅ Correct: Directly calling the screen
            }


            composable("exerciseScreen") {
                val context = LocalContext.current
                ExerciseScreen(context = context)
            }

            composable("dailyChallenges") {
                MentalHealthScreen(
                    mentalHealthViewModel,
                    userViewModel,
                    navController)
            }

            composable("EmergencyScreen") {
                EmergencyScreen(emergencyViewModel)
            }




            composable("map") {
                MapScreen(apiKey = "AIzaSyBsBFoARe_FUQ3whvG_5j0mESD8ssscqDc")
            }


            composable("chatbot_screen/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable

                // Your existing setup for the ChatbotScreen

                val chatbotViewModel: ChatbotViewModel = viewModel()

                ChatbotScreen(
                    navController,
                    chatbotViewModel = chatbotViewModel,
                    userViewModel = userViewModel
                )
            }




            // Assessment Route
            composable("assessment") {
                QuizSelectionScreen(navController)
            }

            // PHQ-9 Assessment Route
            composable("phq9_screen") {
                PHQ9AssessmentScreen(navController)
            }

            // Result Screen Route
            composable("result/{score}") { backStackEntry ->
                val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                ResultScreen(score, userViewModel, quizViewModel)
            }

            // DASS Assessment Route
            composable("dass_screen") {
                DASS21Screen(navController = navController)
            }
            composable("dassResult/{depressionScore}/{anxietyScore}/{stressScore}") { backStackEntry ->
                val depressionScore = backStackEntry.arguments?.getString("depressionScore")?.toInt() ?: 0
                val anxietyScore = backStackEntry.arguments?.getString("anxietyScore")?.toInt() ?: 0
                val stressScore = backStackEntry.arguments?.getString("stressScore")?.toInt() ?: 0
                DASSResultScreen(depressionScore, anxietyScore, stressScore, userViewModel, quizViewModel)
            }


            composable("reminder_screen") {
                ReminderScreen(navController)
            }


            composable("todo_screen") {
                ToDoScreen()
            }

            composable("task_screen") {
                TaskSelectionScreen(navController)
            }


        }

    }
}

@Composable
fun EmergencyFAB(navController: NavController) {
    FloatingActionButton(
        onClick = { navController.navigate("EmergencyScreen") }, // Navigates to the Emergency screen
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .shadow(8.dp, CircleShape)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB71C1C), // Dark Red (Bottom)
                            Color(0xFFFF8A80)  // Light Red (Top)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Call, // Use an emergency icon such as a phone or SOS icon
                contentDescription = "Emergency",
                modifier = Modifier.size(50.dp),
                tint = Color.White
            )
        }
    }
}


@Composable
fun MindMapFAB(navController: NavController) {
    FloatingActionButton(
        onClick = { navController.navigate("map") },
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .shadow(8.dp, CircleShape)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2196F3), // Dark Purple (Bottom)
                            Color(0xBF68B8F5)  // Light Purple (Top)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Map,  // Change icon to a map icon
                contentDescription = "Mind Map",
                modifier = Modifier.size(50.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun ChatbotFAB(navController: NavController, userId: String) {
    FloatingActionButton(
        onClick = { navController.navigate("chatbot_screen/$userId") },
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .shadow(8.dp, CircleShape)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2196F3), // Dark Purple (Bottom)
                            Color(0xBF68B8F5)  // Light Purple (Top)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Face,
                contentDescription = "Chatbot",
                modifier = Modifier.size(50.dp),
                tint = Color.Black
            )
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem("Assessment", R.drawable.assessment, "assessment"),
        BottomNavItem("Community", R.drawable.community, "community"),
        BottomNavItem("Games", R.drawable.games, "games"),
        BottomNavItem("Planners", R.drawable.reminder,"task_Screen"),
        BottomNavItem("Video",R.drawable.play,"youtube_screen")
    )

    BottomNavigation(
        backgroundColor = Color(0xFF94C0EC),
        contentColor = Color.White,
        modifier = Modifier.height(75.dp)
    ) {
        val currentRoute = navController.currentDestination?.route

        items.forEach { item ->
            BottomNavigationItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White, shape = CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }
                },
                label = {
                    Text(
                        item.label,
                        fontSize = 12.sp,
                        color = if (currentRoute == item.route) Color.Black else Color.DarkGray
                    )
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.Unspecified
            )
        }
    }
}


