package com.ekobits.demoapp.presentation.mvvm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekobits.demoapp.domain.usecase.GetUserUseCase
import com.ekobits.demoapp.domain.usecase.RefreshUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVVM (Model-View-ViewModel) Implementation
 *
 * In this pattern:
 * 1. Model: The Use Cases and Domain Models.
 * 2. View: The Composable functions that observe the ViewModel's state.
 * 3. ViewModel: Holds the UI state and handles business logic. It's lifecycle-aware.
 *
 * Data Flow:
 * - View calls ViewModel methods (e.g., `loadProfile`).
 * - ViewModel interacts with the Model (Use Case).
 * - ViewModel updates an observable State (StateFlow).
 * - View observes the state and automatically updates when it changes.
 *
 * Pros:
 * - Clear separation of concerns: ViewModel doesn't know about the View.
 * - Testability: ViewModel can be unit tested without UI.
 * - Lifecycle awareness: State is preserved across configuration changes.
 * - Reactive: UI reacts automatically to state changes.
 *
 * Cons:
 * - Complexity: Requires specialized classes and observation patterns.
 * - Overkill: Can be excessive for very simple screens.
 */

// 1. UI State
sealed class MvvmProfileUiState {
    object Loading : MvvmProfileUiState()
    data class Success(val name: String, val email: String) : MvvmProfileUiState()
    data class Error(val message: String) : MvvmProfileUiState()
}

// 2. ViewModel
@HiltViewModel
class UserProfileMvvmViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val refreshUserUseCase: RefreshUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MvvmProfileUiState>(MvvmProfileUiState.Loading)
    val uiState: StateFlow<MvvmProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            getUserUseCase(userId).collect { user ->
                if (user != null) {
                    _uiState.value = MvvmProfileUiState.Success(user.name, user.email)
                }
            }
        }

        viewModelScope.launch {
            try {
                refreshUserUseCase(userId)
            } catch (e: Exception) {
                if (_uiState.value is MvvmProfileUiState.Loading) {
                    _uiState.value = MvvmProfileUiState.Error("Failed to fetch user")
                }
            }
        }
    }

    fun refresh(userId: String) {
        viewModelScope.launch {
            try {
                refreshUserUseCase(userId)
            } catch (e: Exception) {
                _uiState.value = MvvmProfileUiState.Error("Refresh failed")
            }
        }
    }
}

// 3. View (Composable)
@Composable
fun UserProfileMvvmScreen(
    viewModel: UserProfileMvvmViewModel,
    userId: String
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.loadProfile(userId)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val s = state) {
            is MvvmProfileUiState.Loading -> CircularProgressIndicator()
            is MvvmProfileUiState.Error -> Text(text = s.message)
            is MvvmProfileUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(text = "Name: ${s.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email: ${s.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refresh(userId) }) {
                        Text("Refresh Profile (MVVM)")
                    }
                }
            }
        }
    }
}
