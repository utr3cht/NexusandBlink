# AnnihilationNexus

AnnihilationNexus is a custom plugin for Minecraft servers running Spigot/Paper, designed for the Annihilation game mode. It provides a robust Nexus system and custom classes with unique abilities.

## Features

- **Team-Based Nexus System**: Create and manage nexuses for different teams. When a team's nexus is destroyed, that team is eliminated.
- **Configurable Health**: Set the health of each nexus via commands or in the configuration file.
- **Live Scoreboard**: A sidebar scoreboard displays the real-time health of all nexuses.
- **Custom Classes & Abilities**: Unique classes that give players special abilities.
  - **Dasher**: A highly mobile class with a **Blink** ability. Teleport short distances with visual effects and a configurable cooldown.
  - **ScoutMaster-I**: A versatile class with a **Grapple** ability. Use a special fishing rod to pull yourself around the map.
- **Configurable Abilities**: Fine-tune ability parameters like grapple strength, durability, and cooldowns in the `config.yml`.
- **Persistence**: Player class data is saved and loaded, so players keep their class after relogging or server restarts.

## Commands

All commands are sub-commands of `/anni`.

- `/anni create <teamName>`: Creates a nexus for the specified team at your current location.
- `/anni delete <teamName>`: Deletes the nexus for the specified team.
- `/anni sethealth <teamName> <amount>`: Sets the health of a team's nexus.
- `/anni togglehealth`: Toggles whether nexus health is displayed on hit.
- `/anni reload`: Reloads the plugin's configuration files.
- `/anni class <player> <className>`: Sets a player's class. Available classes: `dasher`, `scoutmaster-i`.
- `/anni getblinkitem`: Gives you the Dasher's Blink item (Purple Dye).

## Permissions

- `annihilationnexus.admin`: Grants access to all `/anni` commands.

## Configuration

The plugin generates the following configuration files in the `plugins/AnnihilationNexus/` directory.

### `config.yml`

This file contains the main settings for the plugin.

```yaml
nexus-material: END_STONE
nexus-health: 75
xp-message: "&a+12 Shotbow XP"
nexus-destruction-delay: 1
nexus-hit-delay: 20

# Grapple Ability Settings
grapple:
  pull-strength-multiplier: 0.15 # Adjusts how strong the grapple pulls. Higher is stronger.
  durability-loss-chance: 0.25  # The chance (0.0 to 1.0) of losing 1 durability on use.
```

### `classes.yml`

This file automatically stores which class each player has. It is not recommended to edit this file manually.

## Installation

1.  Place the `AnnihilationNexus-X.X-SNAPSHOT.jar` file into your server's `plugins` directory.
2.  Start or restart your server.
3.  Configure the `config.yml` file to your liking.
4.  Use the `/anni` commands in-game to set up your nexuses and player classes.

## Building from Source

This project uses Apache Maven.

1.  Clone the repository.
2.  Navigate to the project directory.
3.  Run the command `mvn package`.
4.  The compiled JAR file will be located in the `target` directory.
