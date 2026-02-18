# AppBox Coding Conventions

This document outlines the coding standards and conventions used across the AppBox project.

---

## Jetpack Compose

### Composable Parameters

#### Modifier Parameter Position
- **Always use `modifier` as the first parameter in all composables** (both top-level ViewModel-handling composables and inner UI composables)
- `modifier` should always have a default value of `Modifier = Modifier`
- This provides consistency and makes it easier to apply custom styling at call sites
- Even when a composable has required parameters like `navController` or route parameters, `modifier` still comes first

**Example:**
```kotlin
// Top-level composable with ViewModel
@Composable
fun ShareToken(
    modifier: Modifier = Modifier,  // Always first
    navController: NavController,   // Required parameters after
    tokenId: Long,
) {
    // ViewModel handling...
}

// Inner UI composable
@Composable
private fun ShareToken(
    modifier: Modifier = Modifier,  // Always first
    uiState: ShareTokenUiState,     // State parameters after
    onEvent: (ShareTokenUiEvent) -> Unit,
) {
    // Pure UI...
}
```

### Composable Structure

#### Separation of VM and Preview Composables
Follow the pattern used in `Home.kt`:
- Top-level composable handles ViewModel, state collection, and navigation
- Private inner composable is pure UI, accepts state and event callbacks
- Use the inner composable for previews

**Example:**
```kotlin
@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: MyViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Collect effects only for things the VM initiates (e.g., navigate after async work)
    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is MyUiEffect.NavigateToResult -> navController.navigate(Route.Result(effect.id))
        }
    }

    // Call preview-friendly composable
    // Intercept navigation events here; forward the rest to the VM
    MyScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is MyUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
                is MyUiEvent.NavigateToDetail -> navController.navigate(Route.Detail(event.id))
                else -> viewModel.onEvent(event)
            }
        },
    )
}

@Composable
private fun MyScreen(
    modifier: Modifier = Modifier,
    uiState: MyUiState,
    onEvent: (MyUiEvent) -> Unit,
) {
    // Pure UI implementation
}

@Preview
@Composable
private fun MyScreenPreview() {
    AppTheme {
        MyScreen(
            uiState = MyUiState(),
            onEvent = {}
        )
    }
}
```

### Navigation

#### Pass `navController` to Top-Level Composables
- **Top-level screen composables should accept `navController: NavController`** instead of individual navigation callbacks (`onNavigateToX`, `onNavigateToY`, etc.)
- This keeps the call site in `NavHost` clean and avoids proliferating lambda parameters
- Navigation is then handled directly in the top-level composable's `onEvent` interceptor

**Preferred:**
```kotlin
composable<Route.Home> {
    Home(navController = navController)
}
```

**Avoid:**
```kotlin
composable<Route.Home> {
    Home(
        onNavigateToCreator = { navController.navigate(Route.Creator()) },
        onNavigateToScanner = { navController.navigate(Route.Scanner) },
        onNavigateToSettings = { navController.navigate(Route.Settings) },
        onNavigateToDetail = { id -> navController.navigate(Route.Detail(id)) },
    )
}
```

#### Intercept Navigation Events in Compose
- **Navigation events triggered by user interaction should be intercepted in the top-level composable's `onEvent` lambda** and handled directly with `navController.navigate(...)`, instead of sending them to the VM and bouncing them back via `uiEffect`
- Only use `uiEffect` for navigation that the VM initiates after async work (e.g., "navigate to result screen after a network call completes")
- In the VM, navigation events that are handled in Compose should map to `Unit` in the `onEvent` `when` block

**In the composable:**
```kotlin
onEvent = { event ->
    when (event) {
        // Intercept navigation — handle directly
        is MyUiEvent.NavigateToSettings -> navController.navigate(Route.Settings)
        is MyUiEvent.ItemClicked -> navController.navigate(Route.Detail(event.itemId))
        // Forward everything else to the VM
        else -> viewModel.onEvent(event)
    }
},
```

**In the VM:**
```kotlin
fun onEvent(event: MyUiEvent): Any = when (event) {
    is MyUiEvent.Search -> handleSearch(event)
    is MyUiEvent.Delete -> handleDelete(event)
    // Navigation events handled in Compose
    is MyUiEvent.NavigateToSettings -> Unit
    is MyUiEvent.ItemClicked -> Unit
}
```

### Preview Annotations

- Include both light and dark mode previews for all major composables
- Use `@Preview(showBackground = true)` for light mode
- Use `@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)` for dark mode

**Example:**
```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyComponentPreview() {
    AppTheme {
        Surface {
            MyComponent()
        }
    }
}
```

---

## General Kotlin

### Date/Time

#### Use `kotlin.time.Clock` Instead of `kotlinx.datetime.Clock`
- **Always use `kotlin.time.Clock`** (`kotlin.time.Clock.System.now()`) instead of `kotlinx.datetime.Clock` (`kotlinx.datetime.Clock.System.now()`)
- `kotlinx.datetime.Clock` is deprecated in favor of the Kotlin stdlib version
- The rest of the `kotlinx.datetime` library (e.g., `LocalDate`, `LocalTime`, `TimeZone`, `toLocalDateTime()`, `Instant` extensions) is still used normally — only `Clock` moved to `kotlin.time`

