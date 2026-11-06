> Context: [Main](./context.md) > Coding Conventions

# Coding Conventions

---

## Jetpack Compose

### Modifier Parameter Position
- `modifier: Modifier = Modifier` **must** always be the **first parameter** in every composable, even when there are other mandatory parameters

### Screen Pattern

- Top-level composable handles ViewModel, state collection, and navigation; accepts `navController: NavController` directly
- Private inner composable is pure UI — accepts state and event callbacks; used for previews
- User-triggered navigation is intercepted in the top-level `onEvent` lambda — do **not** bounce through `uiEffect`
- `uiEffect` is only for navigation the VM initiates after async work
- In the VM, navigation events handled in Compose map to `Unit`
- Always include both light and dark mode previews
- Add previews for all major variants of the UI like empty state, loading state, etc

```kotlin
// NavHost call site
composable<Route.My> { MyScreen(navController = navController) }

// Top-level: wires VM, state, and navigation
@Composable
fun MyScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
) {
    val viewModel: MyVM = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Only for VM-initiated navigation (e.g., after async work)
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
                else -> viewModel.onEvent(event)
            }
        },
    )
}

// Inner: pure UI
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

// ViewModel
class MyVM(
    private val repository: MyRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(MyUiState())

    private val uiEffectChannel = Channel<MyUiEffect>()
    val uiEffect = uiEffectChannel.receiveAsFlow().flowOn(Dispatchers.Main.immediate)

    fun onEvent(event: MyUiEvent): Any = when (event) {
        is MyUiEvent.Action1 -> handleAction1(event)
        is MyUiEvent.NavigateToSettings -> Unit  // Handled in Compose
    }

    private fun handleAction1(event: MyUiEvent.Action1) = viewModelScope.launch { ... }
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

### Comments

A comment must state what the code cannot. Otherwise delete it.

- One line, two at most — longer means the code or the name needs fixing, not the comment
- Reason only, never mechanics; no commented-out code, narration, decoration, or hedging (`maybe`, `not sure why`)
- `// TODO: <action>` only, never a question or bare verb; the commit that does the work deletes it
- `// region` / `// endregion` is the only structural marker; single-noun labels (`// Compose`, `# Android` in the catalog) only around dependencies
- Failing these is a defect, not a taste call

### File Header Format
`/** Created by <author> on MM/DD/YY */`

### KDoc Comments
- Only on public APIs and genuinely non-obvious functions — the contract, not the mechanics
- One or two sentences; document only non-obvious parameters

---

*This document is a living guide. Update it when new conventions are established. If the user corrects your code, distill the correction into a convention and add it here.*

---

## Related Context

- [Main Context](./context.md) — Project overview, tech stack, module index, agent guidelines
- [Build System](./build-system.md) — Convention plugins, version catalog, module setup
- [Commons Module](./core/commons/context.md) — Shared utilities, DI module, DevToolsScreen
