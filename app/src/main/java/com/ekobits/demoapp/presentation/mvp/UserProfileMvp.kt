package com.ekobits.demoapp.presentation.mvp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ekobits.demoapp.domain.usecase.GetUserUseCase
import com.ekobits.demoapp.domain.usecase.RefreshUserUseCase
import com.ekobits.demoapp.domain.model.User
import kotlinx.coroutines.*

/**
 * MVP (Model-View-Presenter) Implementation
 *
 * In this pattern:
 * 1. Model: The Use Cases and Domain Models (same as MVC).
 * 2. View: The UI interface (`ProfileView`). The Composable implements or interacts with this.
 * 3. Presenter: The intermediary that handles logic and updates the View.
 *
 * Data Flow:
 * - View calls Presenter methods (e.g., `loadProfile`).
 * - Presenter interacts with the Model (Use Case).
 * - Presenter receives data from Model.
 * - Presenter calls methods on the View interface to update UI (e.g., `showUser`, `showLoading`).
 *
 * Pros:
 * - Better separation of concerns: Presenter is decoupled from the Android framework.
 * - Testability: Presenter can be unit tested by mocking the View interface.
 * - Logic is isolated: Clear flow of data and actions.
 *
 * Cons:
 * - Boilerplate: Requires a View interface and extra code to bind/unbind.
 * - Fragile lifecycle: Presenter needs careful lifecycle management (attaching/detaching View).
 * - Bi-directional dependency: View knows Presenter, and Presenter knows View interface.
 */

// 1. View Interface
interface ProfileView {
    fun showLoading()
    fun showUser(user: User)
    fun showError(message: String)
}

// 2. Presenter
class ProfilePresenter(
    private val getUserUseCase: GetUserUseCase,
    private val refreshUserUseCase: RefreshUserUseCase,
    private val scope: CoroutineScope
) {
    private var view: ProfileView? = null

    fun attachView(view: ProfileView) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }

    fun loadProfile(userId: String) {
        view?.showLoading()
        scope.launch {
            getUserUseCase(userId).collect { user ->
                if (user != null) {
                    view?.showUser(user)
                } else {
                    view?.showError("User not found")
                }
            }
        }
        
        scope.launch {
            try {
                refreshUserUseCase(userId)
            } catch (e: Exception) {
                // Handle refresh error
            }
        }
    }
    
    fun refreshProfile(userId: String) {
        scope.launch {
            try {
                refreshUserUseCase(userId)
            } catch (e: Exception) {
                view?.showError("Failed to refresh")
            }
        }
    }
}

// 3. View Implementation (Composable)
@Composable
fun UserProfileMvpScreen(
    userId: String,
    getUserUseCase: GetUserUseCase,
    refreshUserUseCase: RefreshUserUseCase
) {
    var uiState by remember { mutableStateOf<MvpUiState>(MvpUiState.Loading) }
    val scope = rememberCoroutineScope()

    // Create presenter and handle lifecycle
    val presenter = remember {
        ProfilePresenter(getUserUseCase, refreshUserUseCase, scope)
    }

    DisposableEffect(presenter) {
        val viewImpl = object : ProfileView {
            override fun showLoading() { uiState = MvpUiState.Loading }
            override fun showUser(user: User) { uiState = MvpUiState.Success(user) }
            override fun showError(message: String) { uiState = MvpUiState.Error(message) }
        }
        presenter.attachView(viewImpl)
        presenter.loadProfile(userId)
        onDispose { presenter.detachView() }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = uiState) {
            is MvpUiState.Loading -> CircularProgressIndicator()
            is MvpUiState.Error -> Text(text = state.message)
            is MvpUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Name: ${state.user.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email: ${state.user.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { presenter.refreshProfile(userId) }) {
                        Text("Refresh Profile (MVP)")
                    }
                }
            }
        }
    }
}

sealed class MvpUiState {
    object Loading : MvpUiState()
    data class Success(val user: User) : MvpUiState()
    data class Error(val message: String) : MvpUiState()
}
