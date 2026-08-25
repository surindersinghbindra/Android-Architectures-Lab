package com.ekobits.demoapp.presentation.mvc

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekobits.demoapp.domain.usecase.GetUserUseCase
import com.ekobits.demoapp.domain.usecase.RefreshUserUseCase
import com.ekobits.demoapp.domain.model.User
import kotlinx.coroutines.launch

/**
 * MVC (Model-View-Controller) Implementation
 *
 * In this pattern for Jetpack Compose:
 * 1. Model: Represents the data layer (Use Cases and Domain Models).
 * 2. View: The Composable functions that define the UI.
 * 3. Controller: In this basic MVC approach, the logic for managing state and handling
 *    user interactions is placed directly within the View (the Composable) or a simple
 *    state holder.
 *
 * Data Flow:
 * - The View triggers an action (e.g., LaunchedEffect to load data).
 * - The Controller logic (within the Composable) calls the Model (Use Case).
 * - The Model returns data.
 * - The Controller updates the internal state (`mutableStateOf`).
 * - The View recomposes based on the updated state.
 *
 * Pros:
 * - Simple for very small screens or prototypes.
 * - Minimal boilerplate (no separate ViewModel or Presenter classes).
 * - Fast to implement initially.
 *
 * Cons:
 * - Poor separation of concerns: UI and Business logic are tightly coupled.
 * - Hard to test: Logic is inside the Composable, making unit testing difficult.
 * - State loss: State is tied to the Composable lifecycle (though `rememberSaveable` can help).
 * - Not scalable: As complexity grows, the Composable becomes bloated and hard to maintain.
 */

@Composable
fun UserProfileMvcScreen(
    userId: String,
    getUserUseCase: GetUserUseCase,
    refreshUserUseCase: RefreshUserUseCase
) {
    // State managed directly in the "Controller" part of the View
    var user by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()

    // Controller logic to fetch data
    LaunchedEffect(userId) {
        isLoading = true
        getUserUseCase(userId).collect { result ->
            if (result != null) {
                user = result
                isLoading = false
            } else {
                error = "User not found"
                isLoading = false
            }
        }
    }
    
    // Controller logic to refresh data
    LaunchedEffect(userId) {
        try {
            refreshUserUseCase(userId)
        } catch (e: Exception) {
            // Handle error silently or update state
        }
    }

    // View: UI definition
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            isLoading -> CircularProgressIndicator()
            error != null -> Text(text = error!!)
            user != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Name: ${user?.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email: ${user?.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        scope.launch {
                            refreshUserUseCase(userId)
                        }
                    }) {
                        Text("Refresh Profile")
                    }
                }
            }
        }
    }
}
