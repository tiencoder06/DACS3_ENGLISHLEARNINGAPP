package com.example.englishlearningapp.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.example.englishlearningapp.ui.screens.home.HomeScreen
import com.example.englishlearningapp.ui.screens.practice.PracticeScreen
import com.example.englishlearningapp.ui.screens.profile.ProfileScreen
import com.example.englishlearningapp.ui.screens.progress.ProgressScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizResultScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizViewModel
import com.example.englishlearningapp.ui.screens.review.ReviewScreen
import com.example.englishlearningapp.ui.screens.review.ReviewViewModel
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
        // --- Auth ---
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

        // --- Main Content ---
        composable(Routes.HOME) {
            HomeScreen(
                onGoToTopic = { navController.navigate(Routes.TOPIC) },
                onGoToProgress = { navController.navigate(Routes.PROGRESS) },
                onGoToProfile = { navController.navigate(Routes.PROFILE) },
                onGoToPractice = { navController.navigate("practice/general") },
                onGoToQuiz = { navController.navigate("quiz/general") }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onEditProfileClick = { /* TODO: Navigate to Edit Profile */ },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                onLogoutSuccess = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        // --- Practice ---
        composable(
            route = Routes.PRACTICE,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) {
            PracticeScreen(onBack = { navController.popBackStack() })
        }

        // --- Quiz ---
        composable(
            route = Routes.QUIZ,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val viewModel: QuizViewModel = hiltViewModel(backStackEntry)
            QuizScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToResult = { score, correct, total ->
                    navController.navigate(Routes.result(score, correct, total))
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
        ) { backStackEntry ->
            val quizEntry = remember(backStackEntry) {
                navController.getBackStackEntry("quiz/{lessonId}")
            }
            val quizViewModel: QuizViewModel = hiltViewModel(quizEntry)
            
            QuizResultScreen(
                viewModel = quizViewModel,
                onRetry = {
                    quizViewModel.retry()
                    navController.popBackStack("quiz/{lessonId}", false)
                },
                onBackHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onReview = {
                    navController.navigate(Routes.REVIEW)
                }
            )
        }

        composable(Routes.REVIEW) {
            val quizEntry = remember(it) {
                navController.getBackStackEntry("quiz/{lessonId}")
            }
            val quizViewModel: QuizViewModel = hiltViewModel(quizEntry)
            val reviewViewModel: ReviewViewModel = hiltViewModel()
            
            // Sync wrong questions before showing screen
            LaunchedEffect(Unit) {
                val wrongQuestions = quizViewModel.quizResult.value?.wrongQuestions?.map { it.first } ?: emptyList()
                reviewViewModel.setQuestions(wrongQuestions)
            }

            ReviewScreen(
                viewModel = reviewViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PROGRESS) {
            ProgressScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMistakes = {
                    navController.navigate(Routes.REVIEW)
                }
            )
        }

        // --- Stubs ---
        composable(Routes.TOPIC) { TopicScreenStub() }
        composable(Routes.LESSON) { Box(modifier = Modifier.fillMaxSize()) }
        composable(Routes.VOCABULARY_LIST) { Box(modifier = Modifier.fillMaxSize()) }
    }
}

@Composable fun TopicScreenStub() { Box(modifier = Modifier.fillMaxSize()) { Text("Topics") } }
