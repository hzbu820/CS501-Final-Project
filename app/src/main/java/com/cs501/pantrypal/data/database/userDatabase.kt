package com.cs501.pantrypal.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey
    val id: String,
    val username: String,
    val password: String,
    val email: String,
    val profileImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY username ASC")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :emailAddress")
    suspend fun login(emailAddress: String): User?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
}