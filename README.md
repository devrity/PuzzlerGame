# PuzzlerGame

An Android mobile puzzle game featuring dynamic sliding puzzles with advanced mechanics and customizable configurations.

## Overview

PuzzlerGame is a modern take on classic sliding tile puzzles, offering multiple grid sizes, complex tile behaviors, and progression-based gameplay. Built with Kotlin and designed for flexibility, the game supports three distinct product flavors with unique puzzle collections and branding.

## Features

### ✅ Implemented (DONE)

- **Dynamic board size** – Variable grid configurations from 2×2 up to 9×9
- **Custom initial state** – JSON-based puzzle layouts with configurable starting positions; any tile can be designated as empty, with random shuffle as the default
- **Multiple empty tiles** – Support for several tiles to be hidden or empty at game start
- **Locked tiles (permanent)** – Specific tiles cannot move, including tiles already in their correct position
- **State preservation** – Automatic save and restore on device rotation
- **Multi-flavor apps & content** – Single codebase builds three distinct app flavors with unique puzzles and branding:
  - Puzzle Pals
  - Brain Not Puzzler
  - Puzzle Pro
- **Victory sequence** – Celebratory confetti animation followed by victory screen, then gallery view
- **Gallery progression** – Graph-based puzzle unlocking system with progression tracking
- **Move counter** – Real-time display of the number of moves made
- **Move limit** – Win condition requires completion within a defined maximum number of moves

### 📋 Planned Features (BACKLOG)

#### Cascade Mechanics
- **Cascade Gravity Well (static)** – Fixed "gravity" tile that pulls chained tiles, creating line cascade effects
- **Cascade Gravity Well (managed)** – Player-editable chains determining which tiles are affected by the gravity source

#### Tile Consumption
- **Tile Consumption (free)** – Eater tile consumes any collided tiles without restriction, intentionally becoming overpowered
- **Tile Consumption (limited)** – Eater tile with finite consume counter (can only eat N tiles)

#### Advanced Mechanics
- **Hailstorm reshuffle** – Board-wide shift/reshuffle triggered after X moves instead of immediate failure
- **Forbidden empty cells** – Designated cells that cannot be occupied by certain tile types
- **Outside pass / wrap** – Tiles exiting one edge re-enter at another (selective wrap-around mechanics)
- **Non-counting tile** – Special tile whose moves do not increment the move counter
- **Temporary lock ("11L5")** – Tiles locked for N moves even if in incorrect position, then auto-unlock
- **Rotated tiles** – Tiles with orientation requirements; must match both position and rotation to be considered solved

## Technical Details

- **Platform**: Android
- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36
- **Build System**: Gradle

### Key Dependencies
- AndroidX Core KTX
- Material Design Components
- Konfetti (confetti animations)
- Gson (JSON puzzle configuration parsing)

## Project Structure

```
app/src/main/java/com/devrity/brainnotpuzzler/
├── MainActivity.kt          # Main game activity
├── GalleryActivity.kt       # Puzzle gallery and progression
├── adapter/                 # RecyclerView adapters
├── manager/                 # Business logic and game managers
├── model/                   # Data models
├── ui/                      # Custom UI components
└── util/                    # Utility classes
```

## Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/devrity/PuzzlerGame.git
   ```

2. Open the project in Android Studio

3. Select your desired product flavor:
   - `puzzlePals`
   - `brainNot`
   - `puzzlePro`

4. Build and run on your Android device or emulator

## Product Flavors

Each flavor represents a unique version of the game with distinct:
- Application ID
- App name and branding
- Puzzle collection (via separate JSON configuration files)

## License

This project is developed by [devrity](https://github.com/devrity).

## Contributing

This is an actively developed project. Feel free to open issues or submit pull requests for bug fixes and feature enhancements.
