package com.cs501.pantrypal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [UserIngredients::class, SavedRecipe::class, User::class, GroceryItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverter::class, IngredientListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userIngredientsDao(): UserIngredientsDao
    abstract fun savedRecipeDao(): SavedRecipeDao
    abstract fun userDao(): UserDao
    abstract fun groceryItemDao(): GroceryItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pantry_pal_database"
                )
                 //.fallbackToDestructiveMigration() //Add this if major version changes to clear the database
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}