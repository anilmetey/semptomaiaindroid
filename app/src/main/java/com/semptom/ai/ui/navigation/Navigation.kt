package com.semptom.ai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.semptom.ai.ui.screens.auth.AuthViewModel
import com.semptom.ai.ui.screens.auth.EmailConfigScreen
import com.semptom.ai.ui.screens.auth.ForgotPasswordScreen
import com.semptom.ai.ui.screens.auth.LoginScreen
import com.semptom.ai.ui.screens.auth.RegisterScreen
import com.semptom.ai.ui.screens.auth.VerifyCodeScreen
import com.semptom.ai.ui.screens.auth.ResetPasswordScreen
import com.semptom.ai.ui.screens.disclaimer.DisclaimerScreen
import com.semptom.ai.ui.screens.followup.FollowUpScreen
import com.semptom.ai.ui.screens.home.HomeScreen
import com.semptom.ai.ui.screens.journal.JournalScreen
import com.semptom.ai.ui.screens.profile.ProfileScreen
import com.semptom.ai.ui.screens.result.ResultScreen
import com.semptom.ai.ui.screens.statistics.StatisticsScreen
import com.semptom.ai.ui.screens.symptoms.SymptomSelectionScreen
import com.semptom.ai.ui.screens.triage.TriageScreen
import com.semptom.ai.ui.screens.splash.SplashScreen
import com.semptom.ai.ui.screens.analysis.AnalysisScreen
import com.semptom.ai.ui.screens.analysis.TextAnalysisScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object EmailConfig : Screen("email_config")
    object VerifyCode : Screen("verify_code")
    object ResetPassword : Screen("reset_password")
    object Disclaimer : Screen("disclaimer")
    object Home : Screen("home")
    object SymptomSelection : Screen("symptom_selection")
    object FollowUp : Screen("follow_up")
    object Result : Screen("result")
    object Triage : Screen("triage")
    object Profile : Screen("profile")
    object Journal : Screen("journal")
    object Statistics : Screen("statistics")
    object Analysis : Screen("analysis")
    object TextAnalysis : Screen("text_analysis")
}

@Composable
fun SemptomAINavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    val startDestination = Screen.Splash.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToMain = {
                    if (uiState.isLoggedIn) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }
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
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                onNavigateToVerifyCode = { email ->
                    navController.navigate("${Screen.VerifyCode.route}/$email")
                },
                onBackToLogin = {
                    navController.popBackStack()
                },
                onResetSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onNavigateToEmailConfig = {
                    navController.navigate(Screen.EmailConfig.route)
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.EmailConfig.route) {
            EmailConfigScreen(
                onSaveConfig = { email, password ->
                    // Save email configuration
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("${Screen.VerifyCode.route}/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyCodeScreen(
                email = email,
                onCodeVerified = { code ->
                    navController.navigate("${Screen.ResetPassword.route}/$email/$code")
                },
                onBackToForgotPassword = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        composable("${Screen.ResetPassword.route}/{email}/{code}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""
            ResetPasswordScreen(
                email = email,
                verificationCode = code,
                onPasswordReset = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                    }
                },
                onBackToVerifyCode = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
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
                    navController.navigate(Screen.Analysis.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.Journal.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Analysis.route) {
            AnalysisScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToManualSelection = {
                    navController.navigate(Screen.SymptomSelection.route)
                },
                onNavigateToTextAnalysis = {
                    navController.navigate(Screen.TextAnalysis.route)
                }
            )
        }

        composable(Screen.TextAnalysis.route) {
            TextAnalysisScreen(
                onBack = {
                    // Her durumda güvenilir olsun diye doğrudan Analysis ekranına dön
                    navController.navigate(Screen.Analysis.route) {
                        // Analysis zaten back stack'te ise ona kadar gidip üstünü temizle
                        popUpTo(Screen.Analysis.route) { inclusive = false }
                    }
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
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.Journal.route)
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                }
            )
        }

        composable(Screen.Journal.route) {
            JournalScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToStatistics = {
                    navController.navigate(Screen.Statistics.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToJournal = {
                    navController.navigate(Screen.Journal.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
    }
}
