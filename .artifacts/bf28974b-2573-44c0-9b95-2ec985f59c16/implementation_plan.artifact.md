# Multiplayer Bug Fixes & Improvements Implementation Plan

This plan addresses the three remaining multiplayer issues reported by the user:
1. **Timer (`remainingSeconds`):** Timer does not run in multiplayer mode.
2. **Lives Display in Top Bar:** Large default integer value (e.g. `2147483647`) appears in the top bar during Multiplayer Battle Royale; lives should only be displayed in the scoreboard next to player names in multiplayer.
3. **Premature Game Ending after Round 1:** Due to asynchronous, non-atomic round advancement writes in Firebase, `observePlayerStates` detects `allPlayersFinished` repeatedly, causing multiple round advancements (`1 -> 2 -> 3 -> 4 -> 5`) to trigger instantly within milliseconds, ending the game right after round 1.

## User Review Required

> [!IMPORTANT]
> **Focus for University Submission:** We recommend prioritizing a 100% bug-free, smooth core multiplayer gameplay flow (advancing through all 5 rounds correctly, working timers, correct UI overlays). Handling abrupt app closures / going to home screen mid-game adds significant background state complexity (presence detection / lifecycle cleanup) that can introduce edge cases. The existing heartbeat timeout (20s) already handles inactive players.

## Proposed Changes

### Firebase / Repository Layer
#### [MODIFY] [SessionRepository.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/data/firebase/SessionRepository.kt)
- Add an atomic round advancement method `advanceRoundAtomic(sessionId, nextRound, nextLocationId, updatedPlayers)` that updates `currentRound`, `currentLocationId`, `roundStartTimestamp`, and all player states in a single atomic `updateChildren` / `setValue` call. This prevents race conditions where asynchronous updates cause rapid multi-round skipping.

### Multiplayer UI & ViewModel Layer
#### [MODIFY] [MultiplayerGameViewModel.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/ui/multiplayer/MultiplayerGameViewModel.kt)
- Implement `startTimer()` coroutine (analogous to `GameViewModel`), starting a 60-second countdown per round. When the timer hits 0, automatically call `finishRoundWithoutGuess()` or submit guess.
- Update `startNextRound()` on the host to use `sessionRepository.advanceRoundAtomic(...)`.
- Guard against duplicate round advancement triggers by checking if the round is already being advanced or if `currentRound` matches.

#### [MODIFY] [GameScreen.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/ui/game/GameScreen.kt)
- In `GameStatusHeader`, hide the lives display (`❤️ $lives`) in the top bar when `uiState.isMultiplayer` is true, since multiplayer lives are already shown in the `MultiplayerScoreboard`.

## Verification Plan

### Automated Tests
- Build test: `./gradlew app:assembleDebug`

### Manual Verification
- Deploy to test devices/emulators.
- Verify that the timer counts down every second in multiplayer.
- Verify that Battle Royale mode does not show the giant number in the top bar.
- Verify that completing Round 1 advances correctly to Round 2 (instead of ending the game).
