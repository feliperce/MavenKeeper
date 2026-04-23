# MavenKeeper

A cross-platform desktop app to inspect and clean up your local Maven repository
(`~/.m2/repository`). Built with Kotlin Multiplatform and Compose Desktop.

## Why

Local `.m2` folders grow silently: every plugin, every snapshot, every throwaway
transitive version stays on disk forever. MavenKeeper scans the repository, groups
artifacts by `groupId:artifactId`, and lets you see exactly what's taking up space —
and remove it safely.

## Features

- Scan the local Maven repository and aggregate artifacts by group.
- Per-artifact detail pane with version list, total size, last-used time and license.
- Installed versions and declared dependencies split into tabs.
- Filters: **All**, **Recent**, **Largest**, **Snapshots**, **Stale**.
- Reactive search by `groupId:artifactId` as you type.
- Delete a specific version, or purge all `SNAPSHOT` versions at once (with
  confirmation dialogs).
- Open any artifact or version folder in the system file manager.
- Custom repository path (for non-standard `.m2` locations).

## Install

Grab the installer for your OS from the
[latest release](https://github.com/feliperce/MavenKeeper/releases/latest):

| Platform | File |
| --- | --- |
| Linux (Debian/Ubuntu) | `mavenkeeper_<version>_amd64.deb` |
| macOS | `MavenKeeper-<version>.dmg` |
| Windows | `MavenKeeper-<version>.msi` |

Each installer bundles its own JRE — no separate Java install required.

## Build from source

Requirements: **JDK 17 or newer**. The Gradle wrapper handles Gradle itself.

```shell
# Run the dev build
./gradlew :composeApp:run

# Build an installer for the current OS
./gradlew :composeApp:packageDistributionForCurrentOS

# Build a specific format (must run on matching OS)
./gradlew :composeApp:packageDeb        # Linux, on Linux
./gradlew :composeApp:packageDmg        # macOS, on macOS
./gradlew :composeApp:packageMsi        # Windows, on Windows
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

Output installers land under `composeApp/build/compose/binaries/main/<format>/`.

### Overriding the version

Installers embed the version number in their metadata (Info.plist on macOS,
Add/Remove Programs on Windows, `dpkg` on Linux). By default a local build uses
`1.0.0`. Override with:

```shell
./gradlew :composeApp:packageDeb -PappVersion=1.2.3
```

## Tech stack

- **Kotlin** 2.3.x
- **Compose Multiplatform** 1.11.x (Material 3)
- **Navigation Compose** multiplatform
- **Kotlinx Coroutines** for async scanning
- **Compose Resources** for i18n (default locale: English)
- Distribution via `jpackage` (bundled with JDK 17+)

## Project layout

Single-target Kotlin Multiplatform project (JVM only today; the source-set
layout leaves room for future targets).

```
composeApp/
  src/jvmMain/kotlin/io/github/feliperce/mavenkeeper/
    data/              # Repository scanning, POM parsing, file manager
    di/                # AppContainer (manual DI)
    domain/            # Models & repository interfaces
    ui/
      components/      # Reusable composables (EmptyState, dialogs, ...)
      navigation/      # Destinations & NavHost
      screens/
        library/       # Main list + detail panes
        settings/      # Repository path, about
      theme/           # Color/Type/Shape + MavenKeeperTheme
  src/jvmMain/composeResources/
    values/strings.xml # UI text (English default)
```

## License

[MIT](./LICENSE.md) © Felipe Celestino
