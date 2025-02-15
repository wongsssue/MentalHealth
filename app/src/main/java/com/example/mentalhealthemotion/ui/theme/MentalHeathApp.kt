package com.example.mentalhealthemotion.ui.theme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mentalhealthemotion.Data.AppDatabase
import com.example.mentalhealthemotion.R
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.mentalhealthemotion.Data.EduContentRepository
import com.example.mentalhealthemotion.Data.EduContentViewModel
import com.example.mentalhealthemotion.Data.EduContentViewModelFactory
import com.example.mentalhealthemotion.Data.MoodEntryRepository
import com.example.mentalhealthemotion.Data.MoodEntryViewModel
import com.example.mentalhealthemotion.Data.MoodEntryViewModelFactory
import com.example.mentalhealthemotion.Data.MusicRepository
import com.example.mentalhealthemotion.Data.MusicViewModel
import com.example.mentalhealthemotion.Data.MusicViewModelFactory
import com.example.mentalhealthemotion.Data.UserRepository
import com.example.mentalhealthemotion.Data.UserViewModel
import com.example.mentalhealthemotion.Data.UserViewModelFactory

enum class MentalHeathAppScreen {
    GetStarted,
    Login,
    Register,
    Menu,
    ProfilePage,
    HomePage,
    MoodTrackerPage,
    EditEntryPage,
    StatisticPage,
    QuestionnairePage,
    ResultPage,
    HistoryPage,
    MusicStartPage,
    MusicPage,
    EduLibraryPage,
    AdminHomePage,
    AdminManageEduPage,
    AdminManageUserPage
}


