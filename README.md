# CS 501 Final Project: Pantry Pal - Smart Recipe Discovery


## Overview: Pantry Pal

Pantry Pal is a user-centric recipe discovery app designed to minimize food waste and simplify meal planning by leveraging ingredients users already have. Targeting cooking enthusiasts and beginners alike, the app combines practicality with playful interaction, empowering users to cook creatively while reducing unnecessary grocery purchases.

## Target Users & Goals

*   **Target Users:**
    *   Cooking beginners and enthusiasts seeking new dishes or ways to use existing ingredients.
    *   Environmentally conscious individuals aiming to reduce food waste.
*   **Project Goals:**
    *   Provide personalized recipe recommendations based on available ingredients.
    *   Enhance user engagement through interactive features like the "shake-shake" function.
    *   Offer convenient recipe storage and offline access.
    *   Help users manage kitchen inventory and create shopping lists, improving meal planning efficiency.

## Core Features 

1.  **Local Database for Data Persistence:**
    *   **Functionality:** Stores saved favorite recipes (Personal Cookbook) and user's pantry inventory locally.
    *   **Operations:** Supports CRUD (Create, Read, Update, Delete) operations for managing recipes and pantry items.
    *   **Benefit:** Enables offline access and quick retrieval of user data.
    *   **Implementation:** Room database with TypeConverters for complex data types and migration strategies.

