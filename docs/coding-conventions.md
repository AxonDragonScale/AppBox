> Context: [Main](./context.md) > Coding Conventions

# AppBox Coding Conventions

---

## Jetpack Compose

### Composable Parameters

#### Modifier Parameter Position
- `modifier: Modifier = Modifier` is always the **first parameter** in every composable, even when required params like `navController` are present

```kotlin
@Composable
fun ShareToken(
    modifier: Modifier = Modifier,  // Always first
    navController: NavController,
    tokenId: Long,
) { ... }
```

### Composable Structure

#### Separation of VM and Preview Composables
- Top-level composable handles ViewModel, state collection, and navigation
- Private inner composable is pure UI — accepts state and event callbacks
- Use the inner composable for previews
- Intercept navigation events in the top-level `onEvent` lambda; forward the rest to the VM

```kotlin
@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: MyViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Use uiEffect only for VM-initiated navigation (e.g., after async work)
    viewModel.uiEffect.collectWithLifecycle { effect ->
        when (effect) {
            is MyUiEffect.NavigateToResult -> navController.navigate(Route.Result(effect.id))
        }
    }

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
) { ... }

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyScreenPreview() {
    AppTheme {
        MyScreen(uiState = MyUiState(), onEvent = {})
    }
}
```

### Navigation

- Top-level screen composables accept `navController: NavController` directly — not individual callbacks like `onNavigateToX`
- User-triggered navigation is intercepted in the top-level `onEvent` lambda and handled with `navController.navigate(...)` directly — do **not** bounce through `uiEffect`
- `uiEffect` is only for navigation the VM initiates after async work
- In the VM, navigation events handled in Compose map to `Unit`

```kotlin
// NavHost call site
composable<Route.Home> { Home(navController = navController) }

// In the VM
fun onEvent(event: MyUiEvent): Any = when (event) {
    is MyUiEvent.Search -> handleSearch(event)
    is MyUiEvent.NavigateToSettings -> Unit  // Handled in Compose
    is MyUiEvent.ItemClicked -> Unit         // Handled in Compose
}
```

### Preview Annotations
- Always include both light and dark mode previews

```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MyComponentPreview() {
    AppTheme { Surface { MyComponent() } }
}
```

---

## General Kotlin

### Date/Time

#### Use `kotlin.time` Instead of `kotlinx.datetime` for `Clock` and `Instant`
- `kotlinx.datetime.Clock` and `kotlinx.datetime.Instant` are deprecated — use `kotlin.time.Clock` and `kotlin.time.Instant` instead
- The rest of `kotlinx.datetime` (`LocalDate`, `TimeZone`, `toLocalDateTime()`, etc.) is still used normally

### Class Organization

#### Companion Object Placement
- `companion object` goes at the **top** of its enclosing class, before any other members

```kotlin
class MyVM : ViewModel() {
    companion object {
        private const val TAG = "MyVM"
    }

    val uiState = MutableStateFlow(MyUiState())
    fun onEvent(event: MyUiEvent) { ... }
}
```

### Style Preferences
- Use `=` expression body when a function's sole purpose is to launch a coroutine or is a single `when` expression:
  - `fun loadData() = viewModelScope.launch { ... }`
  - `fun onEvent(event: MyUiEvent): Any = when (event) { ... }`
- Keep lambdas on a single line when the total line length is under 120 characters
- Prefer `.orEmpty()` over `?: ""` / `?: emptyList()` on nullable `String?` and `List?`

### Naming Conventions
- ViewModels: `VM` suffix — `HomeVM`, `SettingsVM`
- UI State: `UiState` suffix — `HomeUiState`
- UI Events: `UiEvent` suffix — `HomeUiEvent`
- UI Effects: `UiEffect` suffix — `HomeUiEffect`

### State Management Pattern

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
        is MyUiEvent.Navigate -> Unit  // Handled in Compose
    }

    private fun handleAction1(event: MyUiEvent.Action1) = viewModelScope.launch { ... }
}
```

---

## Code Organization

### Region Comments for Large Files
- Use `// region <section-name>` and `// endregion` to separate logically distinct sections in large files
- Only use in substantial files — don't over-segment small ones

```kotlin
class MyVM : ViewModel() {

    // region State

    val uiState = MutableStateFlow(MyUiState())
    private val uiEffectChannel = Channel<MyUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    // endregion

    // region Event Handling

    fun onEvent(event: MyUiEvent): Any = when (event) {
        is MyUiEvent.Search -> handleSearch(event)
        is MyUiEvent.Navigate -> Unit
    }

    // endregion
}
```

---

## Documentation

### File Header Format
`/** Created by <author> on MM/DD/YY */`

### KDoc Comments
- Use KDoc for public APIs and complex functions to describe what the function does, not how
- Document non-obvious parameters; skip anything self-evident
- Keep it concise

---

*This document is a living guide. Update it when new conventions are established. If the user corrects your code, distill the correction into a convention and add it here.*

---

## Related Context

- [Main Context](./context.md) — Project overview, tech stack, module index, agent guidelines
- [Build System](./build-system.md) — Convention plugins, version catalog, module setup
- [Commons Module](./core/commons/context.md) — Shared utilities, DI module, DevToolsScreen