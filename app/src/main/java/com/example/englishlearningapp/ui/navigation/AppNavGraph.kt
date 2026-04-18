package com.example.englishlearningapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.englishlearningapp.ui.screens.auth.forgotpassword.ForgotPasswordScreen
import com.example.englishlearningapp.ui.screens.auth.login.LoginScreen
import com.example.englishlearningapp.ui.screens.auth.register.RegisterScreen
import com.example.englishlearningapp.ui.screens.profile.ProfileScreen
import com.example.englishlearningapp.ui.screens.settings.SettingsScreen
import com.example.englishlearningapp.ui.screens.splash.SplashScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        // --- Người 1 Screens ---
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onGoToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // --- Người 2 & 3 Placeholder Stubs (Có nút bấm để Người 1 test) ---
        composable(Routes.HOME) {
            HomeScreenStub(
                onGoToTopic = { navController.navigate(Routes.TOPIC) },
                onGoToProfile = { navController.navigate(Routes.PROFILE) },
                onGoToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.TOPIC) {
            TopicScreenStub(
                onTopicClick = { topicId ->
                    navController.navigate("lesson/$topicId")
                }
            )
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            LessonScreenStub(
                topicId = topicId,
                onLessonClick = { lessonId ->
                    navController.navigate("vocabulary_list/$lessonId")
                }
            )
        }

        composable(
            route = Routes.VOCABULARY_LIST,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            VocabularyListScreenStub(
                lessonId = lessonId,
                onStartQuiz = { navController.navigate("quiz/$lessonId") },
                onStartPractice = { navController.navigate("practice/$lessonId") }
            )
        }

        composable(
            route = Routes.PRACTICE,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            PracticeScreenStub(lessonId = lessonId)
        }

        composable(
            route = Routes.QUIZ,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            QuizScreenStub(lessonId = lessonId)
        }

        composable(Routes.PROGRESS) {
            ProgressScreenStub()
        }
    }
}

// --- Stubs for other team members (Bổ sung nút để Người 1 kiểm tra Profile) ---

@Composable
fun HomeScreenStub(onGoToTopic: () -> Unit, onGoToProfile: () -> Unit, onGoToSettings: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Home Screen (Person 2's Task)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onGoToTopic) {
            Text("Go to Topic List (Person 2)")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Đây là nút để bạn (Người 1) kiểm tra màn hình Profile của mình
        Button(
            onClick = onGoToProfile,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Go to My Profile (Person 1)")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onGoToSettings) {
            Text("Settings (Person 1)")
        }
    }
}

@Composable
fun TopicScreenStub(onTopicClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Topic Screen Stub (Người 2)")
    }
}

@Composable
fun LessonScreenStub(topicId: String, onLessonClick: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Lesson Screen Stub for Topic: $topicId (Người 2)")
    }
}

@Composable
fun VocabularyListScreenStub(lessonId: String, onStartQuiz: () -> Unit, onStartPractice: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Vocabulary List Stub for Lesson: $lessonId (Người 2)")
    }
}

@Composable
fun PracticeScreenStub(lessonId: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Practice Screen Stub for Lesson: $lessonId (Người 3)")
    }
}

@Composable
fun QuizScreenStub(lessonId: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Quiz Screen Stub for Lesson: $lessonId (Người 3)")
    }
}

@Composable
fun ProgressScreenStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Progress Screen Stub (Người 3)")
    }
}
