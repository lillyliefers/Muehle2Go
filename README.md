# Mühle2Go

Android App zum Spielen des Brettspiels Mühle via Bluetooth oder Simulation.

## Features
- Lokales Multiplayer via Bluetooth
- Simulation-Mode ohne echtes Bluetooth
- Unterstützung für alle Spielphasen (Placing, Moving, Flying)
- Spielerwechsel und Sieglogik

## Build Instructions
1. Clone the repo:
   git clone https://github.com/lillyliefers/Muehle2Go.git
2. Open in Android Studio
3. Sync Gradle
4. Run on Emulator or Device

## Notes
- Bluetooth cannot be tested on Emulator, use Simulate Bluetooth button (must make it visible in fragment_role_selection.xml).
- For real devices, grant location permissions. 