# Android Architectural Patterns Comparison

This repository demonstrates the same "User Profile" feature implemented using five different architectural patterns. This serves as a guide for understanding how data flow, state management, and separation of concerns vary across patterns.

## Architectures Implemented

### 1. MVC (Model-View-Controller)
- **Concept:** The Controller handles logic and updates the View directly. In modern Compose, the "Controller" logic is often embedded in the Composable or a simple state holder.
- **Data Flow:** View -> Controller (State update) -> View (Recomposition).
- **Best For:** Simple screens, prototypes, or very small projects.

### 2. MVP (Model-View-Presenter)
- **Concept:** The Presenter is an intermediary that interacts with the Model and updates the View via an interface.
- **Data Flow:** View -> Presenter -> Model -> Presenter -> View Interface.
- **Best For:** Projects where high testability of logic is required without relying on Android-specific classes like ViewModel.

### 3. MVVM (Model-View-ViewModel) - *Standard*
- **Concept:** The ViewModel holds observable state (StateFlow/LiveData). The View observes this state and reacts to changes.
- **Data Flow:** View -> ViewModel -> Model -> ViewModel (State update) -> View (Observation).
- **Best For:** Most modern Android apps. It provides a great balance between separation of concerns, lifecycle awareness, and ease of use.

### 4. MVI (Model-View-Intent)
- **Concept:** A unidirectional flow where user actions (Intents) lead to a single immutable State update through a Reducer.
- **Data Flow:** View (Intent) -> ViewModel/Reducer -> New State -> View (Observation).
- **Best For:** Complex UIs with many states, large teams, or projects requiring high predictability and "time-travel" debugging.

### 5. VIPER (View-Interactor-Presenter-Entity-Router)
- **Concept:** A highly modular approach where navigation (Router) and business logic (Interactor) are completely separated from the Presenter.
- **Data Flow:** View -> Presenter -> Interactor -> Model -> Interactor -> Presenter -> View Interface / Router.
- **Best For:** Enterprise-scale applications with very large teams and complex navigation requirements.

---

## High-Level Differences

| Feature | MVC | MVP | MVVM | MVI | VIPER |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Separation of Concerns** | Low | Medium | High | High | Very High |
| **Testability** | Low | High | High | Very High | Maximum |
| **Boilerplate** | Very Low | Medium | Medium | High | Very High |
| **Complexity** | Simple | Moderate | Moderate | Complex | High |
| **Unidirectional Flow** | No | No | Partial | Yes | No |
| **Lifecycle Aware** | No | No | Yes | Yes | No (requires extra work) |

## Implementation Details
Each implementation can be found in the `com.ekobits.demoapp.presentation` package. They all reuse the same `GetUserUseCase` and `RefreshUserUseCase` to demonstrate how Clean Architecture's Domain Layer remains consistent regardless of the presentation pattern chosen.
