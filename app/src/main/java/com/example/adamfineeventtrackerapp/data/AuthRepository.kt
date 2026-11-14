package com.example.adamfineeventtrackerapp.data

// Handles login and sign up
class AuthRepository(private val db: DatabaseHelper) {

    enum class RegisterResult { SUCCESS, MISSING_FIELDS, USER_EXISTS, FAILED }

    // True if the login is correct
    fun login(username: String, password: String): Boolean =
        db.checkUser(username, password)

    // Makes a new account
    fun register(username: String, password: String): RegisterResult {
        if (username.isBlank() || password.isBlank()) return RegisterResult.MISSING_FIELDS
        if (userExists(username)) return RegisterResult.USER_EXISTS
        return if (db.addUser(username, password)) RegisterResult.SUCCESS else RegisterResult.FAILED
    }

    fun userExists(username: String): Boolean =
        db.userExists(username)
}
