package com.cs501.pantrypal.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentUserId = MutableStateFlow(-1)

    init {
        // Load the current user ID from DataStore
        viewModelScope.launch {
            val dataStore = application.userDataStore
            dataStore.data.map { preferences ->
                preferences[intPreferencesKey("user_id")] ?: -1
            }.collect { userId ->
                if (userId > 0) {
                    _currentUserId.value = userId
                    onUserIdChanged(userId)
                }
            }
        }
    }

    open fun onUserIdChanged(userId: Int) {}

    /**
     * Get the current user ID
     */
    fun getCurrentUserId(): Int {
        return _currentUserId.value
    }
    
    /**
     * Easy way to check if the user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return _currentUserId.value > 0
    }
}