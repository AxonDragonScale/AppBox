# AppBox - Project Context

> **This is the entry point of the AppBox context graph.**
> If you are an AI agent or LLM, read the [Agent Context Guidelines](#agent-context-guidelines) section first.

## Agent Context Guidelines

### How to Use This Context System

This project uses a **context graph** — a set of interlinked markdown files that provide project knowledge. Each file covers a specific scope. You navigate by following links as needed, loading only the context relevant to your current task.

1. **Always start here** — this file is your entry point. It gives you the project overview, tech stack, architecture patterns, and links to everything else.
2. **Load the module context when working on a module** — follow the link in the [Module Index](#module-index) below.
3. **Always load [commons context](./core/commons/context.md)** — every app module depends on `:core:commons`. Know what utilities are available before writing new code.
4. **Follow sub-context links only when needed** — some modules have sub-context files for deep subsystems (e.g., topty's security or backup systems). Only read these when your task touches that subsystem.
5. **Load [coding-conventions.md](./coding-conventions.md) before writing code** — it defines Compose patterns, Kotlin idioms, naming conventions, and state management patterns you must follow.
6. **Load [build-system.md](./build-system.md) only for build/config tasks** — convention plugins, version catalog, signing, and build commands. Not needed for typical feature work.

### Context Loading by Task

| Task | Load These (in order) |
|------|----------------------|
| Feature work on a specific app | This file → App context → Commons context → Coding conventions |
| Working on topty security/encryption | This file → [Topty context](./apps/topty/context.md) → [Topty security](./apps/topty/security.md) → Commons context |
| Working on topty backup/restore | This file → [Topty context](./apps/topty/context.md) → [Topty backup-restore](./apps/topty/backup-restore.md) → Commons context |
| Working on wallmatic background services | This file → [Wallmatic context](./apps/wallmatic/context.md) → [Wallmatic background-services](./apps/wallmatic/background-services.md) → Commons context |
| Working on npinner notifications/scheduling | This file → [NPinner context](./apps/npinner/context.md) → [NPinner notification-system](./apps/npinner/notification-system.md) → Commons context |
| Adding a new app or module | This file → [Build system](./build-system.md) → Coding conventions → Commons context |
| Cross-cutting changes | This file → All affected module contexts → Commons context |
| Build configuration changes | This file → [Build system](./build-system.md) |

### Navigation Conventions

- Every context file has a **breadcrumb** at the top showing its position in the graph.
- Every context file has a **Related Context** section at the bottom with links to relevant docs.
- Sub-context files always link back to their parent module context.
- When you encounter a section that says *"See [link] for details"*, follow that link only if your task requires those details.

---

## Overview

AppBox is a monorepo containing multiple standalone Android applications that share common utilities and build configuration. Each app is independently installable and publishable while benefiting from shared code and consistent architecture.

## Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin (JVM 21) |
| Min SDK | 31 (Android 12) |
| Target SDK | 36 |
| UI Framework | Jetpack Compose |
| DI Framework | Koin |
| Networking | Ktor + Kotlinx Serialization |
| Database | Room |
| DataStore | Preferences + Proto DataStore |
| Image Loading | Coil 3 |
| Widgets | Jetpack Glance |
| Build System | Gradle (Kotlin DSL) with Version Catalogs + Convention Plugins |

## Key Technology Decisions

These are **non-negotiable** choices across the entire project:

1. **Koin** for dependency injection — never Hilt or Dagger
2. **Ktor** for networking — never Retrofit
3. **kotlinx.serialization** for JSON — never Gson or Moshi
4. **Room with KSP** — never KAPT
5. **JVM 21** — modern Kotlin features are available
6. **Compose BOM** — never specify individual Compose versions
7. **Proto DataStore** for complex/typed settings, Preferences DataStore for simple key-value

## Project Structure

```
AppBox/
├── apps/                        # Standalone Android applications
│   ├── jest/                   # Jokes/humor app with widget support
│   ├── limitless/              # Habit tracking app
│   ├── npinner/                # Notification pinner app
│   ├── qrqanvas/               # QR code generator/scanner app
│   ├── topty/                  # 2FA authenticator app
│   └── wallmatic/              # Wallpaper auto-changer app
├── core/                        # Shared library modules
│   └── commons/                # Shared utilities, network, DI setup
├── build-logic/                 # Convention plugins for build configuration
├── docs/                        # Context graph (you are here)
├── gradle/                      # Version catalog (libs.versions.toml)
├── keystore/                    # Signing configuration
└── libraries/                   # Standalone publishable libraries
    └── tlv/                    # BER-TLV encoding/decoding (Kotlin Multiplatform)
```

## Architecture Patterns

All apps follow these patterns consistently:

- **UI Layer**: Jetpack Compose with Material 3
- **State Management**: ViewModel (`*VM` suffix) + `MutableStateFlow<UiState>`
- **Events**: Sealed interface `UiEvent` → `fun onEvent(event): Any`
- **One-shot Effects**: `Channel<UiEffect>` → `receiveAsFlow().flowOn(Dispatchers.Main.immediate)`
- **Dependency Injection**: Koin — `singleOf`, `viewModelOf`, `factoryOf`
- **Navigation**: Navigation Compose with `@Serializable` type-safe routes
- **Async**: Kotlin Coroutines + Flow
- **Networking**: Ktor Client with Kotlinx Serialization JSON

### Module Dependency Rules

- Each app is fully self-contained with its own `Application` class
- Shared code goes in `:core:commons`
- App modules depend on `:core:commons`, never on each other
- Use typesafe project accessors: `implementation(projects.core.commons)`

See [coding-conventions.md](./coding-conventions.md) for detailed code patterns.
See [build-system.md](./build-system.md) for convention plugins and build configuration.

## Module Index

### App Modules

| Module | Package | Purpose | Context |
|--------|---------|---------|---------|
| `:apps:jest` | `com.axondragonscale.jest` | Daily jokes/humor with home screen widget | [context](./apps/jest/context.md) |
| `:apps:limitless` | `com.axondragonscale.limitless` | Habit tracking with statistics | [context](./apps/limitless/context.md) |
| `:apps:npinner` | `com.axondragonscale.npinner` | Persistent notification pinning with scheduling | [context](./apps/npinner/context.md) ∙ [notification-system](./apps/npinner/notification-system.md) |
| `:apps:qrqanvas` | `com.axondragonscale.qrqanvas` | Customizable QR code generator & scanner | [context](./apps/qrqanvas/context.md) |
| `:apps:topty` | `com.axondragonscale.topty` | TOTP-based 2FA code manager | [context](./apps/topty/context.md) ∙ [security](./apps/topty/security.md) ∙ [backup-restore](./apps/topty/backup-restore.md) |
| `:apps:wallmatic` | `com.axondragonscale.wallmatic` | Auto-change wallpapers on schedule | [context](./apps/wallmatic/context.md) ∙ [background-services](./apps/wallmatic/background-services.md) |

### Core Modules

| Module | Package | Purpose | Context |
|--------|---------|---------|---------|
| `:core:commons` | `com.axondragonscale.commons` | Shared utilities, networking, DI, DevTools | [context](./core/commons/context.md) |

### Libraries

| Module | Package | Purpose | Context |
|--------|---------|---------|---------|
| `:libraries:tlv` | `io.github.axondragonscale.tlv` | BER-TLV encoding/decoding (KMP, independently publishable) | [context](./libraries/tlv/context.md) |

## Related Context

- [Coding Conventions](./coding-conventions.md) — Compose patterns, Kotlin idioms, naming, state management
- [Build System](./build-system.md) — Convention plugins, version catalog, build commands
- [Commons Module](./core/commons/context.md) — Shared utilities available to all apps