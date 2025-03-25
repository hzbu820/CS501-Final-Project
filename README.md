# CS501-Final-Project

# Proposal：Pantry Pal-Smart Recipe Discovery App

## 1. Project Overview 
Pantry Pal is a user-centric recipe discovery app designed to minimize food waste and simplify meal planning by leveraging ingredients users already have. Targeting cooking enthusiasts and beginners alike, the app combines practicality with playful interaction, empowering users to cook creatively while reducing unnecessary grocery purchases.

## 2. Targets and Users
- **Target Users:**  
  - Cook beginner and enthusiasts who wants to learn cooking or new dishs 
  - Environmentalists who want to reduce food waste

- **Project Goal:**  
  - Provides personalized recipe recommendations using available ingredients 
  - Enhances fun and interactivity through the "shake-shake" function   
  - Provides convenient recipe storage and offline access 
  - Helps users manage kitchen inventory and shopping lists to improve meal planning efficiency

## 3. Core Features   
1. **Ingredient-Based Recipe Search**  
   Users can input or select ingredients (e.g., "chicken, tomato") to discover tailored recipes, encouraging cooking with what’s on hand rather than purchasing extra groceries.

2. **“Shake for Surprise” (Fun Sensor Integration)**  
   A playful feature where users shake their phone to receive a random recipe suggestion, powered by the accelerometer, making meal planning interactive and fun.

3. **Personal Cookbook (Local Save)**  
   Save favorite recipes locally for offline access, turning Pantry Pal into a convenient digital recipe book available anytime.

4. **Pantry & Grocery Helper**  
   Track pantry items within the app and, when viewing recipes, see what’s missing—adding needed ingredients to a grocery list for efficient shopping.

5. **Clean UI & Accessibility**  
   A simple, readable interface with proper contrast, dark mode, voice input search, and screen reader support ensures a delightful experience for all users.

6. **Potential Feature: AI recognition to update inventory**  
   Call up the device camera and update the food pantry in the app by taking photos of existing ingredients, which the AI then recognizes as text.

## 4. Technical Approach 
- **Database:**  
  - A local database stores saved recipes and pantry lists, enabling offline access and quick retrieval for the Personal Cookbook and Pantry & Grocery Helper features.

- **API：**  
  - Integrates with the Edamam Recipe API [Edamam Recipe API](https://developer.edamam.com/edamam-recipe-api)to fetch recipes based on user-entered ingredients.

- **Sensors:**  
  - **Accelerometer:** Detects phone shakes to trigger the "Shake for Surprise" feature.
  - **Microphone:** Supports voice input for hands-free recipe or ingredient searches.

## 5. Testing Strategy
- **Target Devices:**  
  - Pantry Pal is designed for multi-device compatibility, supporting both phones and tablets. The responsive UI adapts seamlessly, offering a compact layout on phones and an expanded dashboard on tablets for an optimal user experience across screen sizes.

## 6. Expected Outcomes  
- **Reduced Food Waste:**  
  -  Users utilize pantry staples efficiently, cutting unnecessary purchases.

- **Enhanced User Experience:**  
  - Engaging features (e.g., shake-to-discover) and intuitive design drive retention.


## 8. Initial Sketches
  - Recipe Search and Detail Page:
  ![Recipe_page](images/recipe_search-detail.jpg)

  - Cook Books and Grocery List Page:
  ![cook_book](images/cook_book-grocery_list.jpg)

  - User Profile Page:
  ![User_Profile](images/profile.jpg)

