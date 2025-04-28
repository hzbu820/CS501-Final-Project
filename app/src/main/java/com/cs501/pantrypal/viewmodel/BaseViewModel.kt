package com.cs501.pantrypal.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentUserId = MutableStateFlow("")

    init {
        // Load the current user ID from DataStore
        viewModelScope.launch {
            val dataStore = application.userDataStore
            dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("user_id")] ?: ""
            }.collect { userId ->
                if (userId != "") {
                    _currentUserId.value = userId
                    onUserIdChanged(userId)
                }
            }
        }
    }

    open fun onUserIdChanged(userId: String) {}

    /**
     * Get the current user ID
     */
    fun getCurrentUserId(): String {
        return _currentUserId.value
    }

    /**
     * Easy way to check if the user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return _currentUserId.value != ""
    }
}