2.  **External API Integration:**
    *   **API Used:** [Edamam Recipe API](https://developer.edamam.com/edamam-recipe-api)
    *   **Functionality:** Fetches recipes based on user-inputted ingredients.
    *   **Benefit:** Provides a vast library of recipes tailored to the user's available items.
    *   **Implementation:** Retrofit client with OkHttp caching for offline access to previously fetched recipes.

3.  **Onboard Sensor Integration:**
    *   **Accelerometer:** Powers the "Shake for Surprise" feature, offering a random recipe suggestion when the user shakes their phone, adding an element of fun.
    *   **Microphone:** Enables voice input for searching recipes or ingredients hands-free using Android's SpeechRecognizer.
    *   **Camera:** Barcode scanning feature to automatically update pantry inventory with ML Kit for barcode recognition.

4.  **Multi-Device Support & Optimization:**
    *   **Tested Devices:** Designed and tested for compatibility on both standard Android smartphones and tablets.
    *   **Responsive UI:** Utilizes Jetpack Compose's responsive design principles to adapt the layout dynamically. Phones display a compact view, while tablets offer an expanded multi-pane UI for enhanced usability.
    *   **Specific Adaptations:** Different layouts for portrait/landscape, custom composables that respond to window size classes.

5.  **Clean, Usable, and Delightful UI/UX (with Accessibility):**
    *   **UI Toolkit:** Built entirely with Jetpack Compose.
    *   **Design:** Follows Material Design 3 guidelines for consistency, providing smooth interactions, fluid animations (e.g., screen transitions), and intuitive navigation.
    *   **Accessibility:** Adheres to accessibility best practices, including:
        *   Support for screen readers with semantics API and contentDescription.
        *   WCAG AA-compliant color contrast (minimum 4.5:1) and readable typography.
        *   Text scaling compatibility with fontScale support.
        *   Dark mode support with dynamic theming.
        *   Alternative input methods (voice search).

## Technology Stack

*   **Language:** 
    * Kotlin 1.8.20
    * JDK 17
    * Kotlin DSL for build scripts

*   **UI Toolkit:** 
    * Jetpack Compose 1.5.0
    * Compose Navigation 2.7.0
    * Compose Material 3 1.1.1
    * Accompanist libraries 0.30.1 (systemuicontroller, permissions)

*   **Architecture:** MVVM + Clean Architecture + Unidirectional Data Flow
    *   **Presentation Layer:** 
        * Compose UI with StateHolders pattern
        * ViewModels with UiState/UiEvent pattern
        * Navigation using Compose Navigation with type-safe arguments
    *   **Domain Layer:** 
        * Pure Kotlin modules with no Android dependencies
        * Use Cases implementing single responsibility principle
        * Domain Models as immutable data classes
        * Repository Interfaces defining clear contracts
    *   **Data Layer:** 
        * Repository implementations following offline-first approach
        * Data Sources (API, Local DB) with clear separation
        * DTOs with mappers to domain models
        * Data source factories with fallback mechanisms

*   **State Management:** 
    * Kotlin StateFlow and SharedFlow
    * Immutable state objects
    * State hoisting pattern in Compose
    * Side-effect handling with Channel

*   **Asynchronous Programming:** 
    * Kotlin Coroutines 1.7.2
    * Structured concurrency with coroutineScope
    * Flow for reactive streams
    * Dispatchers.IO for blocking operations
    * Error handling with supervisorScope

*   **Database:**
    *   Room Database 2.6.0
        * Entities with relationships (1:1, 1:N, N:M)
        * DAO pattern with suspend functions
        * Migration strategies with versioned schemas
        * TypeConverters for complex data
        * FTS (Full-Text Search) for ingredient searching
    *   DataStore 1.0.0
        * Protocol Buffers for typed preferences
        * Preferences DataStore for simple key-value
        * Data migration from SharedPreferences

*   **Networking:**
    *   Retrofit 2.9.0
        * Custom call adapters for Flow integration
        * Custom converters for complex responses
        * Error handling interceptors
    *   OkHttp 4.11.0
        * Caching interceptors with time-based strategies
        * Logging interceptor for debugging
        * Timeout configurations
        * Certificate pinning for security
    *   Moshi 1.15.0
        * Custom type adapters
        * Kotlin codegen for reflection-free parsing
        * Null safety handling

*   **Dependency Injection:** 
    * Hilt 2.48
    * ViewModelInject for ViewModels
    * Module structure by feature and layer
    * Scoped bindings (@Singleton, @ActivityScoped)
    * Assisted Inject for factory pattern

*   **Image Loading:** 
    * Coil-Compose 2.4.0
    * Custom ImageLoader configuration
    * Disk cache with size limits
    * Crossfade animations
    * Placeholder and error handling


*   **Barcode Scanning:** 
    * ML Kit Barcode Scanning 18.2.0
    * CameraX 1.3.0 for camera integration
    * Custom analyzer for real-time scanning
    * Result verification with API lookup

*   **Design:** 
    * Material Design 3 implementation
    * Dynamic color adaptation
    * Custom theme with brand colors
    * Typography system with custom fonts
    * Adaptive layouts with WindowSizeClass

*   **Build System:**
    * Gradle 8.0
    * Version catalogs for dependency management
    * Custom build flavors (dev, staging, prod)
    * Custom Gradle plugins for code generation
    * Modular build configuration

## Project Structure

```
app/
├── build.gradle.kts             # App-level build with dependencies
├── src/
│   ├── main/
│   │   ├── java/com/cs501/pantrypal/
│   │   │   ├── di/                    # Dependency injection
│   │   │   │   ├── AppModule.kt       # Application-wide dependencies
│   │   │   │   ├── DatabaseModule.kt  # Database injection
│   │   │   │   ├── NetworkModule.kt   # API-related injection
│   │   │   │   └── RepositoryModule.kt # Repository bindings
│   │   │   ├── domain/                # Domain layer (pure business logic)
│   │   │   │   ├── model/             # Domain entities
│   │   │   │   │   ├── Recipe.kt
│   │   │   │   │   ├── Ingredient.kt
│   │   │   │   │   └── User.kt
│   │   │   │   ├── repository/        # Repository interfaces
│   │   │   │   │   ├── RecipeRepository.kt
│   │   │   │   │   └── PantryRepository.kt
│   │   │   │   └── usecase/           # Business logic
│   │   │   │       ├── recipe/
│   │   │   │       │   ├── GetRecipesByIngredientsUseCase.kt
│   │   │   │       │   └── SaveRecipeUseCase.kt
│   │   │   │       └── pantry/
│   │   │   │           ├── AddIngredientUseCase.kt
│   │   │   │           └── GetPantryItemsUseCase.kt
│   │   │   ├── data/                  # Data layer implementation
│   │   │   │   ├── local/             # Local data sources
│   │   │   │   │   ├── dao/           # Database Access Objects
│   │   │   │   │   │   ├── RecipeDao.kt
│   │   │   │   │   │   └── IngredientDao.kt
│   │   │   │   │   ├── entity/        # Room entities
│   │   │   │   │   │   ├── RecipeEntity.kt
│   │   │   │   │   │   └── IngredientEntity.kt
│   │   │   │   │   ├── typeconverter/  # Type converters
│   │   │   │   │   │   └── DateConverter.kt
│   │   │   │   │   └── PantryDatabase.kt # Room database
│   │   │   │   ├── remote/            # Remote data sources
│   │   │   │   │   ├── api/           # API interfaces
│   │   │   │   │   │   └── EdamamApi.kt
│   │   │   │   │   ├── dto/           # Data Transfer Objects
│   │   │   │   │   │   ├── RecipeDto.kt
│   │   │   │   │   │   └── IngredientDto.kt
│   │   │   │   │   └── mapper/        # DTO to domain mappers
│   │   │   │   │       └── RecipeMapper.kt
│   │   │   │   ├── repository/        # Repository implementations
│   │   │   │   │   ├── RecipeRepositoryImpl.kt
│   │   │   │   │   └── PantryRepositoryImpl.kt
│   │   │   │   └── preferences/       # User preferences
│   │   │   │       └── UserPreferencesManager.kt
│   │   │   ├── ui/                    # Presentation layer
│   │   │   │   ├── MainActivity.kt    # Entry point
│   │   │   │   ├── PantryPalNavHost.kt # Navigation setup
│   │   │   │   ├── components/        # Reusable UI components
│   │   │   │   │   ├── RecipeCard.kt
│   │   │   │   │   ├── IngredientItem.kt
│   │   │   │   │   └── LoadingIndicator.kt
│   │   │   │   ├── theme/             # App theming
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── screens/           # App screens
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   ├── pantry/
│   │   │   │   │   │   ├── PantryScreen.kt
│   │   │   │   │   │   └── PantryViewModel.kt
│   │   │   │   │   ├── search/
│   │   │   │   │   │   ├── SearchScreen.kt
│   │   │   │   │   │   └── SearchViewModel.kt
│   │   │   │   │   ├── recipe/
│   │   │   │   │   │   ├── RecipeDetailScreen.kt
│   │   │   │   │   │   └── RecipeDetailViewModel.kt
│   │   │   │   │   └── profile/
│   │   │   │   │       ├── ProfileScreen.kt
│   │   │   │   │       └── ProfileViewModel.kt
│   │   │   │   └── common/            # Common UI elements
│   │   │   │       ├── UiState.kt
│   │   │   │       └── UiEvent.kt
│   │   │   ├── util/                  # Utility classes
│   │   │   │   ├── extensions/        # Kotlin extensions
│   │   │   │   │   ├── FlowExt.kt
│   │   │   │   │   └── StringExt.kt
│   │   │   │   ├── Constants.kt       # App constants
│   │   │   │   └── NetworkUtils.kt    # Network helpers
│   │   │   └── PantryPalApplication.kt # Application class
│   │   ├── res/                       # Resources
│   │   │   ├── drawable/              # Images and icons
│   │   │   ├── values/               
│   │   │   │   ├── colors.xml        
│   │   │   │   ├── strings.xml       
│   │   │   │   └── themes.xml        
│   │   │   └── font/                  # Custom fonts
│   │   └── AndroidManifest.xml        # App manifest
│   ├── test/                         # Unit tests
│   │   └── java/com/cs501/pantrypal/
│   │       ├── domain/               # Domain tests
│   │       │   └── usecase/          
│   │       ├── data/                 # Data layer tests
│   │       │   ├── repository/       
│   │       │   └── remote/           
│   │       └── util/                 # Test utilities
│   └── androidTest/                  # Instrumentation tests
│       └── java/com/cs501/pantrypal/
│           ├── ui/                   # UI tests
│           └── data/                 # Integration tests
└── proguard-rules.pro               # ProGuard rules
```

## Detailed Setup Instructions


### 1. Environment Configuration

1. **Local Properties Setup:**
   Create a `local.properties` file in the project root:
   ```properties
   # SDK location (auto-generated by Android Studio)
   sdk.dir=/path/to/your/Android/sdk
   
   # API Keys
   EDAMAM_APP_ID="your_edamam_app_id"
   EDAMAM_APP_KEY="your_edamam_api_key"
   
   ```

2. **Google Services Configuration:**
   - Download `google-services.json` from Firebase console
   - Place in app/ directory
   - Enable ML Kit APIs in Firebase console:
     - Barcode scanning
     - On-device capabilities

### 3. Building the Project

1. **Gradle Configurations:**
   ```bash
   # Clean project (useful for resolving build issues)
   ./gradlew clean
   
   # Check for dependency updates
   ./gradlew dependencyUpdates
   
   # Run static analysis
   ./gradlew detekt
   
   # Build debug variant
   ./gradlew assembleDebug
   
   # Build release variant (unsigned)
   ./gradlew assembleRelease
   ```

2. **Troubleshooting Build Issues:**
   - Enable offline mode if network is unstable: `./gradlew --offline build`
   - Increase Gradle memory: Add `org.gradle.jvmargs=-Xmx4g -XX:MaxPermSize=512m` to gradle.properties
   - Clear caches: `./gradlew cleanBuildCache`

### 4. Running the Application

1. **Device Setup Options:**
   - **Physical Device:**
     - Enable USB debugging on device
     - Connect via USB
     - Grant debugging permissions
   
   - **Emulator:**
     - Create device with API 33+ in AVD Manager
     - Allocate at least 2GB RAM to emulator
     - Enable hardware acceleration

2. **Launch Application:**
   ```bash
   # Install and run on device
   ./gradlew installDebug && adb shell am start -n com.cs501.pantrypal.debug/com.cs501.pantrypal.ui.MainActivity
   
   # OR in Android Studio:
   # Select device and click Run
   ```

3. **Debug Mode Features:**
   - App includes special debug menu (shake device 3 times)
   - Network logging visible in debug builds
   - Debug database viewer accessible via notification

### 5. Testing the Application

1. **Manual Testing Checklist:**
   - Verify barcode scanning on physical products
   - Test offline functionality by enabling airplane mode
   - Validate responsive layouts on different devices
   - Check dark/light theme transitions

## Architectural Details

### Clean Architecture Implementation

1. **Entity Layer :**
   - Pure Kotlin data classes representing core business objects
   - No dependencies on any framework or library
   - Example: `Recipe`, `Ingredient`, `User`

2. **Use Case Layer:**
   - Implements application-specific business rules
   - Each use case represents one specific action
   - Depends only on entities layer
   - Example: `GetRecipesByIngredientsUseCase`, `SaveRecipeUseCase`

3. **Interface Adapter Layer:**
   - Converts data between use cases and external formats
   - Includes ViewModels, Repositories, and Data Mappers
   - Example: `RecipeViewModel`, `RecipeRepositoryImpl`, `RecipeMapper`

4. **Frameworks & Drivers Layer (Outermost):**
   - Contains frameworks and tools: UI, Database, Web APIs
   - Example: Compose UI components, Room Database, Retrofit client

### Architectural Patterns

#### 1. MVVM (Model-View-ViewModel)

We implement MVVM using Jetpack's ViewModel and Compose's state management:

```kotlin
// Example ViewModel implementation
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val getRecipesByIngredientsUseCase: GetRecipesByIngredientsUseCase,
    private val saveRecentSearchUseCase: SaveRecentSearchUseCase,
    private val sensorManager: SensorManager
) : ViewModel() {

    // UI State exposed as immutable state
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    // UI Events channel
    private val _uiEvents = Channel<SearchUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()
    
    // Handle user actions
    fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.SearchRecipes -> searchRecipes(action.ingredients)
            is SearchAction.FilterResults -> filterResults(action.filters)
            is SearchAction.SaveRecipe -> saveRecipe(action.recipeId)
            // ...
        }
    }
    
    private fun searchRecipes(ingredients: List<String>) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        getRecipesByIngredientsUseCase(ingredients)
            .onSuccess { recipes ->
                _uiState.update { 
                    it.copy(isLoading = false, recipes = recipes) 
                }
                saveRecentSearchUseCase(ingredients)
            }
            .onFailure { error ->
                _uiState.update { 
                    it.copy(isLoading = false, error = error.message) 
                }
            }
    }
    
    // Additional functions...
}

// Immutable UI State
data class SearchUiState(
    val isLoading: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val filters: RecipeFilters = RecipeFilters(),
    val error: String? = null
)

// UI Events (one-time events like navigation, toasts)
sealed class SearchUiEvent {
    data class NavigateToRecipeDetail(val recipeId: String) : SearchUiEvent()
    data class ShowToast(val message: String) : SearchUiEvent()
    // ...
}

// User actions
sealed class SearchAction {
    data class SearchRecipes(val ingredients: List<String>) : SearchAction()
    data class FilterResults(val filters: RecipeFilters) : SearchAction()
    data class SaveRecipe(val recipeId: String) : SearchAction()
    // ...
}
```

#### 2. Repository Pattern

Our repositories encapsulate data access logic and provide a clean API to the domain layer:

```kotlin
// Repository interface (in domain layer)
interface RecipeRepository {
    suspend fun getRecipesByIngredients(ingredients: List<String>): Result<List<Recipe>>
    suspend fun getRecipeById(id: String): Result<Recipe>
    suspend fun saveRecipe(recipe: Recipe): Result<Unit>
    suspend fun getSavedRecipes(): Flow<List<Recipe>>
    // ...
}

// Repository implementation (in data layer)
class RecipeRepositoryImpl @Inject constructor(
    private val recipeRemoteDataSource: RecipeRemoteDataSource,
    private val recipeLocalDataSource: RecipeLocalDataSource,
    private val networkManager: NetworkManager,
    private val recipeMapper: RecipeMapper
) : RecipeRepository {

    override suspend fun getRecipesByIngredients(ingredients: List<String>): Result<List<Recipe>> {
        // Offline-first approach
        return if (networkManager.isNetworkAvailable()) {
            try {
                // Get from remote source
                val remoteDtos = recipeRemoteDataSource.getRecipesByIngredients(ingredients)
                
                // Map DTOs to domain models
                val recipes = remoteDtos.map { recipeMapper.mapToDomain(it) }
                
                // Cache results locally
                recipeLocalDataSource.cacheRecipes(remoteDtos)
                
                Result.success(recipes)
            } catch (e: Exception) {
                // Fallback to local cache on error
                val localRecipes = recipeLocalDataSource.getRecipesByIngredients(ingredients)
                    .map { recipeMapper.mapToDomain(it) }
                    
                if (localRecipes.isNotEmpty()) {
                    Result.success(localRecipes)
                } else {
                    Result.failure(e)
                }
            }
        } else {
            // Offline mode - use only local cache
            val localRecipes = recipeLocalDataSource.getRecipesByIngredients(ingredients)
                .map { recipeMapper.mapToDomain(it) }
                
            if (localRecipes.isNotEmpty()) {
                Result.success(localRecipes)
            } else {
                Result.failure(IOException("No network connection and no cached data available"))
            }
        }
    }
    
    // Other repository methods...
}
```

#### 3. Use Case Pattern

Each use case implements a single responsibility and contains core business logic:

```kotlin
// Use case definition
class GetRecipesByIngredientsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val dietaryPreferencesRepository: DietaryPreferencesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(ingredients: List<String>): Result<List<Recipe>> = withContext(ioDispatcher) {
        // Get user's dietary preferences
        val preferences = dietaryPreferencesRepository.getUserDietaryPreferences()
        
        // Fetch recipes
        val recipesResult = recipeRepository.getRecipesByIngredients(ingredients)
        
        // Apply business rules
        recipesResult.map { recipes ->
            recipes
                .filter { recipe -> 
                    // Filter based on dietary preferences
                    recipe.matchesDietaryPreferences(preferences)
                }
                .sortedByDescending { recipe ->
                    // Sort by matching ingredient count
                    recipe.matchingIngredientCount(ingredients)
                }
        }
    }
}
```



### Key Architecture Benefits

1. **Separation of Concerns:**
   - Each layer has clear responsibilities
   - Domain layer contains business logic independent of UI and data concerns
   - Dependencies only point inward (domain layer has no external dependencies)

2. **Testability:**
   - Domain logic can be tested without UI or database dependencies
   - Use cases represent atomic business operations that can be tested in isolation
   - ViewModels can be tested using fake repositories

3. **Flexibility:**
   - UI framework can be changed without affecting business logic
   - Data sources can be swapped without impacting the rest of the app
   - New features can be added with minimal changes to existing code

4. **Maintainability:**
   - Clear boundaries between components
   - Consistent patterns throughout the codebase
   - Single responsibility principle applied at all levels


