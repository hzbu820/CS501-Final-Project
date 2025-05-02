package com.cs501.pantrypal.data.firebase

import com.cs501.pantrypal.data.database.SavedRecipe
import com.cs501.pantrypal.data.database.User
import com.cs501.pantrypal.data.database.UserIngredients
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseService private constructor() {
    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    companion object {
        @Volatile
        private var instance: FirebaseService? = null

        fun getInstance(): FirebaseService {
            return instance ?: synchronized(this) {
                instance ?: FirebaseService().also { instance = it }
            }
        }
    }

    suspend fun syncUserData(user: User): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val userMap = mapOf(
                    "id" to user.id,
                    "username" to user.username,
                    "email" to user.email,
                    "createdAt" to user.createdAt,
                    "password" to user.password,
                )

                db.collection("users").document(user.id.toString()).set(userMap).await()

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncUserIngredients(ingredients: List<UserIngredients>, userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val batch = db.batch()

                val existingDocs =
                    db.collection("users").document(userId.toString()).collection("ingredients")
                        .get().await()

                for (doc in existingDocs) {
                    batch.delete(doc.reference)
                }

                for (ingredient in ingredients) {
                    val docRef =
                        db.collection("users").document(userId.toString()).collection("ingredients")
                            .document(ingredient.id.toString())

                    batch.set(docRef, ingredient)
                }

                batch.commit().await()

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun syncRecipes(recipes: List<SavedRecipe>, userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val batch = db.batch()

                val existingDocs =
                    db.collection("users").document(userId.toString()).collection("recipes")
                        .get().await()

                for (doc in existingDocs) {
                    batch.delete(doc.reference)
                }

                for (recipe in recipes) {
                    val docRef =
                        db.collection("users").document(userId.toString()).collection("recipes")
                            .document(recipe.id.toString())

                    val recipeMap = mapOf(
                        "id" to recipe.id,
                        "label" to recipe.label,
                        "image" to recipe.image,
                        "url" to recipe.url,
                        "ingredientLines" to recipe.ingredientLines,
                        "calories" to recipe.calories,
                        "isFavorite" to recipe.isFavorite,
                        "dateAdded" to recipe.dateAdded,
                        "userId" to recipe.userId,
                        "cookbookName" to recipe.cookbookName
                    )

                    batch.set(docRef, recipeMap)
                }

                batch.commit().await()

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun restoreUserIngredients(userId: String): List<UserIngredients> {
        return try {
            withContext(Dispatchers.IO) {
                val querySnapshot =
                    db.collection("users").document(userId.toString()).collection("ingredients")
                        .get().await()

                querySnapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        UserIngredients(
                            name = data["name"] as String,
                            foodCategory = data["foodCategory"] as String,
                            image = data["image"] as String,
                            quantity = data["quantity"] as String,
                            unit = data["unit"] as String,
                            expirationDate = data["expirationDate"] as String,
                            location = data["location"] as String,
                            notes = data["notes"] as String,
                            isFavorite = data["favorite"] as Boolean,
                            userId = (data["userId"] as String)
                        )

                    } else null

                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun restoreRecipes(userId: String): List<SavedRecipe> {
        return try {
            withContext(Dispatchers.IO) {
                val querySnapshot =
                    db.collection("users").document(userId.toString()).collection("recipes")
                        .get().await()

                querySnapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        SavedRecipe(
                            id = (data["id"] as Long).toInt(),
                            label = data["label"] as String,
                            image = data["image"] as String,
                            url = data["url"] as String,
                            ingredientLines = (data["ingredientLines"] as List<*>).map { it as String },
                            calories = (data["calories"] as Number).toDouble(),
                            isFavorite = data["isFavorite"] as Boolean,
                            dateAdded = data["dateAdded"] as Long,
                            userId = data["userId"] as String,
                            cookbookName = data["cookbookName"] as String
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun isEmailNotRegistered(email: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val querySnapshot =
                    db.collection("users").whereEqualTo("email", email).get().await()

                querySnapshot.isEmpty
            }
        } catch (e: Exception) {
            true
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return try {
            withContext(Dispatchers.IO) {
                val querySnapshot =
                    db.collection("users").whereEqualTo("email", email).get().await()

                if (querySnapshot.isEmpty) null
                else {
                    val data = querySnapshot.documents[0].data
                    if (data != null) {
                        User(
                            id = (data["id"] as String),
                            username = data["username"] as String,
                            password = data["password"] as String,
                            email = data["email"] as String,
                            createdAt = data["createdAt"] as Long
                        )
                    } else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteUserAccount(userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                db.collection("users").document(userId).delete().await()

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateUserPassword(
        userId: String,
        newPassword: String
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val userMap = mapOf(
                    "password" to newPassword
                )

                db.collection("users").document(userId).update(userMap).await()

                true
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun isUserDeleted(userId: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val querySnapshot = db.collection("users").whereEqualTo("id", userId).get().await()

                querySnapshot.isEmpty
            }
        } catch (e: Exception) {
            false
        }
    }
}