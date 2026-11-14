package com.example.adamfineeventtrackerapp.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.adamfineeventtrackerapp.data.AuthRepository
import com.example.adamfineeventtrackerapp.data.AuthRepository.RegisterResult

// Login logic
class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set

    // Message to show the user
    var message by mutableStateOf<String?>(null)
        private set

    fun onUsernameChange(value: String) { username = value }
    fun onPasswordChange(value: String) { password = value }

    fun login(onSuccess: () -> Unit) {
        if (authRepository.login(username, password)) {
            onSuccess()
        } else {
            message = "Invalid username or password!"
        }
    }

    fun register() {
        message = when (authRepository.register(username, password)) {
            RegisterResult.SUCCESS -> "Account Created! You can now log in."
            RegisterResult.MISSING_FIELDS -> "Fill in both fields to register!"
            RegisterResult.USER_EXISTS -> "Username already exists!"
            RegisterResult.FAILED -> "Could not create account. Please try again."
        }
    }

    fun onMessageShown() { message = null }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { LoginViewModel(authRepository) }
        }
    }
}
