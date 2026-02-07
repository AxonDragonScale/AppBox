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
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: MyViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Handle effects/navigation
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            // Handle navigation effects
        }
    }
    
    // Call preview-friendly composable
    MyScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = { viewModel.onEvent(it) }
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
