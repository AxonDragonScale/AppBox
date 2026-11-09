---
title: "AppBox — agent entry point"
status: current
summary: "Map of the AppBox monorepo: stack invariants, the documentation graph, and how to work in the repo."
read_when: "At the start of every session, before opening any other doc."
---

# AppBox — Agent Entry Point

Monorepo of independent Android apps (`apps/`), shared capability modules (`core/`), standalone
publishable libraries (`libraries/`), and convention-plugin build logic (`build-logic/`).
Kotlin 21 · Compose · Koin · Ktor · Room + KSP.

This file is the map, not the territory. Read it, then open only what your task points to.

Every doc opens with the same four-field header — `title`, `status`, `summary`, `read_when`. Reading
those six lines tells you what the doc covers, whether to trust it, and whether it is your task. If
it isn't, stop there.

## Ground rules

### The stack is fixed

- **Koin**, never Hilt or Dagger. **Ktor**, never Retrofit. **kotlinx.serialization**, never
  Gson or Moshi.
- **Room with KSP**, never KAPT. **JVM 21**. **Compose BOM only** — never pin individual Compose
  versions.
- **Proto DataStore** for typed settings, **Preferences DataStore** for simple keys.
- `:core:common` stays free of Compose, Koin, Ktor, Room and DataStore.
- Apps never depend on each other.
- No tests in app modules. Only publishable libraries (`libraries/tlv`, `libraries/bitops`) keep
  `commonTest` suites.

### Before you write anything

- [Coding conventions](./coding-conventions.md) — Compose screen pattern, ViewModel/state/event,
  naming, comments. **Always read before writing or reviewing code.**
- The owning module's `context.md` in the [doc map](#documentation-map) — package, module path,
  architecture, data model, on-device risks.
- [Build system](./build-system.md) — only when touching Gradle, dependencies, or signing.

### Working style

- Don't re-read a file you just wrote or edited.
- Edit the lines that need changing; don't rewrite whole files.
- Don't re-run a build or command to "verify" unless the outcome was genuinely uncertain.
- Batch related edits; parallelize independent reads.
- Scope searches to the module you're in.
- Keep replies short. A one-line confirmation beats a paragraph recap.

## Documentation map

Every durable doc in the graph. If it isn't here, it doesn't exist.

Paths in these tables are relative to `docs/`, not the repository root — `libraries/tlv/context`
means `docs/libraries/tlv/context.md`.

### Foundations

Read both before any non-trivial change; they apply to every module.

| Doc | What's inside |
|---|---|
| [coding-conventions](./coding-conventions.md) | Architecture rules, Compose screen pattern, VM/state/event/effect, Kotlin idioms, comment style, testing policy |
| [build-system](./build-system.md) | Convention plugins, version catalog, signing, build commands |

### Apps

Read the app's `context.md` before changing it. Open a sub-doc only when the task touches that
subsystem.

| Doc | What's inside |
|---|---|
| [apps/cardy/context](./apps/cardy/context.md) | `:apps:cardy` — NFC EMV card reader, encrypted vault, known limitations |
| [apps/devinator/context](./apps/devinator/context.md) | `:apps:devinator` — developer-option / USB / wireless-debugging toggles, QS tiles, Tweaks, Logcat |
| [apps/devinator/design](./apps/devinator/design.md) | Devinator behaviour rules, state machine, edge cases |
| [apps/devinator/ui](./apps/devinator/ui.md) | Devinator visual spec, tokens, literal copy |
| [apps/jest/context](./apps/jest/context.md) | `:apps:jest` — jokes app, the reference KMP layout (Android/JVM/iOS), Glance widget |
| [apps/limitless/context](./apps/limitless/context.md) | `:apps:limitless` — habit tracking, statistics, KMP |
| [apps/npinner/context](./apps/npinner/context.md) | `:apps:npinner` — persistent notification pinning, scheduling |
| [apps/npinner/notification-system](./apps/npinner/notification-system.md) | Notification lifecycle, receivers, workers, reboot survival |
| [apps/qrqanvas/context](./apps/qrqanvas/context.md) | `:apps:qrqanvas` — stylized QR generator, CameraX + ML Kit scanner |
| [apps/topty/context](./apps/topty/context.md) | `:apps:topty` — TOTP/HOTP 2FA authenticator |
| [apps/topty/security](./apps/topty/security.md) | PBKDF2, AES-256-GCM, biometrics, key storage, re-encryption |
| [apps/topty/backup-restore](./apps/topty/backup-restore.md) | Backup formats (Topty/Aegis/URI), import/export flows |
| [apps/wallmatic/context](./apps/wallmatic/context.md) | `:apps:wallmatic` — scheduled wallpaper rotation, albums |
| [apps/wallmatic/background-services](./apps/wallmatic/background-services.md) | Update cycle, Doze/reboot survival, tiles, receivers |

