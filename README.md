# DemoApp - Modern Android Architecture & Learning Lab

This project demonstrates a comprehensive implementation of various Android Architectural patterns (MVC, MVP, MVVM, MVI, VIPER) using Jetpack Compose, Room, Retrofit, and Hilt.

## 📚 Architectural Overview
For a detailed comparison of the implemented patterns, please see [README_ARCHITECTURES.md](file:///Users/surindersingh/AndroidStudioProjects/DemoApp/README_ARCHITECTURES.md).

---

## 🛠 Lessons Learned & Implementation Notes

During the development of the User Profile feature, several critical "Senior Developer" challenges were encountered and solved. These serve as a reference for best practices in professional Android development.

### 1. Reactive Data Flow & Room (The "Phantom Null" Issue)
*   **The Problem**: In an Offline-First setup, we observed that the UI would briefly show a "User Not Found" error even when the data was present.
*   **The Cause**: Room's `@Insert(REPLACE)` strategy is actually a `DELETE` followed by an `INSERT`. During the microsecond between these operations, Room's `InvalidationTracker` triggers a `null` emission to the Flow.
*   **The Solution**: 
    1.  Use `.distinctUntilChanged()` in the Repository to filter out redundant state updates.
    2.  Update the UI State Machine (ViewModel) to treat a `null` from the database as a "Transient Loading" state rather than a permanent error, until the network refresh confirms a failure.

### 2. Clean Architecture Responsibilities
*   **Repository vs. UseCase Mapping**:
    *   **Repository**: Responsible for **Data → Domain** mapping (e.g., `UserDto` to `User`). This ensures the Domain layer is completely decoupled from framework-specific models.
    *   **UseCase**: Responsible for **Business Logic** (e.g., calculating member status or combining multiple data sources).
*   **Main-Safety**: Even though Retrofit and Room handle dispatchers internally, a Repository should explicitly use `withContext(Dispatchers.IO)` to guarantee that every operation is safe to call from the Main thread.

### 3. Handling Android Framework Components
In a clean architecture, heavy Android components belong in the **Data/Framework Layer** and are accessed via **Interfaces** in the Domain layer:
*   **Bluetooth/BLE**: Implement a `BluetoothRepository` in the Data layer.
*   **Permissions**: Activity handles the request; a `PermissionProvider` in the Data layer handles the status check.
*   **Foreground Services**: Reside in the Data/Framework layer but delegate all actual logic to **UseCases**.
*   **Broadcast Receivers**: Act as external event triggers that feed data into Repositories.

### 4. Advanced Debugging in Android Studio
*   **Screen Recomposition**: Use **Layout Inspector > Show Recomposition Counts** to identify performance bottlenecks in Compose.
*   **Reactive Stream Probes**: When debugging fast-moving Flows, use a "History Logger" (recording emissions in a list) or a "Coroutine Brake" (`delay(100)`) to visualize the exact sequence of state changes without the debugger merging values.

---

## 🚀 Technical Stack
- **UI**: Jetpack Compose
- **Concurrency**: Kotlin Coroutines & Flow
- **Dependency Injection**: Hilt
- **Networking**: Retrofit & OKHttp
- **Local Storage**: Room Persistence Library
