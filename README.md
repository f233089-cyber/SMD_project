# RecipeHub - SMD Semester Project (Assignment 3 + Assignment 4)

RecipeHub is an Android semester project designed for the uploaded SMD Assignment #03 and #04 requirements. It uses a single evolving codebase: Assignment #03 features are implemented as the local/API phase, and Assignment #04 features extend the same project with Firebase, notifications, Jetpack Compose, and two self-researched features.

## Project idea

A recipe discovery and meal-planning app:

- Users explore meals from TheMealDB public REST API.
- Users save and manage meals locally using SQLiteOpenHelper.
- Users search, filter, and sort their saved meals using SQL queries.
- Users sign in using Firebase Authentication.
- Users sync cloud meal plans in real time using Cloud Firestore.
- Users receive FCM/local notifications.
- A profile/dashboard screen is built using Jetpack Compose.
- Two additional researched features are included: WorkManager reminders and Android Photo Picker.

## Important setup before running

1. Open this folder in Android Studio.
2. Create a Firebase project and add an Android app with package name:
   `com.smd.recipehub`
3. Download `google-services.json` from Firebase Console and place it inside:
   `app/google-services.json`
4. Enable Firebase Authentication providers:
   - Email/Password
   - Google
5. Add your Web Client ID to `local.properties` in the project root:

```properties
WEB_CLIENT_ID=your_firebase_web_client_id_here
```

6. Create Firestore Database in test mode first, then replace with the included rules in `firebase/firestore.rules`.
7. Sync Gradle and run the app.

## Assignment #03 mapping

- F1 REST API: `MealApiService.kt`, `RetrofitClient.kt`, `ApiMealsFragment.kt`
- F2 SQLite schema: `DatabaseHelper.kt`
- F3 CRUD: `SavedMealDao.kt`, `SavedMealsFragment.kt`
- F4 Integration strategy: API meals can be saved into SQLite; API and local modules also work separately.
- F5 Dynamic SQL: `SavedMealDao.querySavedMeals()` uses `LIKE` and safe `ORDER BY` clauses.

## Assignment #04 mapping

- F1 Firebase Auth: `AuthRepository.kt`, `AuthActivity.kt`
- F2 Firestore real-time data: `FirestoreRepository.kt`, `CloudPlansFragment.kt`
- Push Notifications: `RecipeFirebaseMessagingService.kt`, `NotificationHelper.kt`
- Jetpack Compose: `ProfileComposeFragment.kt`, `ProfileScreen.kt`
- Self-researched feature #1: `DailyRecipeReminderWorker.kt` using WorkManager
- Self-researched feature #2: Android Photo Picker inside `ProfileComposeFragment.kt`

## Suggested GitHub workflow

```bash
git init
git add .
git commit -m "Initial RecipeHub semester project scaffold"
git checkout -b feature/a3-rest-api
# commit API work
git checkout -b feature/a3-sqlite-crud
# commit SQLite work
git checkout -b feature/a4-firebase-auth
# commit auth work
git checkout -b feature/a4-firestore-sync
# commit Firestore work
git checkout -b feature/a4-compose-notifications
# commit Compose and notification work
```

For final submission, push to GitHub, open Pull Requests from each branch into `main`, and add peer review comments.

## Note for students

Use this as your base project and customize package text, UI colors, screenshots, roll numbers, Firebase project, GitHub commits, and viva understanding before submission.
