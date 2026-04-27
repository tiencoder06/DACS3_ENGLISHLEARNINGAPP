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
import com.example.englishlearningapp.ui.screens.quiz.QuizPartSelectionScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizResultScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizScreen
import com.example.englishlearningapp.ui.screens.quiz.QuizViewModel
import com.example.englishlearningapp.ui.screens.review.ReviewPracticeScreen
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
                onGoToTopic = { topicId -> 
                    navController.navigate(Routes.lessonList(topicId)) 
                },
                onGoToProgress = { navController.navigate(Routes.PROGRESS) },
                onGoToProfile = { navController.navigate(Routes.PROFILE) },
                onGoToPractice = { navController.navigate("practice/general") },
                onGoToQuiz = { navController.navigate(Routes.quizPartSelection("general")) }
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

        @Suppress("DEPRECATION")
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
            route = Routes.QUIZ_PART_SELECTION,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            QuizPartSelectionScreen(
                lessonId = lessonId,
                onBack = { navController.popBackStack() },
                onPartSelected = { part ->
                    navController.navigate(Routes.quiz(lessonId, part))
                }
            )
        }

        composable(
            route = Routes.QUIZ,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType },
                navArgument("part") { type = NavType.IntType }
            )
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
            // Note: This logic assumes the QuizScreen is still in the backstack
            // If the route was navigated from QUIZ, we find its backstack entry to share the ViewModel
            val quizEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(Routes.QUIZ)
                } catch (e: Exception) {
                    null
                }
            }
            
            if (quizEntry != null) {
                val quizViewModel: QuizViewModel = hiltViewModel(quizEntry)
                QuizResultScreen(
                    viewModel = quizViewModel,
                    onRetry = {
                        quizViewModel.retry()
                        navController.popBackStack()
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
            } else {
                // Fallback if quiz entry is lost
                Text("Error: Quiz data not found")
            }
        }

        composable(Routes.REVIEW) { backStackEntry ->
            val quizEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(Routes.QUIZ)
                } catch (e: Exception) {
                    null
                }
            }
            
            val reviewViewModel: ReviewViewModel = hiltViewModel()
            
            if (quizEntry != null) {
                val quizViewModel: QuizViewModel = hiltViewModel(quizEntry)
                LaunchedEffect(Unit) {
                    val wrongQuestions = quizViewModel.quizResult.value?.wrongQuestions?.map { it.first } ?: emptyList()
                    reviewViewModel.setQuestions(wrongQuestions)
                }
            }

            ReviewScreen(
                viewModel = reviewViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REVIEW_PRACTICE) {
            ReviewPracticeScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PROGRESS) {
            ProgressScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMistakes = {
                    navController.navigate(Routes.REVIEW_PRACTICE)
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
