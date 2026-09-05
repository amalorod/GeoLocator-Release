# Implementation Plan - New Game Modes & Challenge Mode

Implement Entdecker mode, Detective mode, and transform the unused Daily Quest game mode into a Challenge mode with predefined challenge scenarios.

## User Review Required

> [!IMPORTANT]
> - `GameMode.DAILY_QUEST` will be replaced by `GameMode.CHALLENGE`. (Note: The independent Daily Quest feature/screen for daily tasks accessed via the home button remains fully intact).
> - `ENTDECKER` (Entdecker-Modus): 300 seconds per round, full Street View navigation.
> - `DETECTIVE` (Detektiv-Modus): 60 seconds per round, no Street View navigation (static observation).
> - `CHALLENGE` (Challenge-Modus): Opens a new challenge selection screen with predefined challenge scenarios (e.g., America Challenge, Europe Master, South America Pro), mapped to `CustomGameSettings`.

## Proposed Changes

### Game Domain & UI Models

#### [MODIFY] [GameMode.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/ui/game/GameMode.kt)
- Add `ENTDECKER` (displayName = "Entdecker", roundDurationSeconds = 300, streetViewNavigationEnabled = true, isAvailable = true)
- Add `DETECTIVE` (displayName = "Detektiv", roundDurationSeconds = 60, streetViewNavigationEnabled = false, isAvailable = true)
- Replace `DAILY_QUEST` with `CHALLENGE` (displayName = "Challenges", roundDurationSeconds = 60, streetViewNavigationEnabled = true, isAvailable = true)

#### [MODIFY] [AppDestination.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/navigation/AppDestination.kt)
- Add `data object ChallengeSelection : AppDestination("challengeSelection")`

### New UI Component

#### [NEW] [ChallengeSelectionScreen.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/ui/game/ChallengeSelectionScreen.kt)
- Create UI screen listing predefined challenges (e.g. America Challenge, Europe Master, South America Pro).
- On selection, passes `CustomGameSettings` to `gameViewModel.startCustomGame(settings)` and navigates to the game screen.

### Navigation & Routing

#### [MODIFY] [GeoGuessrNavHost.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/navigation/GeoGuessrNavHost.kt)
- Update `onStartGameClick` routing: if `selectedGameMode == GameMode.CHALLENGE`, navigate to `AppDestination.ChallengeSelection.route`.
- Add composable route for `ChallengeSelectionScreen`.

## Verification Plan

### Automated Tests
- Gradle build verification (`gradle_build("app:assembleDebug")`).

### Manual Verification
- Deploy and verify that Entdecker, Detective, and Challenge modes appear in the Mode selection dialog.
- Verify Entdecker has a 300s timer and navigation enabled.
- Verify Detective has a 60s timer and navigation disabled.
- Verify Challenge mode opens the challenge selection screen, selects a challenge, and starts the game correctly.
