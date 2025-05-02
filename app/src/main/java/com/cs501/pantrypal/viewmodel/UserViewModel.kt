package com.cs501.pantrypal.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.firebase.FirebaseService
import com.cs501.pantrypal.data.repository.UserRepository
import com.cs501.pantrypal.util.PasswordCheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

// DataStore to store user preferences


class UserViewModel(application: Application) : BaseViewModel(application) {
    private val repository: UserRepository


    // StateFlow for current user information
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    init {
        val database = AppDatabase.getDatabase(getApplication())
        repository = UserRepository(database.userDao())
        val currentUserId = getCurrentUserId()
        viewModelScope.launch {
            _currentUser.value = repository.getUserById(currentUserId)
        }
    }

    override fun onUserIdChanged(userId: String) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user != null) {
                setCurrentUserId(user.id)
                _currentUser.value = user
            }
        }
    }


    /**
     * Login function
     */
    suspend fun login(emailAddress: String, password: String): Map<String, Any> {
        val user = repository.getUserByEmail(emailAddress)
        val firebaseUser = FirebaseService.getInstance().getUserByEmail(emailAddress)
        val passwordCheck = PasswordCheck()

        if (firebaseUser == null) {
            if (user != null) {
                repository.deleteUser(user)
            }
            return mapOf("success" to false, "message" to "User not found")
        }

        if (user != null) {
            val isPasswordValid = passwordCheck.verifyPassword(password, user.password)
            if (isPasswordValid) {
                dataStore.edit { preferences ->
                    preferences[stringPreferencesKey("user_id")] = user.id
                }
                setCurrentUserId(user.id)
                _currentUser.value = user
                return mapOf("success" to true, "message" to "Login successful")
            } else {
                return mapOf("success" to false, "message" to "Password is incorrect")
            }
        }

        val isPasswordValid = passwordCheck.verifyPassword(password, firebaseUser.password)
        if (isPasswordValid) {
            // Save user ID to DataStore
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey("user_id")] = firebaseUser.id
            }
            setCurrentUserId(firebaseUser.id)
            _currentUser.value = firebaseUser
            repository.registerUser(firebaseUser)
            return mapOf("success" to true, "message" to "Login successful")
        } else {
            return mapOf("success" to false, "message" to "Password is incorrect")
        }

        return mapOf("success" to false, "message" to "Unknown error")
    }

    /**
     * Register function
     */
    suspend fun register(username: String, password: String, email: String = ""): Boolean {
        // Check if email already exists
        val firebaseUser = FirebaseService.getInstance()
        val existingUserRemote = firebaseUser.isEmailNotRegistered(email)
        val existingUser = repository.getUserByEmail(email)

        if (existingUser != null || !existingUserRemote) {
            return false
        }
        val id = UUID.randomUUID().toString()

        // Hash password
        val hashPassword = PasswordCheck()
        val hashedPassword = hashPassword.hashPassword(password)

        // Create new user
        val newUser = User(
            id = id, username = username, password = hashedPassword, email = email
        )
        repository.registerUser(newUser)

        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("user_id")] = newUser.id
        }
        setCurrentUserId(newUser.id)
        _currentUser.value = newUser
        // Sync user data with Firebase
        firebaseUser.syncUserData(newUser)
        return true
    }

    /**
     * Logout function
     */
    fun logout() {
        clearUserId()
        _currentUser.value = null
    }

    /**
     * Update user profile information
     */
    fun updateUserProfile(user: User): Boolean {
        viewModelScope.launch {
            repository.updateUser(user)
            setCurrentUserId(user.id)
            _currentUser.value = user
        }
        return true
    }

    /**
     * Delete user account
     */
    fun deleteUserAccount(user: User): Boolean {
        viewModelScope.launch {
            repository.deleteUser(user)
            clearUserId()
            _currentUser.value = null
        }
        return true
    }


}