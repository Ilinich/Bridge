# Bridge

[![CI](https://github.com/Ilinich/Bridge/actions/workflows/ci.yml/badge.svg)](https://github.com/Ilinich/Bridge/actions/workflows/ci.yml)

A football supporter app for Android and iOS, built with Kotlin Multiplatform and Compose
Multiplatform. Clone it and run it — there is no API key to obtain and no account to create.

> Unofficial fan project. Not affiliated with, endorsed by, or connected to Chelsea Football Club.
> No club artwork is stored in this repository; every image is loaded at runtime from the data
> sources listed below.

## Status

Under construction. The module graph, build conventions and CI are in place; screens land phase
by phase.

## Data sources

| Source | Used for | Access |
|---|---|---|
| [TheSportsDB](https://www.thesportsdb.com/free_sports_api) | club profile, squad, next and last match | free test key, no registration |
| [openfootball/football.json](https://github.com/openfootball/football.json) | full Premier League season fixtures | public JSON on GitHub, no key |

Both tiers are limited, and the app treats those limits as content rather than as errors — a
truncated squad renders as a squad, not as a retry screen. The limits are documented per screen
as the screens land.

## Modules

| Module | Contains |
|---|---|
| `foundation:tessera` | state-holder framework: composable state, actions, composition |
| `foundation:cache` | in-memory cache with soft and hard TTL |
| `core:data` | HTTP clients, DTOs, domain models, repositories |
| `uikit` | theme, glass surfaces, runtime-shader brushes, shared components |
| `navigation` | Navigation 3 routes and swipe-to-dismiss |
| `feature:matches` | matchday, season calendar, match detail |
| `feature:squad` | squad grid, player pager |
| `shared` | dependency graph, navigation host, iOS framework |
| `androidApp` / `iosApp` | platform shells |

Feature modules never depend on each other.

## Building

Requires JDK 21 and an Android SDK; Gradle provisions its own toolchain.

```bash
./gradlew :androidApp:assembleDebug                      # Android
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64    # iOS framework
./gradlew detekt                                         # static analysis, every module
./gradlew allTests                                       # unit tests, every module
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run.

## License

[MIT](LICENSE).
