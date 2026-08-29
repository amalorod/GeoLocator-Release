# Refactor GeoLocation to use GeoCoordinate

The goal is to eliminate data duplication and centralize validation logic by using `GeoCoordinate` as a member of `GeoLocation` instead of having separate `latitude` and `longitude` fields in both classes.

## User Review Required

> [!IMPORTANT]
> This change affects `LocalLocationRepository.kt` where all 30+ static locations are defined. The constructor of `GeoLocation` will change from taking `latitude` and `longitude` doubles to taking a `coordinate: GeoCoordinate` object.
> I will maintain backward compatibility for *reading* latitude and longitude by adding delegation properties to `GeoLocation`.

## Proposed Changes

### [Domain Layer]

#### [MODIFY] [GeoLocation.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/domain/model/GeoLocation.kt)
- Change constructor to use `coordinate: GeoCoordinate`.
- Add `val latitude: Double get() = coordinate.latitude`.
- Add `val longitude: Double get() = coordinate.longitude`.

### [Data Layer]

#### [MODIFY] [LocalLocationRepository.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/data/location/LocalLocationRepository.kt)
- Update all `GeoLocation` instantiations to use the new `coordinate` parameter.

### [UI Layer]

#### [MODIFY] [GameViewModel.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/ui/game/GameViewModel.kt)
- Simplify `submitGuess` to use `actualLocation.coordinate`.

#### [MODIFY] [MultiplayerGameViewModel.kt](file:///C:/Users/AM/Documents/01_uni-aktuelle-Projekte/2. Semester/BSI2/GeoGuessr_App/app/src/main/java/com/example/geoguessr_app/ui/multiplayer/MultiplayerGameViewModel.kt)
- Simplify `submitGuess` to use `actualLocation.coordinate`.

## Verification Plan

### Automated Tests
- Run `CalculateDistanceUseCaseTest` and `CalculateScoreUseCaseTest` to ensure no regressions in game logic.
- Perform a `gradle build` to ensure all constructor calls are correctly updated.

### Manual Verification
- Deploy the app and start a "Normal" game to verify that the first location loads correctly and the distance calculation still works.
- Verify that the "Hint" and "Region" fields are still correctly displayed.
