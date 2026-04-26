package com.example.englishlearningapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Import các màn hình Auth & Profile
import com.example.englishlearningapp.ui.screens.auth.forgotpassword.ForgotPasswordScreen
import com.example.englishlearningapp.ui.screens.auth.login.LoginScreen
import com.example.englishlearningapp.ui.screens.auth.register.RegisterScreen
import com.example.englishlearningapp.ui.screens.profile.EditProfileScreen
import com.example.englishlearningapp.ui.screens.profile.ProfileScreen
import com.example.englishlearningapp.ui.screens.settings.SettingsScreen
import com.example.englishlearningapp.ui.screens.splash.SplashScreen
import com.example.englishlearningapp.ui.screens.home.HomeScreen


import com.example.englishlearningapp.ui.screens.topic.TopicScreen
import com.example.englishlearningapp.ui.screens.lesson.LessonScreen
import com.example.englishlearningapp.ui.screens.vocabulary.VocabularyDetailScreen
import com.example.englishlearningapp.ui.screens.vocabulary.VocabularyListScreen

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
        // --- 1. Luồng Splash & Auth ---
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
                onGoToRegister = { navController.navigate(Routes.REGISTER) },
                onGoToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }

        // --- 2. Luồng Chính (Home & Learning) ---
        composable(Routes.HOME) {
            HomeScreen(
                onGoToTopic = { navController.navigate(Routes.TOPIC) },
                onGoToProfile = { navController.navigate(Routes.PROFILE) },
                onGoToProgress = { navController.navigate(Routes.PROGRESS) } // Thêm dòng này vào
            )
        }

        composable(Routes.TOPIC) {
            TopicScreen(navController = navController)
        }

        // Truyền topicId từ TopicScreen sang LessonScreen
        composable(
            route = "lesson/{topicId}",
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            LessonScreen(navController = navController, topicId = topicId)
        }

        // --- 3. Luồng Cá nhân & Cài đặt ---
        composable(Routes.PROFILE) {
            ProfileScreen(
                onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                onNavigateToChangePassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        // --- 4. Luồng Quiz & Progress (Người 3) ---
        composable("quiz/{lessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            // Gọi QuizScreen thật ở đây
        }

        composable(Routes.PROGRESS) {
            // Gọi ProgressScreen thật ở đây
        }
        // Thêm màn hình Danh sách từ vựng
        composable("vocabulary_list/{lessonId}") { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            VocabularyListScreen(navController = navController, lessonId = lessonId)
        }

        // Màn hình chi tiết từ vựng (Flashcards)
        composable(
            route = "vocabulary_detail/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            VocabularyDetailScreen(navController = navController, lessonId = lessonId)
        }
    }
}
