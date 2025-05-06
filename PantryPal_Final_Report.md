# Pantry Pal: Smart Recipe Discovery App - Final Report

## 1. App Features and Design Decisions

### Core Features

**1. Ingredient-Based Recipe Search**  
Pantry Pal enables users to discover recipes based on ingredients they already have. This primary feature helps reduce food waste and simplifies meal planning. Users can input up to five ingredients, and the app retrieves recipes that use these ingredients through the Edamam Recipe API integration.

**Design Decision:** We implemented a flexible multi-ingredient input system that allows users to add and remove ingredients dynamically, with a capped maximum of five ingredients to keep the UI clean and the API calls efficient. This strikes a balance between flexibility and usability.

**2. "Shake for Surprise" Feature**  
This playful feature utilizes the device's accelerometer sensor to provide random recipe suggestions when the user shakes their phone. It adds an element of fun and serendipity to the meal planning process.

**Design Decision:** We implemented a `ShakeSensorManager` class to detect shake gestures with adjustable sensitivity. We included a cooldown period between shakes to prevent accidental triggers and added a visual feedback system via Snackbar notifications to confirm the action was recognized.

**3. Personal Cookbook (Local Save)**  
Users can save favorite recipes locally for offline access, organizing them in a digital cookbook that's available anytime without requiring internet connectivity.

**Design Decision:** We chose a Room database implementation for local storage, using a clearly defined entity structure for saved recipes with efficient query methods. We implemented a visually appealing grid layout for the cookbook view that adapts to different screen sizes.

**4. Pantry & Grocery Helper**  
This feature allows users to track pantry items and automatically generate shopping lists based on missing ingredients for recipes they want to try.

**Design Decision:** We integrated inventory management with recipe exploration by cross-referencing pantry items against recipe ingredients. The grocery list feature includes CRUD operations with intuitive swipe-to-delete functionality and checkboxes for shopping convenience.

**5. Multi-Device Support with Responsive UI**  
The app provides distinct layouts optimized for both phones and tablets, ensuring a great user experience across different device types.

**Design Decision:** We implemented conditional UI rendering using Jetpack Compose's responsive design principles. The app detects screen size and adjusts the layout accordingly—phones get a vertical, stack-based UI, while tablets utilize a multi-pane layout that takes advantage of the additional screen real estate.

### UI/UX Design Philosophy

Our design approach focused on creating a clean, intuitive interface that emphasizes content and functionality without overwhelming users. Key UI/UX design decisions include:

1. **Material Design Implementation:** Adhering to Material Design 3 principles for consistent visual language and behavior
2. **Accessibility Considerations:** Supporting screen readers, maintaining proper contrast ratios, implementing dark mode, and providing text scaling compatibility
3. **Progressive Disclosure:** Presenting information in stages to avoid cognitive overload
4. **Visual Feedback:** Incorporating loading indicators, transition animations, and clear error states
5. **Intuitive Navigation:** Using a bottom navigation bar for primary sections with logical screen flows

## 2. Architecture and Technologies Used

### Architecture Pattern: MVVM

We implemented the Model-View-ViewModel (MVVM) architecture pattern for several key reasons:

1. **Separation of Concerns:** The clear separation between UI (View), data logic (Model), and UI logic (ViewModel) made the codebase more maintainable and testable
2. **UI State Management:** ViewModels preserve UI state during configuration changes
3. **Testability:** Business logic in ViewModels can be unit tested independent of Android framework components
4. **Jetpack Compose Integration:** MVVM pairs naturally with Compose's declarative UI paradigm

The architecture consists of:
- **Model:** Database entities and network response objects
- **View:** Composable functions that render UI elements
- **ViewModel:** State holders that coordinate data flow and transform it for display

### Technology Stack & Libraries

| Technology/Library | Purpose | Justification |
|-------------------|---------|---------------|
| **Kotlin** | Primary programming language | Modern features, coroutines support, null safety |
| **Jetpack Compose** | UI framework | Declarative UI design, faster development, built-in animations |
| **Room Database** | Local data persistence | Type safety, SQL abstraction, integration with Kotlin coroutines |
| **Retrofit** | Network API communication | Type-safe API calls, coroutine support, JSON parsing |
| **Coroutines & Flow** | Asynchronous programming | Clean async code without callbacks, lifecycle awareness |
| **Coil** | Image loading | Kotlin-first image loading, Compose integration, efficient caching |
| **Navigation Component** | App navigation | Type-safe navigation with arguments, deep link support |
| **ViewModel** | UI state management | Lifecycle awareness, state preservation |
| **Material 3** | Design system | Consistent visual language, pre-built components |
| **Firebase Auth** | User authentication | Secure, scalable identity management |

