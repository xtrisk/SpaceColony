# Space Colony Documentation

## Overview

Space Colony is a single-module Android game written in Java. The player manages an in-memory crew across four colony locations:

- `Quarters`
- `Simulator`
- `Mission Control`
- `Medbay`

The game loop is straightforward:

1. Recruit crew members with one of five specializations.
2. Move them between colony rooms.
3. Train them in the simulator to gain experience.
4. Send exactly two crew members on a mission.
5. Resolve a turn-based encounter against a generated threat.
6. Recover defeated crew in Medbay or return active crew to Quarters.

The project is designed around OOP concepts including abstraction, inheritance, encapsulation, polymorphism, enums, and singleton managers.

## Tech Stack

- Android application module built with Gradle
- Java 8 source/target compatibility
- Android SDK:
  - `compileSdk 34`
  - `targetSdk 34`
  - `minSdk 26`
- UI libraries:
  - `androidx.appcompat:appcompat:1.6.1`
  - `com.google.android.material:material:1.11.0`
  - `androidx.recyclerview:recyclerview:1.3.2`
  - `androidx.constraintlayout:constraintlayout:2.1.4`
  - `androidx.cardview:cardview:1.0.0`

## Project Structure

```text
SpaceColony/
├── build.gradle
├── settings.gradle
├── run-android.bat
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/spacecolony/
│       │   ├── MainActivity.java
│       │   ├── RecruitActivity.java
│       │   ├── QuartersActivity.java
│       │   ├── SimulatorActivity.java
│       │   ├── MissionControlActivity.java
│       │   ├── MedbayActivity.java
│       │   ├── StatsActivity.java
│       │   ├── adapter/CrewMemberAdapter.java
│       │   ├── game/
│       │   │   ├── Quarters.java
│       │   │   ├── Simulator.java
│       │   │   └── MissionControl.java
│       │   └── model/
│       │       ├── CrewMember.java
│       │       ├── Pilot.java
│       │       ├── Engineer.java
│       │       ├── Medic.java
│       │       ├── Scientist.java
│       │       ├── Soldier.java
│       │       ├── Threat.java
│       │       ├── Storage.java
│       │       └── Location.java
│       └── res/
│           ├── layout/
│           ├── drawable/
│           └── values/
└── gradle/
```

## Application Flow

### Main Screen

`MainActivity` is the colony dashboard. It shows:

- crew counts by location
- total crew count
- completed mission count
- navigation buttons to every other screen

The dashboard refreshes in `onResume()`, so counts update after every move, mission, or discharge.

### Recruitment

`RecruitActivity` lets the player:

- enter a crew name
- choose a specialization from a spinner
- preview the role's starting stats and special ability
- create the crew member

Recruitment is delegated to `Quarters.createCrewMember(...)`, which creates the correct subclass and stores it in shared memory.

### Quarters

`QuartersActivity` lists all crew currently in `Location.QUARTERS` using a `RecyclerView`. Selected crew can be moved to:

- `Simulator`
- `Mission Control`

### Simulator

`SimulatorActivity` lists all crew in `Location.SIMULATOR`. The player can:

- train selected crew
- send selected crew back to Quarters

Training gives:

- `+1` experience
- `+1` training session count

Returning to Quarters restores full energy.

### Mission Control

`MissionControlActivity` is the game's main combat screen. It has two phases:

1. Crew selection
2. Tactical mission execution

Rules enforced by the screen:

- only crew already in `Mission Control` can be selected
- exactly two crew members are required to launch a mission
- each round gives turns to surviving crew members
- each turn offers:
  - `Attack`
  - `Defend`
  - `Special`

The mission view includes:

- current turn indicator
- health bars for both crew members
- health bar for the threat
- scrolling battle log

### Medbay

`MedbayActivity` shows defeated crew members sent to `Location.MEDBAY`. They can be discharged back to Quarters, which restores full energy.

### Statistics

`StatsActivity` displays:

- total crew count
- completed mission count
- live counts by location
- per-crew stats:
  - specialization
  - ID
  - current location
  - effective skill
  - base skill
  - experience
  - current energy
  - training session count
  - missions completed
  - wins
  - losses

## Core Domain Model

### `CrewMember` (abstract)

`CrewMember` is the abstract base class for every playable role. It contains:

- identity:
  - auto-incremented `id`
  - `name`
- core stats:
  - `baseSkill`
  - `resilience`
  - `experience`
  - `energy`
  - `maxEnergy`
- state:
  - `location`
  - `defending`
- statistics:
  - `missionsCompleted`
  - `missionsWon`
  - `missionsLost`
  - `trainingSessions`

Important behavior:

- `act()`
  - standard attack damage
  - returns `effectiveSkill + random(0..2)`
- `takeDamage(int)`
  - reduces incoming damage by resilience
  - doubles resilience if defending
  - clears the defending flag after the hit
- `getEffectiveSkill()`
  - `baseSkill + experience`
- `restoreEnergy()`
  - heals to full
- `healEnergy(int)`
  - capped healing
- `useSpecial(...)`
  - abstract, implemented differently by each subclass

### Crew Specializations

#### `Pilot`

- Base stats: `skill 5`, `resilience 4`, `maxEnergy 20`
- Special: enables defending for the next incoming hit

#### `Engineer`

- Base stats: `skill 6`, `resilience 3`, `maxEnergy 19`
- Special: heals self for `4`

#### `Medic`

- Base stats: `skill 7`, `resilience 2`, `maxEnergy 18`
- Special: heals ally for `6`, or heals self for `3` if no valid ally exists

#### `Scientist`

- Base stats: `skill 8`, `resilience 1`, `maxEnergy 17`
- Special: permanently reduces threat resilience by `2`

