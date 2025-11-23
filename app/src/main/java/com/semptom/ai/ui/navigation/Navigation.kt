package com.semptom.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.semptom.ai.ui.screens.auth.LoginScreen
import com.semptom.ai.ui.screens.auth.RegisterScreen
import com.semptom.ai.ui.screens.disclaimer.DisclaimerScreen
import com.semptom.ai.ui.screens.followup.FollowUpScreen
import com.semptom.ai.ui.screens.home.HomeScreen
import com.semptom.ai.ui.screens.journal.JournalScreen
import com.semptom.ai.ui.screens.profile.ProfileScreen
import com.semptom.ai.ui.screens.result.ResultScreen
import com.semptom.ai.ui.screens.statistics.StatisticsScreen
import com.semptom.ai.ui.screens.symptoms.SymptomSelectionScreen
import com.semptom.ai.ui.screens.triage.TriageScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Disclaimer : Screen("disclaimer")
    object Home : Screen("home")
    object SymptomSelection : Screen("symptom_selection")
    object FollowUp : Screen("follow_up")
    object Result : Screen("result")
    object Triage : Screen("triage")
    object Profile : Screen("profile")
    object Journal : Screen("journal")
    object Statistics : Screen("statistics")
}

@Composable
fun SemptomAINavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Disclaimer.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPassword = {
                    // TODO: Implement forgot password flow if needed
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Disclaimer.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Disclaimer.route) {
            DisclaimerScreen(
                onAccept = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Disclaimer.route) { inclusive = true }
                    }
                },
                onDecline = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Disclaimer.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onStartAnalysis = {
                    navController.navigate(Screen.SymptomSelection.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.Journal.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        composable(Screen.SymptomSelection.route) {
            SymptomSelectionScreen(
                onNext = {
                    navController.navigate(Screen.FollowUp.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FollowUp.route) {
            FollowUpScreen(
                onAnalyze = { isTriage ->
                    if (isTriage) {
                        navController.navigate(Screen.Triage.route)
                    } else {
                        navController.navigate(Screen.Result.route)
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                onNewAnalysis = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Triage.route) {
            TriageScreen(
                onBack = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Journal.route) {
            JournalScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
