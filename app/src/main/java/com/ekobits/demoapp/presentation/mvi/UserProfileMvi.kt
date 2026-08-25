package com.ekobits.demoapp.presentation.mvi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ekobits.demoapp.domain.usecase.GetUserUseCase
import com.ekobits.demoapp.domain.usecase.RefreshUserUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * MVI (Model-View-Intent) Implementation
 *
 * In this pattern:
 * 1. Intent (Action): Represents a user's intention (e.g., `LoadProfile`, `Refresh`).
 * 2. State: A single immutable source of truth for the UI state.
 * 3. Model: The logic that takes an Intent and produces a new State (Reducer).
 * 4. View: Observes the single State and sends Intents back to the ViewModel.
 *
 * Data Flow:
 * - View sends an **Intent** to the ViewModel.
 * - ViewModel processes the Intent using the Model/Use Cases.
 * - ViewModel reduces the result into a new **State**.
 * - View observes the single **State** stream and updates the UI.
 *
 * Pros:
 * - Single source of truth: UI state is predictable and easy to debug.
 * - Unidirectional data flow: Clear path for data and actions.
 * - Time-travel debugging: Possible because every state change is recorded as a transition.
 * - Thread safety: Immutable state prevents race conditions in UI.
 *
 * Cons:
 * - High boilerplate: Requires defining Intents, States, and a Reducer.
 * - Complexity: Can be over-engineered for simple UIs.
 * - Memory usage: Many small State objects may be created (though usually negligible).
 */

// 1. Intent (Action)
sealed class ProfileIntent {
    data class LoadProfile(val userId: String) : ProfileIntent()
    data class RefreshProfile(val userId: String) : ProfileIntent()
}

// 2. State
data class MviProfileState(
    val isLoading: Boolean = false,
    val name: String = "",
    val email: String = "",
    val error: String? = null
)

// 3. ViewModel (acting as the processor/reducer)
class UserProfileMviViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val refreshUserUseCase: RefreshUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MviProfileState())
    val state: StateFlow<MviProfileState> = _state.asStateFlow()

    fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile(intent.userId)
            is ProfileIntent.RefreshProfile -> refreshProfile(intent.userId)
        }
    }

    private fun loadProfile(userId: String) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            getUserUseCase(userId).collect { user ->
                if (user != null) {
                    _state.update { it.copy(isLoading = false, name = user.name, email = user.email, error = null) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "User not found") }
                }
            }
        }
        refreshProfile(userId) // Auto-refresh on load
    }

    private fun refreshProfile(userId: String) {
        viewModelScope.launch {
            try {
                refreshUserUseCase(userId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Refresh failed") }
            }
        }
    }
}

// 4. View (Composable)
@Composable
fun UserProfileMviScreen(
    viewModel: UserProfileMviViewModel,
    userId: String
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.handleIntent(ProfileIntent.LoadProfile(userId))
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else if (state.error != null) {
            Text(text = state.error!!)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Name: ${state.name}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Email: ${state.email}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.handleIntent(ProfileIntent.RefreshProfile(userId)) }) {
                    Text("Refresh Profile (MVI)")
                }
            }
        }
    }
}
