package com.ekobits.demoapp.presentation.viper

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
 * VIPER (View-Interactor-Presenter-Entity-Router) Implementation
 *
 * In this pattern:
 * 1. View: The UI (Composable and `ViperProfileView` interface).
 * 2. Interactor: Contains business logic (wraps Use Cases).
 * 3. Presenter: Orchestrates the Interactor, Router, and View.
 * 4. Entity: Plain data objects (Domain Models like `User`).
 * 5. Router: Handles navigation between screens.
 *
 * Data Flow:
 * - View sends user actions to the Presenter.
 * - Presenter asks the Interactor to fetch/process data.
 * - Interactor calls Use Cases (Model) and returns data to the Presenter.
 * - Presenter updates the View via its interface.
 * - If navigation is needed, Presenter calls the Router.
 *
 * Pros:
 * - Maximum separation of concerns: Every component has a single responsibility.
 * - Highly testable: Every component can be tested in isolation.
 * - Maintainable: Easy to swap implementations (e.g., change the Router).
 * - Ideal for large teams: Work can be divided by component.
 *
 * Cons:
 * - Extremely high boilerplate: Requires many classes and interfaces for even simple screens.
 * - Complexity: Hard to follow the flow of data through so many layers.
 * - Learning curve: Steep for developers unfamiliar with the pattern.
 * - Over-engineered for most Android apps.
 */

// 1. View Interface
interface ViperProfileView {
    fun showLoading()
    fun showUser(user: User)
    fun showError(message: String)
}

// 2. Interactor (Wraps Business Logic)
class UserProfileInteractor(
    private val getUserUseCase: GetUserUseCase,
    private val refreshUserUseCase: RefreshUserUseCase
) {
    fun getUserStream(userId: String) = getUserUseCase(userId)
    suspend fun refreshUser(userId: String) = refreshUserUseCase(userId)
}

// 3. Router (Handles Navigation)
interface ProfileRouter {
    fun navigateToEditProfile(userId: String)
    fun goBack()
}

// 4. Presenter
class UserProfileViperPresenter(
    private val interactor: UserProfileInteractor,
    private val router: ProfileRouter,
    private val scope: CoroutineScope
) {
    private var view: ViperProfileView? = null

    fun attachView(view: ViperProfileView) {
        this.view = view
    }

    fun detachView() {
        this.view = null
    }

    fun loadProfile(userId: String) {
        view?.showLoading()
        scope.launch {
            interactor.getUserStream(userId).collect { user ->
                if (user != null) {
                    view?.showUser(user)
                } else {
                    view?.showError("User not found")
                }
            }
        }
        
        scope.launch {
            try {
                interactor.refreshUser(userId)
            } catch (e: Exception) {
                // Silent background error
            }
        }
    }

    fun onRefreshClicked(userId: String) {
        scope.launch {
            try {
                interactor.refreshUser(userId)
            } catch (e: Exception) {
                view?.showError("Refresh failed")
            }
        }
    }

    fun onEditClicked(userId: String) {
        router.navigateToEditProfile(userId)
    }
}

// 5. View Implementation (Composable)
@Composable
fun UserProfileViperScreen(
    userId: String,
    interactor: UserProfileInteractor,
    router: ProfileRouter
) {
    var state by remember { mutableStateOf<ViperUiState>(ViperUiState.Loading) }
    val scope = rememberCoroutineScope()
    
    val presenter = remember {
        UserProfileViperPresenter(interactor, router, scope)
    }

    DisposableEffect(presenter) {
        val viewImpl = object : ViperProfileView {
            override fun showLoading() { state = ViperUiState.Loading }
            override fun showUser(user: User) { state = ViperUiState.Success(user) }
            override fun showError(message: String) { state = ViperUiState.Error(message) }
        }
        presenter.attachView(viewImpl)
        presenter.loadProfile(userId)
        onDispose { presenter.detachView() }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val s = state) {
            is ViperUiState.Loading -> CircularProgressIndicator()
            is ViperUiState.Error -> Text(text = s.message)
            is ViperUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Name: ${s.user.name}", style = MaterialTheme.typography.headlineMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Email: ${s.user.email}", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        Button(onClick = { presenter.onRefreshClicked(userId) }) {
                            Text("Refresh (VIPER)")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { presenter.onEditClicked(userId) }) {
                            Text("Edit")
                        }
                    }
                }
            }
        }
    }
}

sealed class ViperUiState {
    object Loading : ViperUiState()
    data class Success(val user: User) : ViperUiState()
    data class Error(val message: String) : ViperUiState()
}