### Data Flow Architecture

Our data flow follows a repository pattern that centralizes data access logic:

1. **UI Layer:** Composables observe ViewModel states
2. **ViewModel Layer:** Coordinates data operations through repositories
3. **Repository Layer:** Abstracts data sources and provides a clean API to ViewModels
4. **Data Sources:** Room database (local) and Retrofit API client (remote)

This architecture helps maintain a unidirectional data flow, making it easier to track the origin of data changes and debug the application.

## 3. Challenges Faced and Solutions

### Challenge 1: Responsive UI for Different Screen Sizes

**Problem:** Creating a UI that works well on both phone and tablet form factors without duplicating code.

**Solution:** We implemented a responsive design system using Jetpack Compose that:
- Detects screen size at runtime using `LocalConfiguration`
- Conditionally renders different layouts based on screen width (threshold set at 600dp)
- Uses shared component functions with adaptive parameters
- Maintains a consistent component library across form factors

This approach allowed us to reuse much of our UI code while still providing optimal experiences for each device type.

### Challenge 2: Shake Detection Reliability

**Problem:** Initial shake detection was either too sensitive (triggering accidentally) or not sensitive enough (requiring excessive force).

**Solution:** We implemented a customizable sensitivity threshold and debounce mechanism in the `ShakeSensorManager` class. We:
- Added a configurable shake threshold (initially set to 11.5f)
- Implemented a time-based filter to prevent multiple detections within 1000ms
- Calculated acceleration using a delta between previous and current readings
- Used vector magnitude from all three axes to accurately detect shakes from any direction

These improvements created a much more reliable and satisfying shake detection experience.

### Challenge 3: API Rate Limiting

**Problem:** The Edamam API has strict rate limits, leading to frequent quota errors during development and testing.

**Solution:** We implemented several strategies to mitigate API limitations:
- Created a custom caching layer to store recent API responses locally
- Added a "debounce" to search inputs to limit the frequency of API calls
- Implemented error handling to gracefully display rate limit errors to users
- Added offline fallbacks using previously cached results when possible
- Prioritized user-triggered searches over automatic refreshes

These measures significantly reduced the number of API calls while maintaining a responsive user experience.

### Challenge 4: Data Synchronization

**Problem:** Maintaining consistency between the local database and in-memory state, especially with multiple ViewModels potentially accessing the same data.

**Solution:** We implemented a centralized data repository pattern with:
- Single source of truth for each data type
- Kotlin Flow to emit updates to interested components
- Clear separation between read and write operations
- Transaction support for related database operations
- Consistent error handling across data operations

This approach ensured data consistency throughout the app and simplified the debugging process when data-related issues occurred.

## 4. User Testing and Feedback

We conducted user testing sessions with 10 individuals representing our target demographic. Here's a summary of their feedback and our proposed improvements:

### Positive Feedback

1. **Intuitive Interface:** 9/10 users found the app easy to navigate without instruction
2. **Shake Feature:** 8/10 users enjoyed the "shake for surprise" feature, finding it novel and engaging
3. **Recipe Variety:** All users appreciated the diversity of recipes available
4. **Offline Access:** Users valued being able to access saved recipes without an internet connection
5. **Responsive Design:** Tablet users specifically praised the multi-pane layout as efficient and well-organized

### Areas for Improvement

1. **Search Refinement:** 6/10 users requested more filtering options (dietary restrictions, cuisine type, meal type)
   - **Planned Solution:** Add advanced filtering options in the search screen with collapsible filter panels

2. **Ingredient Scanning:** 7/10 users suggested adding barcode/text scanning for easier pantry management
   - **Planned Solution:** Implement camera integration with OCR and barcode recognition in a future update

3. **Recipe Scaling:** 4/10 users wanted the ability to scale recipes for different serving sizes
   - **Planned Solution:** Add a serving size adjustment feature that recalculates ingredient quantities

4. **Cooking Mode:** 5/10 users requested a dedicated cooking mode with step-by-step instructions and timers
   - **Planned Solution:** Create a distraction-free cooking mode with large text, screen-wake lock, and integrated timers

5. **Social Sharing:** 3/10 users wanted to share recipes with friends and family
   - **Planned Solution:** Implement sharing functionality using Android's share sheet API

### Implementation Priorities Based on Feedback

Based on user feedback frequency and development complexity, we've prioritized these improvements:

1. **High Priority:** Advanced filtering options (dietary, cuisine type)
2. **Medium Priority:** Recipe scaling and cooking mode
3. **Medium Priority:** Social sharing functionality
4. **Long-term Goal:** Ingredient scanning using camera and OCR

