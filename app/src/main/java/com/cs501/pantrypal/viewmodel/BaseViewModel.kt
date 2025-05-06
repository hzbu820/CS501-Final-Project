package com.cs501.pantrypal.viewmodel

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cs501.pantrypal.data.firebase.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentUserId = MutableStateFlow("")
    val dataStore = application.userDataStore

    init {
        // Load the current user ID from DataStore
        viewModelScope.launch {

            dataStore.data.map { preferences ->
                preferences[stringPreferencesKey("user_id")] ?: ""
            }.collect { userId ->
                if (userId != "") {
                    val firebaseService = FirebaseService.getInstance()
                    val deleted = firebaseService.isUserDeleted(userId)
                    if (deleted) {
                        // If the user is deleted, clear the user ID
                        dataStore.edit { prefs ->
                            prefs[stringPreferencesKey("user_id")] = ""
                        }
                    } else {
                        // If the user is not deleted, set the current user ID
                        _currentUserId.value = userId
                        onUserIdChanged(userId)
                    }
                }
            }
        }
    }

    open fun onUserIdChanged(userId: String) {}

    fun logout(){
        viewModelScope.launch {
            val dataStore = getApplication<Application>().userDataStore
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_id")] = ""
            }
            _currentUserId.value = ""
        }
        onLogout();
    }

    abstract fun onLogout();

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


    /**
     * Set the current user ID
     */
    fun setCurrentUserId(userId: String) {
        viewModelScope.launch {
            val dataStore = getApplication<Application>().userDataStore
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey("user_id")] = userId
            }
            _currentUserId.value = userId
        }
    }

}