@Composable
fun AppBar(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        // Menu Icon
        IconButton(onClick = onMenuClick) {
            Icon(
                painter = painterResource(R.drawable.menu),
                contentDescription = "Menu Bar",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

@Composable
fun MentalHeathApp(
    navController: NavHostController = rememberNavController(),
    context: Context = LocalContext.current
) {

    // Initialize the database and repository
    val appDatabase = remember { AppDatabase.getDatabase(context) }
    val userRepository = remember { UserRepository(appDatabase.userDao,context) }
    val moodEntryRepository = remember { MoodEntryRepository(appDatabase.moodEntryDao,context)}
    val musicRepository = remember { MusicRepository(appDatabase.moodEntryDao,context)}
    val eduRepository = remember { EduContentRepository(appDatabase.eduContentDao,context)}

    // Create ViewModelFactory instances
    val userViewModelFactory = remember { UserViewModelFactory(userRepository) }
    val moodEntryViewModelFactory = remember { MoodEntryViewModelFactory(moodEntryRepository) }
    val musicViewModelFactory = remember { MusicViewModelFactory(musicRepository) }
    val eduContentViewModelFactory = remember { EduContentViewModelFactory(eduRepository)}

    // Get ViewModel instances using the factory
    val userViewModel: UserViewModel = viewModel(factory = userViewModelFactory)
    val moodViewModel: MoodEntryViewModel = viewModel(factory = moodEntryViewModelFactory)
    val musicViewModel: MusicViewModel = viewModel(factory = musicViewModelFactory)
    val eduViewModel: EduContentViewModel = viewModel(factory = eduContentViewModelFactory)

    //Initialize admin, content
    userViewModel.initializeAdmin()
    eduViewModel.initializeContent()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: MentalHeathAppScreen.GetStarted.name


    // Define pages where the AppBar should be shown
    val showAppBarScreens = setOf(
        MentalHeathAppScreen.HomePage.name,
        MentalHeathAppScreen.MoodTrackerPage.name,
        MentalHeathAppScreen.EditEntryPage.name,
        MentalHeathAppScreen.StatisticPage.name,
        MentalHeathAppScreen.QuestionnairePage.name,
        MentalHeathAppScreen.ResultPage.name,
        MentalHeathAppScreen.HistoryPage.name,
        MentalHeathAppScreen.MusicStartPage.name,
        MentalHeathAppScreen.MusicPage.name,
        MentalHeathAppScreen.EduLibraryPage.name
    )

    Scaffold(
        topBar = {
            if (currentRoute in showAppBarScreens) {
                AppBar(
                    onMenuClick = { navController.navigate(MentalHeathAppScreen.Menu.name) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MentalHeathAppScreen.GetStarted.name,
            modifier = Modifier.padding(innerPadding)
        ) {

            //WelcomeScreen
            composable(route = MentalHeathAppScreen.GetStarted.name) {
                WelcomeScreen(
                    loginPage = {navController.navigate(MentalHeathAppScreen.Login.name)},
                    registerPage = { navController.navigate(MentalHeathAppScreen.Register.name) }
                )
            }

            //LoginScreen
            composable(route = MentalHeathAppScreen.Login.name) {
                LoginScreen(
                    userViewModel,
                    navigateToAdminPage = {navController.navigate(MentalHeathAppScreen.AdminHomePage.name)},
                    navigateToRegisterPage = { navController.navigate(MentalHeathAppScreen.Register.name) },
                    navigateToUserPage = {
                        navController.navigate(MentalHeathAppScreen.HomePage.name)
                    }
                )
            }

            //RegisterScreen
            composable(route = MentalHeathAppScreen.Register.name) {
                RegisterScreen(
                    userViewModel,
                    loginPage = {navController.navigate(MentalHeathAppScreen.Login.name)}
                )
            }

            //MenuScreen
            composable(route = MentalHeathAppScreen.Menu.name) {
                MenuScreen(
                    onMenuItemClick = { item ->
                        navController.navigate(item)
                    },
                    onSignOutClick = {
                        userViewModel.clearUser()
                        navController.navigate(MentalHeathAppScreen.GetStarted.name)
                    },
                    navController = navController
                )
            }

            //HomePage
            composable(route = MentalHeathAppScreen.HomePage.name) {
                HomePage(
                    onEmotionClick = { navController.navigate(MentalHeathAppScreen.MoodTrackerPage.name)},
                    onMentalClick = { }
                )
            }


            //MoodTrackerPage
            composable(route = MentalHeathAppScreen.MoodTrackerPage.name) {
                MoodTrackerPage(
                    moodViewModel,
                    userViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            //Mood Entry Page
            composable(
                route = "EditEntryPage?isEditing={isEditing}",
                arguments = listOf(
                    navArgument("isEditing") { type = NavType.BoolType; defaultValue = false }
                )
            ) { backStackEntry ->
                val isEditing = backStackEntry.arguments?.getBoolean("isEditing") ?: false

                EditEntryPage(
                    userViewModel,
                    moodViewModel,
                    isEditing = isEditing,
                    onback = { navController.navigate(MentalHeathAppScreen.MoodTrackerPage.name) }
                )
            }


            //ProfilePage
            composable(route = MentalHeathAppScreen.ProfilePage.name) {
                ProfilePage(
                    userViewModel,
                    onBack = {navController.navigate(MentalHeathAppScreen.Menu.name)}
                )
            }

            //Menu Bar
            composable(route = MentalHeathAppScreen.Menu.name){
                MenuScreen(
                    onMenuItemClick = { item ->
                        navController.navigate(item)
                    },
                    onSignOutClick = {
                        navController.navigate(MentalHeathAppScreen.GetStarted.name)
                    },
                    navController = navController
                )
            }

            //Statistic Page
            composable(route = MentalHeathAppScreen.StatisticPage.name) {
                StatisticPage(
                    moodEntryViewModel = moodViewModel,
                    userViewModel = userViewModel,
                    toEntryPage = {navController.navigate(MentalHeathAppScreen.MoodTrackerPage.name) },
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            //Music Start Page
            composable(route = MentalHeathAppScreen.MusicStartPage.name) {
                MusicStartPage(
                    onNavigate = { route -> navController.navigate(route) },
                    musicSuggestions = { navController.navigate(MentalHeathAppScreen.MusicPage.name)}
                )
            }

            // Music Page
            composable(route = MentalHeathAppScreen.MusicPage.name) {
                MusicPage(
                    onNavigate = { route -> navController.navigate(route) },
                    toMusicStartPage = {navController.navigate(MentalHeathAppScreen.MusicStartPage.name)},
                    musicViewModel = musicViewModel,
                    userViewModel = userViewModel
                )
            }

            // Sleep Test Page
            composable(route = MentalHeathAppScreen.QuestionnairePage.name) {
                QuestionnaireScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Test History Page
            composable(route = MentalHeathAppScreen.HistoryPage.name) {
                HistoryScreen(
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Educational Library Page
            composable(route = MentalHeathAppScreen.EduLibraryPage.name) {
                EducationalLibraryPage(
                    eduViewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }

            // Admin Home Page
            composable(route = MentalHeathAppScreen.AdminHomePage.name) {
                AdminHomePage(
                    navController,
                    onSignOutClick = { navController.navigate(MentalHeathAppScreen.GetStarted.name) }
                )
            }

            // Admin Manage User Page
            composable(route = MentalHeathAppScreen.AdminManageUserPage.name) {
                AdminUserList(
                    userViewModel,
                    backClick = { navController.navigate(MentalHeathAppScreen.AdminHomePage.name) }
                )
            }

            // Admin Manage Edu Page
            composable(route = MentalHeathAppScreen.AdminManageEduPage.name) {
                AdminEducationalLibrary(
                    eduViewModel,
                    backClick = { navController.navigate(MentalHeathAppScreen.AdminHomePage.name) }
                )
            }
        }
    }
}

//@Composable
//fun SleepQualityApp() {
//    val navController = rememberNavController()
//    NavHost(navController = navController, startDestination = "questionnaire") {
//        composable("questionnaire") { QuestionnaireScreen(navController) }
//        composable("result/{result}") { backStackEntry ->
//            val result = backStackEntry.arguments?.getString("result") ?: "Unknown"
//            ResultScreen(navController, result)
//        }
//        composable("history") { HistoryScreen(navController) }
//    }
//}
//