#### `Soldier`

- Base stats: `skill 9`, `resilience 0`, `maxEnergy 16`
- Special: heavy attack for about `1.5 x effectiveSkill` plus random bonus

### `Threat`

`Threat` represents the system-generated enemy for each mission.

Fields:

- `name`
- `skill`
- `resilience`
- `energy`
- `maxEnergy`

Behavior:

- `attack(CrewMember target)`
  - retaliates with `skill + random(0..2)`
- `defend(int rawDamage)`
  - reduces received damage by resilience
- `reduceResilience(int amount)`
  - used by the Scientist special
- `isDefeated()`

### `Location`

`Location` is an enum with human-readable display names:

- `QUARTERS`
- `SIMULATOR`
- `MISSION_CONTROL`
- `MEDBAY`

### `Storage`

`Storage` is a singleton repository backed by:

- `HashMap<Integer, CrewMember> crewMap`

It provides:

- add/get/remove operations
- full crew listing
- filtering by location
- location summary counts
- total crew count

This storage is in memory only. Closing the app resets all progress.

## Game Managers

### `Quarters`

`Quarters` is a singleton responsible for:

- recruiting crew by specialization string
- setting initial location to `QUARTERS`
- restoring energy when crew return to Quarters

### `Simulator`

`Simulator` is a singleton responsible for:

- moving crew to `SIMULATOR`
- awarding training experience

Training does not cost energy and always succeeds.

### `MissionControl`

`MissionControl` is a singleton responsible for:

- moving crew to `MISSION_CONTROL`
- generating threats
- resolving combat actions
- finalizing mission success/failure
- tracking global mission progression with `missionCounter`

Threat scaling uses the completed mission counter:

- `skill = 4 + missionCounter`
- `resilience = max(0, missionCounter / 2)`
- `maxEnergy = 20 + missionCounter * 3`

Threat names rotate through a fixed list, including:

- `Asteroid Storm`
- `Solar Flare`
- `Hull Breach`
- `Alien Boarding Party`
- `Reactor Meltdown`
- `Fuel Leak Explosion`
- `Oxygen System Failure`
- `Rogue AI Drone`
- `Meteor Shower`
- `Microorganism Outbreak`

## Combat System

### Launch Requirements

- exactly two crew members must be selected
- both must already be in `Mission Control`

### Standard Turn Logic

For each active crew member:

1. The player chooses an action.
2. The selected action is executed.
3. If the threat is defeated, the mission ends immediately.
4. Otherwise, the threat retaliates against the acting crew member.
5. If both crew members are defeated, the mission fails.
6. If at least one survives, the round continues.

### Available Actions

#### Attack

- calls `actor.act()`
- applies threat resilience through `threat.defend(rawDamage)`

#### Defend

- sets `defending = true`
- the next incoming hit against that crew member uses doubled resilience

#### Special

- uses specialization-specific behavior through polymorphism

### Victory Outcome

On victory:

- `missionCounter` increments
- each surviving, non-defeated crew member gets:
  - `+1` experience
  - `+1` mission completed
  - `+1` mission won
- survivors remain in `Mission Control`

### Failure Outcome

On failure:

- `missionCounter` increments
- both crew members are sent to `Medbay`
- each gets:
  - `+1` mission completed
  - `+1` mission lost

Note:

- the implementation comment says Medbay recovery is partial, but the actual code calls `restoreEnergy()`, which fully restores energy before placing the crew member in `MEDBAY`

## UI and Presentation

The UI uses a custom space-themed visual style:

- dark blue layered backgrounds
- colored metric cards on the main dashboard
- specialization color coding in crew cards
- Material card-based lists
- progress bars for energy
- portrait orientation on all activities

Reusable styling is defined in:

- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`
- multiple custom drawable backgrounds in `app/src/main/res/drawable/`

## OOP Concepts Demonstrated

This project clearly demonstrates:

- abstraction
  - `CrewMember` defines shared state and abstract specialization behavior
- inheritance
  - five specialized roles extend `CrewMember`
- polymorphism
  - `useSpecial(...)` behaves differently per subclass
- encapsulation
  - class fields are hidden behind methods and controlled updates
- singleton pattern
  - `Storage`, `Quarters`, `Simulator`, and `MissionControl`
- enum usage
  - `Location`
- collection usage
  - `HashMap`, `ArrayList`, `List`, `Set`, `Map`

## Build and Run

### Android Studio

1. Open the project root in Android Studio.
2. Let Gradle sync finish.
3. Run the `app` configuration on an emulator or Android device.

### Command Line

Debug APK build:

```bat
gradlew.bat assembleDebug
```

The repository also includes:

```bat
run-android.bat
```

That script:

1. sets `GRADLE_USER_HOME` to the local `.gradle-user-home`
2. installs the debug build
3. launches the app with `adb shell monkey`

## Limitations and Notes

- No persistence layer is implemented.
  - Crew, mission count, and stats exist only for the current app process.
- No automated tests are included in the project.
- Crew selection order in missions comes from selected IDs converted from a `Set`, so the displayed A/B ordering is not guaranteed to be stable.
- In `Threat.attack(...)`, the combat log checks `target.isDefending()` after damage is applied, but `takeDamage(...)` resets that flag during the hit, so the log text may not show the defend multiplier even when it was used.
- `StatsActivity` builds its report in `onCreate()` only, so if the activity stays alive in the back stack it may not refresh until recreated.

## Verified State

The project was inspected from source and the debug build was verified successfully with:

```bat
gradlew.bat assembleDebug
```

using the local project Gradle home:

```bat
GRADLE_USER_HOME=.gradle-user-home
```
