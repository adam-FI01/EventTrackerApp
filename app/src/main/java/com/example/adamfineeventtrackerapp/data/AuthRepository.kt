package com.example.adamfineeventtrackerapp.data

/**
 * Single entry point for everything account related (login, sign up, lookups).
 * Screens and ViewModels talk to this class instead of the database directly.
 */
class AuthRepository(private val db: DatabaseHelper) {

    enum class RegisterResult { SUCCESS, MISSING_FIELDS, USER_EXISTS, FAILED }

    /** Returns true when the username and password match a stored account. */
    fun login(username: String, password: String): Boolean =
        db.checkUser(username, password)

    /** Creates a new account if the fields are filled in and the username is free. */
    fun register(username: String, password: String): RegisterResult {
        if (username.isBlank() || password.isBlank()) return RegisterResult.MISSING_FIELDS
        if (userExists(username)) return RegisterResult.USER_EXISTS
        return if (db.addUser(username, password)) RegisterResult.SUCCESS else RegisterResult.FAILED
    }

    fun userExists(username: String): Boolean =
        db.userExists(username)
}