### Core modules

Check here before writing a helper — the shared one usually exists.

| Doc | What's inside |
|---|---|
| [core/context](./core/context.md) | `:core:android`, `:core:ui`, `:core:devtools`, `:core:datastore`, `:core:room`, `:core:network`, `:libraries:qr` — contents and dependency direction |
| [core/common/context](./core/common/context.md) | `:core:common` — Logger, `Result`, `CommonJson`, `DateTimeExt`, Flow helpers, `AppInfo` |

### Libraries

| Doc | What's inside |
|---|---|
| [libraries/tlv/context](./libraries/tlv/context.md) | `:libraries:tlv` — BER-TLV codec, KMP, independently published |
| [libraries/logcat-viewer/context](./libraries/logcat-viewer/context.md) | `:libraries:logcat-viewer` — in-app log viewer with a host-supplied source |

### Design and reference

| Doc | What's inside |
|---|---|
| [design/openpencil-skill](./design/openpencil-skill.md) | Driving OpenPencil MCP: JSX rendering, tokens, variants, verification. **Read before editing any `.fig`** |
| [reference/emv-nfc-library-technical-reference](./reference/emv-nfc-library-technical-reference.md) | devnied/EMV-NFC-Paycard-Enrollment internals — APDU, TLV, provider seam |

## Open work

Plans and migrations exist only while the work is open. **When the work lands, delete the doc**,
fold anything durable into the matching doc above, and remove the row.

| Doc | Work in flight |
|---|---|
| [apps/jest/ui-revamp-plan](./apps/jest/ui-revamp-plan.md) | Responsive redesign, three layout tiers |
| [libraries/bitops/plan](./libraries/bitops/plan.md) | `KmpBitSet` API — module exists but is not implemented |
| [libraries/kotp/plan](./libraries/kotp/plan.md) | Extracting OTP generation out of Topty — module not created |
| [migration/kmp-single-app-migration-playbook](./migration/kmp-single-app-migration-playbook.md) | Repeatable Android → KMP playbook, live until every app is migrated |

## Documentation policy

- **Every doc opens with the fixed header**: `title`, `status` (`current` | `in-progress` |
  `reference`), `summary`, `read_when`. Nothing else goes above it.
- **Durable docs describe the code as it is.** Change them in the same commit as the code.
- **Open-work docs describe intent.** They are deleted on completion — never left to rot.
- **Adding a doc means adding a row** to the map or the open-work table, in the same change, and
  keeping that row and the header saying the same thing.
- **A doc nobody can reach is a bug.** Every file in `docs/` is linked from this page.
- **Check it before committing**: `./scripts/check-docs.sh` verifies every link, index entry, and
  backticked repo path. Enable the local pre-commit hook once per clone with
  `git config core.hooksPath .githooks`.

## Commands

```bash
./gradlew :apps:<module>:assembleDebug          # e.g. :apps:topty:assembleDebug
./gradlew :apps:jest:jestAndroid:assembleDebug  # KMP apps build through the Android shell
```