**Preferred:**
```kotlin
import kotlin.time.Clock

val now = Clock.System.now()
```

**Avoid:**
```kotlin
import kotlinx.datetime.Clock

val now = Clock.System.now() // Deprecated
```

### Class Organization

#### Companion Object Placement
- **Keep `companion object` at the top of its enclosing class/interface/object**, before any other members
- This ensures constants and factory methods are immediately visible when reading the class

**Preferred:**
```kotlin
class MyVM : ViewModel() {

    companion object {
        private const val TAG = "MyVM"
    }

    val uiState = MutableStateFlow(MyUiState())

    fun onEvent(event: MyUiEvent) { ... }
}
```

**Avoid:**
```kotlin
class MyVM : ViewModel() {

    val uiState = MutableStateFlow(MyUiState())

    fun onEvent(event: MyUiEvent) { ... }

    companion object {  // ❌ Should be at the top
        private const val TAG = "MyVM"
    }
}
```

### Coroutine Launch Style

#### Prefer Expression Body for Coroutine-Launching Functions
- When a function's sole purpose is to launch a coroutine, use expression body syntax (`=`) instead of wrapping in a block body
- This reduces nesting and makes the intent clearer

**Preferred:**
```kotlin
fun loadData() = viewModelScope.launch {
    val result = repository.fetchData()
    uiState.update { it.copy(data = result) }
}
```

**Avoid:**
```kotlin
fun loadData() {
    viewModelScope.launch {
        val result = repository.fetchData()
        uiState.update { it.copy(data = result) }
    }
}
```

### Lambda Formatting

#### Single-Line Lambdas
- **Prefer keeping lambdas on a single line when the total line length is under 120 characters**
- This improves readability for short, simple lambdas

**Preferred:**
```kotlin
items.filter { it.isActive }
items.map { Item(id = it.id, name = it.name) }
uiState.update { it.copy(isLoading = true) }
```

**Avoid (when it fits in a single line under 120 chars):**
```kotlin
items.filter {
    it.isActive
}
```

### Nullability Idioms

#### Prefer `.orEmpty()` over `?: ""`
- Use `.orEmpty()` on nullable `String?`, `List?`, etc. instead of the Elvis operator with an empty default
- This is more idiomatic and reads more naturally

**Preferred:**
```kotlin
val name = user.name.orEmpty()
val items = response.items.orEmpty()
```

**Avoid:**
```kotlin
val name = user.name ?: ""
val items = response.items ?: emptyList()
```

### Naming Conventions
- ViewModels: Use `VM` suffix (e.g., `HomeVM`, `SettingsVM`)
- UI State classes: Use `UiState` suffix (e.g., `HomeUiState`)
- UI Events: Use `UiEvent` suffix (e.g., `HomeUiEvent`)
- UI Effects: Use `UiEffect` suffix (e.g., `HomeUiEffect`)

### State Management Pattern

Follow the pattern established in `HomeVM`:
```kotlin
class MyVM(
    private val repository: MyRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(MyUiState())
    
    private val uiEffectChannel = Channel<MyUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)
    
    fun onEvent(event: MyUiEvent): Any = when (event) {
        is MyUiEvent.Action1 -> handleAction1(event)
        is MyUiEvent.Action2 -> handleAction2(event)
        // Navigation events handled in Compose
        is MyUiEvent.Navigate -> Unit
    }
    
    private fun handleAction1(event: MyUiEvent.Action1) {
        // Implementation
    }
}
```

---

## Code Organization

### Region Comments for Large Files
- Use `// region:` and `// endregion` comments to separate big, logically different chunks of code in large files
- Only use regions for substantial sections — don't over-segment small files or trivially short blocks
- Region names should be descriptive of the logical section they wrap

**Example:**
```kotlin
class MyVM(
    private val repository: MyRepository,
) : ViewModel() {

    companion object {
        private const val TAG = "MyVM"
    }

    // region: State

    val uiState = MutableStateFlow(MyUiState())

    private val uiEffectChannel = Channel<MyUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    // endregion

    // region: Event Handling

    fun onEvent(event: MyUiEvent): Any = when (event) {
        is MyUiEvent.Search -> handleSearch(event)
        is MyUiEvent.Delete -> handleDelete(event)
        is MyUiEvent.Navigate -> Unit
    }

    private fun handleSearch(event: MyUiEvent.Search) {
        // ...
    }

    private fun handleDelete(event: MyUiEvent.Delete) {
        // ...
    }

    // endregion
}
```

---

## Documentation

### File Headers
- Include author/creation date comment at the top of new files
- Format: `// Created by [Name] on DD/MM/YY`

### KDoc Comments
- Use KDoc for public APIs and complex functions
- Describe what the function does, not how it does it
- Document non-obvious parameters

### Don't comment very obvious things

---

## Project Structure

### Module Organization
- Keep related features together in their own packages
- Use consistent package structure across modules:
  - `ui/` - UI components and screens
  - `model/` - Data models
  - `repository/` - Data repositories
  - `di/` - Dependency injection modules

---

This document is a living guide and will be updated as new conventions are established.
