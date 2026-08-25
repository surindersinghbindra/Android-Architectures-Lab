# Android Architectural Patterns Comparison

This repository demonstrates the same "User Profile" feature implemented using five different architectural patterns. This serves as a guide for understanding how data flow, state management, and separation of concerns vary across patterns.

---

## 🧠 Architectural Mental Model

To understand these architectures, we must first look at the "Mental Model" that underpins the entire project. We use a **Clean Architecture** core that remains unchanged, while only the "Face" (Presentation Layer) of the app changes for each pattern.

### 1. The Strategy (The Core)
The goal of this lab is to show that **Business Logic** is independent of **UI Patterns**. We use a shared **Domain Layer** and **Data Layer** for every implementation. This means the rules for *how we get a user* never change, only *how we show it* changes.

#### The Plan:
1. **MVC (Model-View-Controller)**: The "Classic" Android way. The Activity/Fragment acts as the Controller.
2. **MVP (Model-View-Presenter)**: Separation via an Interface. The Presenter handles logic and tells the View exactly what to do.
3. **MVVM (Model-View-ViewModel)**: Our current implementation. Reactive data flow using `StateFlow`.
4. **MVI (Model-View-Intent)**: Unidirectional Data Flow (UDF). "Intents" (actions) are sent to a reducer to produce a new "State".
5. **VIPER**: A highly modular approach (View, Interactor, Presenter, Entity, Router).


### 2. Shared Domain & Data Layers (The Foundation)
Regardless of whether we use MVC or VIPER, these layers are the "Source of Truth":
*   **Domain Layer**: Contains `UseCases` (The "What"). It defines the business actions like "Observe User" or "Refresh Profile". It has NO knowledge of Android, Retrofit, or Room.
*   **Data Layer**: Contains `Repositories` (The "How"). It handles the complexity of switching between the Network (Retrofit) and the Local Cache (Room).

### 3. Directory Structure
We organized the presentation layer into specific packages to make comparisons easy:

```text
com.ekobits.demoapp.presentation/
├── mvc/    # Tight coupling, Logic in UI
├── mvp/    # Interface-based decoupling
├── mvvm/   # Reactive, State-driven (Recommended)
├── mvi/    # Unidirectional, Action -> State
└── viper/  # Modular, Router-driven
```

### 4. Summary of Implementations
By looking at the same "User Profile" screen across these folders, you can observe how the **Responsibility** shifts:
*   In **MVC**, the UI is the "Boss".
*   In **MVP**, the Presenter is a "Micromanager" (telling the UI exactly what to do).
*   In **MVVM**, the ViewModel is a "Radio Station" (broadcasting state for the UI to tune into).
*   In **MVI**, the system is a "Factory Line" (Actions go in, States come out).
*   In **VIPER**, the system is an "Office Department" (Everyone has a very specific, narrow job).

#### Summary of Implementations

| Pattern | Location | Key Characteristic |
| :--- | :--- | :--- |
| **MVC** | `presentation.mvc` | The simplest. UI and logic are tightly coupled in the Composable/Activity. |
| **MVP** | `presentation.mvp` | Decoupled via an interface. The Presenter tells the View exactly what to display. |
| **MVVM** | `presentation.mvvm` | **Recommended**. Reactive and lifecycle-aware. Uses `StateFlow` to observe changes. |
| **MVI** | `presentation.mvi` | Unidirectional Data Flow. Best for complex state management and predictability. |
| **VIPER** | `presentation.viper` | Most modular. High separation of concerns, including navigation (Router) and logic (Interactor). |


---

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
