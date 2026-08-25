# Android Architectures Lab - Multi-Pattern Implementation Explorer

This project demonstrates a comprehensive implementation of various Android Architectural patterns (MVC, MVP, MVVM, MVI, VIPER) using Jetpack Compose, Room, Retrofit, and Hilt.

## 📚 Architectural Overview

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

---

## 🛠 Lessons Learned & Implementation Notes

During the development of the User Profile feature, several critical "Senior Developer" challenges were encountered and solved. These serve as a reference for best practices in professional Android development.

### 1. Clean Architecture Responsibilities
*   **Repository vs. UseCase Mapping**:
    *   **Repository**: Responsible for **Data → Domain** mapping (e.g., `UserDto` to `User`). This ensures the Domain layer is completely decoupled from framework-specific models.
    *   **UseCase**: Responsible for **Business Logic**.

#### Examples of Use Cases in Action:
1.  **Business Logic: "The Member Status Rule"**: If a user's email ends in `@ekobits.com`, they are an "Internal Member." The Use Case calculates this before passing the data to the ViewModel.
    ```kotlin
    class GetEnhancedUserUseCase(private val repository: UserRepository) {
        operator fun invoke(userId: String): Flow<EnhancedUser?> {
            return repository.getUserStream(userId).map { user ->
                user?.let {
                    EnhancedUser(it, if (it.email.endsWith("@ekobits.com")) "Admin" else "Guest")
                }
            }
        }
    }
    ```
2.  **Orchestration: Combining Two Sources**: A Use Case can combine a `User` profile from one repository with `OrderHistory` from another to create a single "FullProfile" domain model.
    ```kotlin
    class GetFullProfileUseCase(private val userRepo: UserRepository, private val orderRepo: OrderRepository) {
        operator fun invoke(userId: String): Flow<FullProfile> {
            return combine(userRepo.getUserStream(userId), orderRepo.getOrders(userId)) { user, orders ->
                FullProfile(user, orders)
            }
        }
    }
    ```
3.  **Validation & Guardrails**: Before refreshing a user, a Use Case can check if the phone number is valid (e.g., 10 digits).
    ```kotlin
    class RefreshUserUseCase(private val repository: UserRepository) {
        suspend operator fun invoke(phoneNumber: String): Result<Unit> {
            if (phoneNumber.length < 10) return Result.failure(Exception("Invalid Phone"))
            return repository.refreshUser(phoneNumber)
        }
    }
    ```

#### 💡 Summary: Why bother with Use Cases?
*   **Reusability**: If you add a "Settings" screen that also needs to know if the user is an "Admin," you just reuse `GetEnhancedUserUseCase`. You don't rewrite the `if` statement.
*   **Testing**: You can write a Unit Test for a Use Case that checks 10 different email formats without ever launching an emulator or a Database.
*   **Thin ViewModels**: Your ViewModel stays focused on UI State (loading, success, error) and doesn't get cluttered with calculation logic.

*   **Main-Safety**: Even though Retrofit and Room handle dispatchers internally, a Repository should explicitly use `withContext(Dispatchers.IO)` to guarantee that every operation is safe to call from the Main thread.

### 2. Handling Android Framework Components
To understand how complex components fit, use the **Architectural Mental Model**:
*   **Domain is the "Brain"** (Pure Kotlin): It makes decisions but doesn't know how to move.
*   **Data is the "Body"** (Sensors/Storage/Network): It handles the physical world (BLE, Retrofit, Room, MMKV, gRPC, WebSockets).
*   **Presentation is the "Face"** (UI): It shows what the brain is thinking.

When you add Android-specific components, here is where they belong in Clean Architecture:

#### 1. Bluetooth & BLE (Data Layer)
Bluetooth is just another "Data Source," similar to an API or a Database.
*   **Domain**: You define an interface like `BluetoothRepository` with methods like `scanForDevices()` or `connect()`.
*   **Data**: You implement this interface using the Android Bluetooth Stack. All the "messy" BLE callbacks and GATT logic stay here.
*   **Why**: If Android releases a new Bluetooth API tomorrow, you only change the Data layer. Your Use Cases and UI stay exactly the same.

