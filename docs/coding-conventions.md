---
title: "Coding conventions"
status: current
summary: "Rules every app follows: architecture, Compose screen pattern, ViewModel/state/event/effect, Kotlin idioms, comments, testing policy."
read_when: "Before writing or reviewing any code."
---

> Context: [Main](./context.md) > Coding Conventions

# Coding Conventions

---

## Architecture

Every app in the monorepo uses the same shape:

- **UI**: Jetpack Compose + Material 3
- **State**: `ViewModel` (`*VM` suffix) holding `MutableStateFlow<UiState>`
- **Events**: sealed interface `UiEvent` → `fun onEvent(event): Any`
- **One-shot effects**: `Channel<UiEffect>` → `receiveAsFlow().flowOn(Dispatchers.Main.immediate)`
- **DI**: Koin — `singleOf`, `viewModelOf`, `factoryOf`
- **Navigation**: Navigation Compose with `@Serializable` type-safe routes
- **Async**: Coroutines + Flow

Module rules: each app is self-contained with its own `Application` class. Pure Kotlin goes in
`:core:common`, Compose helpers in `:core:ui`, infrastructure in its capability module
(`:core:android`, `:core:datastore`, `:core:room`, `:core:network`, `:core:devtools`). Apps depend on
core modules, never on each other.

---

## Jetpack Compose

### Design Fidelity

- Every app has one design file, `apps/<app>/<app>.fig`. It is the source of truth for layout,
sizes, spacing and colour; `docs/apps/<app>/ui.md` is the source of truth for copy
- When the app and the design disagree, **fix the `.fig` first**, then `ui.md`, then the code. Never
  patch the app to hide a design-side mismatch — a screen that compensates for the design rots as
  soon as the next screen is drawn
- Draw real tap targets in the design (a text action is a 40 dp frame, an icon button is a 32 dp
  slot) so the design's geometry is the shipped geometry
- Verify a screen by rendering its previews (`android preview render :apps:<module> --fqn=…`) against
  an export of the design frame, in both themes
- Do not judge text geometry from the OpenPencil CLI in headless mode: it has no access to system
  fonts, substitutes them and measures text ~25 % too wide. Font-faithful checks need the running app

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

## Testing

### No Tests In Apps

- The apps ship **no unit, integration or instrumentation tests**. Do not add a `src/test` or
  `src/androidTest` source set, a test dependency, or test configuration to an app module
- App correctness rests on three things instead: `assembleDebug` compiling, Compose previews
  rendered against the app's design file (`android preview render :apps:<module> --fqn=<composable>`),
  and the on-device checks in that module's risk table in `docs/apps/<app>/context.md`
- The publishable libraries (`libraries/tlv`, `libraries/bitops`) keep their own `commonTest`
  suites — those are the only tests in the repo

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

### Shared Utilities and Failure Handling

- **Extend the core modules instead of writing a one-off.** Pure Kotlin helpers (logging, `Result`, JSON,
  date/time, generic flow utilities) belong in `:core:common`; Compose helpers in `:core:ui`; DataStore,
  Room, Android and network helpers in their capability modules. If a shared helper is missing a case — a
  `Throwable` overload, a nullable pref, a formatter — add it to the shared helper. A private variant of
  a core function sitting next to its call site is a review failure.
- **Handle recoverable failure inside the function that can fail.** Callers get a safe value or a
  result type; `runCatching` chains and `try`/`catch` sprinkled at call sites are not. Resource-bound
  code (DataStore, alarms, network) absorbs and logs its own exceptions.

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
- [Core Modules Overview](./core/context.md) — Android, UI, DevTools, DataStore, Room, Network, QR
- [Core Common Module](./core/common/context.md) — Pure Kotlin utilities (Logger, Result, Json, DateTime, Flow)
