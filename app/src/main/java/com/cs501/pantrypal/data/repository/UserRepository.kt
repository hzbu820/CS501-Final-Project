package com.cs501.pantrypal.data.repository

import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.database.UserDao
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    
    suspend fun getUserById(id: Int): User? {
        return userDao.getUserById(id)
    }
    
    suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)
    }
    
    suspend fun login(username: String, password: String): User? {
        return userDao.login(username, password)
    }
    
    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
}