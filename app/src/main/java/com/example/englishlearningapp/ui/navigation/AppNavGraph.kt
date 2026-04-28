package com.example.englishlearningapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import com.example.englishlearningapp.ui.screens.quiz.QuizScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizResultScreen
import com.example.englishlearningapp.ui.screens.vocabulary.VocabularyListScreen
import com.example.englishlearningapp.ui.screens.vocabulary.VocabularyDetailScreen
import com.example.englishlearningapp.ui.screens.progress.ProgressScreen
import com.example.englishlearningapp.ui.screens.review.ReviewScreen
import com.example.englishlearningapp.ui.screens.practice.PracticeScreen
import com.example.englishlearningapp.ui.screens.placement.PlacementIntroScreen
import com.example.englishlearningapp.ui.screens.placement.PlacementQuestionScreen
import com.example.englishlearningapp.ui.screens.placement.PlacementResultScreen
import com.example.englishlearningapp.ui.screens.placement.PlacementViewModel
import com.example.englishlearningapp.utils.TextToSpeechHelper

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    ttsHelper: TextToSpeechHelper
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        modifier = modifier
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigate = { destination ->
                    navController.navigate(destination) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.PLACEMENT_INTRO) {
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
                    navController.navigate(Routes.PLACEMENT_INTRO) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        @Suppress("DEPRECATION")
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                onSecurityClick = { navController.navigate(Routes.FORGOT_PASSWORD) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // --- Placement Test ---
        composable(Routes.PLACEMENT_INTRO) {
            val viewModel: PlacementViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isCompleted) {
                if (uiState.isCompleted) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PLACEMENT_INTRO) { inclusive = true }
                    }
                }
            }

            PlacementIntroScreen(
                isLoading = uiState.isSaving,
                errorMessage = uiState.errorMessage,
                onClearError = { viewModel.clearError() },
                onStartClick = {
                    navController.navigate(Routes.PLACEMENT_QUESTION)
                },
                onSkipClick = {
                    viewModel.skipPlacement()
                }
            )
        }

        composable(Routes.PLACEMENT_QUESTION) {
            val parentEntry = remember(it) { navController.getBackStackEntry(Routes.PLACEMENT_INTRO) }
            val viewModel: PlacementViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isCompleted) {
                if (uiState.isCompleted) {
                    navController.navigate(Routes.PLACEMENT_RESULT) {
                        popUpTo(Routes.PLACEMENT_QUESTION) { inclusive = true }
                    }
                }
            }

            PlacementQuestionScreen(
                uiState = uiState,
                ttsHelper = ttsHelper,
                onAnswerSelected = { viewModel.selectAnswer(it) },
                onNextClick = { viewModel.goToNextQuestion() },
                onBackClick = { viewModel.goToPreviousQuestion() },
                onSubmitClick = { viewModel.submitPlacement() },
                onNavigateBack = { navController.popBackStack() },
                onClearError = { viewModel.clearError() }
            )
        }

        composable(Routes.PLACEMENT_RESULT) {
            val parentEntry = remember(it) { navController.getBackStackEntry(Routes.PLACEMENT_INTRO) }
            val viewModel: PlacementViewModel = hiltViewModel(parentEntry)
            val uiState by viewModel.uiState.collectAsState()

            PlacementResultScreen(
                result = uiState.result,
                onStartLearningClick = { topicId, _ ->
                    // Quan trọng: Điều hướng về HOME trước để làm gốc
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PLACEMENT_INTRO) { inclusive = true }
                    }
                    // Sau đó mới mở màn hình Lesson nếu có ID
                    if (topicId.isNotEmpty()) {
                        navController.navigate(Routes.lessonList(topicId))
                    }
                },
                onHomeClick = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PLACEMENT_INTRO) { inclusive = true }
                    }
                }
            )
        }

        // --- Các màn hình chính ---
        composable(Routes.HOME) {
            HomeScreen(
                onGoToTopic = { navController.navigate(Routes.TOPIC) },
                onGoToProgress = { navController.navigate(Routes.PROGRESS) },
                onGoToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.TOPIC) {
            TopicScreen(navController = navController)
        }

        composable(
            route = Routes.LESSON,
            arguments = listOf(navArgument("topicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val topicId = backStackEntry.arguments?.getString("topicId") ?: ""
            LessonScreen(
                navController = navController,
                topicId = topicId
            )
        }

        composable(
            route = Routes.VOCABULARY_LIST,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            VocabularyListScreen(
                navController = navController,
                lessonId = lessonId
            )
        }

        composable(
            route = Routes.VOCABULARY_DETAIL,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            VocabularyDetailScreen(
                navController = navController,
                lessonId = lessonId
            )
        }

        composable(
            route = Routes.PRACTICE,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            PracticeScreen(
                onBack = { navController.popBackStack() },
                onFinish = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.QUIZ,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            QuizScreen(
                onBack = { navController.popBackStack() },
                onNavigateToResult = { score, correct, total ->
                    navController.navigate(Routes.result(score, correct, total)) {
                        popUpTo(Routes.quiz(lessonId)) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("correct") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) {
            QuizResultScreen(
                onRetry = { navController.popBackStack() },
                onBackHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onReview = { navController.navigate(Routes.REVIEW) }
            )
        }

        composable(Routes.PROGRESS) {
            ProgressScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMistakes = { navController.navigate(Routes.REVIEW) }
            )
        }

        composable(Routes.REVIEW) {
            ReviewScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
