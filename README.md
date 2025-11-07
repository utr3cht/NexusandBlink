# AnnihilationNexus

AnnihilationNexus is a custom plugin for Minecraft servers running Spigot/Paper, designed for the Annihilation game mode. It provides a robust Nexus system and custom classes with unique abilities.

## Features

- **Team-Based Nexus System**: Create and manage nexuses for different teams. When a team's nexus is destroyed, that team is eliminated.
- **Configurable Health**: Set the health of each nexus via commands or in the configuration file.
- **Live Scoreboard**: A sidebar scoreboard displays the real-time health of all nexuses.
- **Global Nexus Hit Cooldown**: Prevents the Nexus from being destroyed too quickly when multiple players attack simultaneously.
- **Custom Classes & Abilities**: Unique classes that give players special abilities.
  - **Dasher**: A highly mobile class with a **Blink** ability. Teleport short distances with visual effects and a configurable cooldown. Cooldown status is dynamically displayed in the item name.
  - **Scout**: A versatile class with a **Grapple** ability. Use a special fishing rod to pull yourself around the map. Also reduces fall damage by 50% when holding the grapple item.
  - **Scorpio**: A class with a **Scorpio Hook** ability. Pulls enemies or allies towards you.
  - **Assassin**: A stealthy class with a **Leap** ability. Launch yourself forward with temporary invisibility and speed. Robust fall damage immunity is provided during the leap. Cooldown status is dynamically displayed in the item name.
  - **Spy**: A deceptive class with **Vanish** and **Flee** abilities. Vanish by sneaking, and use Flee to spawn a decoy and gain temporary invisibility and speed. Cooldown status is dynamically displayed in the item name.
- **Launcher Pads**: Create launcher pads by placing a stone pressure plate on top of an iron or diamond block. Diamond blocks launch players twice as far. Robust fall damage immunity is provided after using a launcher pad.
- **Configurable Abilities**: Fine-tune ability parameters like grapple strength, durability, and cooldowns in the `config.yml`.
- **Persistence**: Player class data is saved and loaded, so players keep their class after relogging or server restarts.

## Commands

### Class Command

- `/class <player> <class>`: Sets a player's class. Available classes: `dasher`, `scout`, `scorpio`, `assassin`, `spy`.

### Nexus Command

- `/nexus create <teamName>`: Creates a nexus for the specified team at your current location.
- `/nexus delete`: Deletes the nexus at your current location.
- `/nexus setnexushp <teamName> <amount>`: Sets the health of a team's nexus.

### Anni Command

- `/anni reload`: Reloads the plugin configuration.

## Permissions

- `annihilationnexus.admin`: Grants access to all `/nexus` and `/anni` commands.
- `annihilationnexus.class.set`: Grants access to the `/class` command.

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

# Scorpio Ability Settings
scorpio:
  hook-cooldown: 3 # Cooldown in seconds for the Scorpio Hook.
  enemy-pull-fall-immunity: 10 # Fall immunity in seconds for enemies pulled by Scorpio.
  friendly-pull-fall-immunity: 5 # Fall immunity in seconds for allies pulled by Scorpio.

# Launcher Pad Settings
launcher-pad:
  iron-power: 2.0 # Launch power for iron block launcher pads.
  diamond-power: 4.0 # Launch power for diamond block launcher pads.
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