#### 2. Permissions (Presentation & Data)
Permissions are tricky because they bridge the UI and the System.
*   **Presentation (Activity/ViewModel)**: This is where you **request** the permission (showing the system dialog) because you need an Activity context.
*   **Data (PermissionProvider)**: You can create a `PermissionChecker` implementation in the Data layer that simply returns a `Boolean` (`isPermissionGranted`).
*   **Domain**: You define a `CheckPermissionUseCase` that your app uses to decide if it should even try to start the Bluetooth scan.

#### 3. Foreground Services (Data/Infrastructure Layer)
A Service is an Android Framework component.
*   **Placement**: It belongs in the Data Layer (or a dedicated Infrastructure module).
*   **Logic**: The Service itself should be "dumb." It shouldn't contain business logic. Instead, the Service should inject and call Use Cases.
*   **Example**: A Music Player Service receives a "Play" command. It calls `PlayMusicUseCase`. The Use Case handles the logic, and the Service just handles the Android notification and the media player lifecycle.

#### 4. Broadcast Receivers (Data Layer)
Broadcast Receivers are "Events" from the outside world.
*   **Placement**: Data Layer.
*   **Flow**: The Receiver listens for a system event (like `ConnectivityChanged` or `SmsReceived`). It then pushes that data into a `Flow` or `Channel` defined in a Repository.
*   **Example**: A `SmsRepository` implementation has a Broadcast Receiver inside it. When an SMS arrives, the repository emits that SMS through a `Flow<Sms>` which the Domain (Use Case) is collecting.

#### 📁 Revised Folder Structure for Complex Apps
```text
app (Presentation)
 ├── viewmodels/
 ├── ui/ (Composables)
 └── activities/ (Permission Request Logic)

domain (Pure Kotlin)
 ├── model/ (User, Device, ScanResult)
 ├── repository/ (UserRepository, BluetoothRepository)
 └── usecase/ (ConnectToDeviceUseCase, GetUserUseCase)

data (Android Framework Heavy)
 ├── remote/ (Retrofit)
 ├── local/ (Room)
 ├── bluetooth/ 
 │    ├── BleScannerImpl.kt (Handles BLE logic)
 │    └── BluetoothRepositoryImpl.kt
 ├── services/ 
 │    └── BackgroundSyncService.kt (Calls UseCases)
 └── receivers/
      └── BootReceiver.kt
```

> [!IMPORTANT]
> **Key Takeaway for an Interview:**
> If you are asked where a "Service" goes, the senior answer is:
> *"The Service is an Android entry point, much like an Activity. It belongs in the Framework/Data layer. However, it should stay logic-free and simply delegate work to the Domain Layer (Use Cases). This ensures that even background work follows our business rules."*

### 3. Advanced Debugging in Android Studio
*   **Screen Recomposition**: Use **Layout Inspector > Show Recomposition Counts** to identify performance bottlenecks in Compose.
*   **Reactive Stream Probes**: When debugging fast-moving Flows, use a "History Logger" (recording emissions in a list) or a "Coroutine Brake" (`delay(100)`) to visualize the exact sequence of state changes without the debugger merging values.

### 4. Reactive Data Flow & Room (The "Phantom Null" Issue)
*   **The Problem**: In an Offline-First setup, we observed that the UI would briefly show a "User Not Found" error even when the data was present.
*   **The Cause**: Room's `@Insert(REPLACE)` strategy is actually a `DELETE` followed by an `INSERT`. During the microsecond between these operations, Room's `InvalidationTracker` triggers a `null` emission to the Flow.
*   **The Solution**: 
    1.  Use `.distinctUntilChanged()` in the Repository to filter out redundant state updates.
    2.  Update the UI State Machine (ViewModel) to treat a `null` from the database as a "Transient Loading" state rather than a permanent error, until the network refresh confirms a failure.

---

## 🚀 Technical Stack
- **UI**: Jetpack Compose
- **Concurrency**: Kotlin Coroutines & Flow
- **Dependency Injection**: Hilt
- **Networking**: Retrofit & OKHttp
- **Local Storage**: Room Persistence Library
