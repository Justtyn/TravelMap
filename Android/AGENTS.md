# Repository Guidelines

## Project Structure & Module Organization
TravelMap is a single-module Android app rooted in `app/`, while Gradle settings reside at the repository root. Java sources live under `app/src/main/java/com/justyn/travelmap`, UI resources sit in `app/src/main/res`, JVM unit tests belong in `app/src/test`, and instrumentation suites in `app/src/androidTest`. Feature docs such as `API_DOC.md` stay beside the module, and shared assets must be stored inside the relevant `res/` folders for packaging.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` – compile and package a debug APK; run this before installing on a device or emulator.
- `./gradlew clean build` – full rebuild with lint, unit tests, and packaging, used for CI verification.
- `./gradlew testDebugUnitTest` – execute local JVM tests in `app/src/test`.
- `./gradlew connectedDebugAndroidTest` – run Espresso/UI tests on the attached emulator or device.
- `./gradlew lint` – static analysis; fix warnings before opening a pull request.

## Coding Style & Naming Conventions
Code is Java 8+, formatted with Android Studio defaults (4-space indentation, no tabs). Classes follow `PascalCase`, methods and fields use `camelCase`, constants stay `SCREAMING_SNAKE_CASE`, and fragment/activity names keep the `FeatureTypeSuffix` pattern (`PlanFragment`, `BookingAdapter`). Resource files follow Android guidelines (`fragment_home.xml`, `ic_location_primary.xml`, centralized strings). Run “Reformat Code” and optimize imports before committing.

## Testing Guidelines
Write JUnit4 unit tests mirroring source packages (`FeatureManagerTest`) for logic that does not require Android APIs. UI flows use Espresso tests named `FeatureFragmentTest`. Cover any new repository/service logic with at least one test, document fixtures in `app/src/test/resources`, and run `testDebugUnitTest` before pushing. Instrumentation suites are required whenever navigation, permissions, or map interactions change.

## Commit & Pull Request Guidelines
Commits should stay small, present-tense, and English (e.g., `Add booking filter dialog`). Group related files, keep formatting-only commits separate, and reference issue IDs when available. Pull requests need a summary, linked issue, testing notes (`testDebugUnitTest`, device/emulator), and before/after screenshots for UI work. Call out API or schema adjustments so the backend team can sync.

## Security & Configuration Tips
Keep credentials and API keys outside version control by injecting them from `local.properties` or Gradle properties via `BuildConfig`. Do not hard-code secrets in `strings.xml`. When adding services, document required toggles in `README` or `API_DOC.md` and supply safe defaults so the app can boot with mocked data.
