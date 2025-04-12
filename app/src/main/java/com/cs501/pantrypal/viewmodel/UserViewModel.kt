package com.cs501.pantrypal.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.database.AppDatabase
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore to store user preferences
val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository
    
    // Current login status
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Boolean get() = _isLoggedIn.value
    
    // Current user information
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser
    
    // Key for storing user ID in DataStore
    private val USER_ID_KEY = intPreferencesKey("user_id")
    
    // DataStore instance
    private val dataStore = application.userDataStore
    
    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database.userDao())
        
        // Check if user is already logged in
        viewModelScope.launch {
            dataStore.data.map { preferences ->
                preferences[USER_ID_KEY]
            }.collect { userId ->
                if (userId != null && userId > 0) {
                    val user = repository.getUserById(userId)
                    if (user != null) {
                        _currentUser.value = user
                        _isLoggedIn.value = true
                    }
                }
            }
        }
    }
    
    /**
     * Login function
     */
    suspend fun login(username: String, password: String): Boolean {
        val user = repository.login(username, password)
        return if (user != null) {
            // Save user ID to DataStore
            dataStore.edit { preferences ->
                preferences[USER_ID_KEY] = user.id
            }
            _currentUser.value = user
            _isLoggedIn.value = true
            true
        } else {
            false
        }
    }
    
    /**
     * Register function
     */
    suspend fun register(username: String, password: String, email: String = ""): Boolean {
        // Check if username already exists
        val existingUser = repository.getUserByUsername(username)
        if (existingUser != null) {
            return false
        }
        
        // Create new user
        val newUser = User(
            username = username,
            password = password,
            email = email
        )
        
        val userId = repository.registerUser(newUser)
        return if (userId > 0) {
            val user = repository.getUserById(userId.toInt())
            if (user != null) {
                dataStore.edit { preferences ->
                    preferences[USER_ID_KEY] = user.id
                }
                _currentUser.value = user
                _isLoggedIn.value = true
            }
            true
        } else {
            false
        }
    }
    
    /**
     * Logout function
     */
    fun logout() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(USER_ID_KEY)
            }
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }
}