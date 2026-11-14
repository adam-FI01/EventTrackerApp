package com.example.adamfineeventtrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.adamfineeventtrackerapp.data.AuthRepository
import com.example.adamfineeventtrackerapp.data.DatabaseHelper
import com.example.adamfineeventtrackerapp.reminders.ReminderScheduler
import com.example.adamfineeventtrackerapp.ui.events.EventListScreen
import com.example.adamfineeventtrackerapp.ui.events.EventListViewModel
import com.example.adamfineeventtrackerapp.ui.login.LoginView
import com.example.adamfineeventtrackerapp.ui.login.LoginViewModel
import com.example.adamfineeventtrackerapp.ui.settings.SettingsScreen
import com.example.adamfineeventtrackerapp.ui.settings.SettingsViewModel
import com.example.adamfineeventtrackerapp.ui.theme.AdamFineEventTrackerAppTheme

/**
 * App entry point: creates the shared dependencies and wires up navigation
 * between the login, event list and settings screens.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = DatabaseHelper(applicationContext)
        val authRepository = AuthRepository(db)
        val reminderScheduler = ReminderScheduler(applicationContext)

        setContent {
            AdamFineEventTrackerAppTheme(darkTheme = true, dynamicColor = false) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = ROUTE_LOGIN) {
                    composable(ROUTE_LOGIN) {
                        LoginView(
                            viewModel = viewModel(factory = LoginViewModel.factory(authRepository)),
                            onLoginSuccess = { navController.navigate(ROUTE_EVENTS) }
                        )
                    }

                    composable(ROUTE_EVENTS) {
                        EventListScreen(
                            viewModel = viewModel(factory = EventListViewModel.factory(db, reminderScheduler)),
                            onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                        )
                    }

                    composable(ROUTE_SETTINGS) {
                        SettingsScreen(
                            viewModel = viewModel<SettingsViewModel>(),
                            onBack = { navController.popBackStack() },
                            onSignOut = {
                                navController.navigate(ROUTE_LOGIN) {
                                    popUpTo(ROUTE_LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    private companion object {
        const val ROUTE_LOGIN = "login"
        const val ROUTE_EVENTS = "events"
        const val ROUTE_SETTINGS = "settings"
    }
}
