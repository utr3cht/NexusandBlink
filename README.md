# AnnihilationNexus

AnnihilationNexus is a custom plugin for Minecraft servers running Spigot/Paper, designed for the Annihilation game mode. It provides a robust Nexus system, custom classes with unique abilities, and team-based gameplay mechanics.

## Features

### Team-Based Gameplay
- **Nexus System**: Create and manage nexuses for different teams. When a team's nexus is destroyed, that team is eliminated.
- **Team Management**: Players can join teams using the `/team` command. Their team choice is saved, and they will automatically rejoin upon logging back in.
- **Friendly Fire Control**: Administrators can enable or disable friendly fire for all teams using the `/anni friendlyfire` command. This setting is persistent.
- **Live Scoreboard**: A sidebar scoreboard displays the real-time health of all nexuses.

### Custom Classes & Abilities
- **Persistence**: Player class data is saved and loaded, so players keep their class after relogging or server restarts.
- **Team-Aware Abilities**: Most abilities now correctly distinguish between teammates and enemies.
- **Available Classes**:
  - **Dasher**: A highly mobile class with a **Blink** ability.
  - **Scout**: A versatile class with a **Grapple** ability.
  - **Scorpio**: A tactical class with a **Scorpio Hook**. Right-click to pull enemies, and left-click to pull yourself to teammates.
  - **Assassin**: A stealthy class with a **Leap** ability.
  - **Spy**: A deceptive class with **Vanish** and **Flee** abilities.
  - **Farmer**: A support class with unique farming abilities.
    - **Feast**: Restore hunger for all nearby teammates, regardless of their class.
    - **Famine**: Inflict hunger and reduce food levels of all nearby enemies.
    - **Protected Crops**: Crops planted by a Farmer are protected. Only teammates can break them when fully grown, while enemies can break them at any time.
  - **Transporter**: A utility class with the ability to set up teleporters.

### Other Features
- **Launcher Pads**: Create launcher pads for quick transportation.
- **Configurable Abilities**: Fine-tune ability parameters in the `config.yml`.
- **Custom Kill Messages**: Kill messages are formatted to show the class of both the victim and the killer (e.g., `playerA(FAR) was killed by playerB(ASN)`).

## Commands

### Player Commands
- `/class [class_name]`: Selects your class. If a class name is provided, it selects that class. If not, it opens a class selection GUI.
- `/team <team_name>`: Joins the specified team.
- `/togglescoreboard`: Toggles the visibility of the sidebar scoreboard.

### Admin Commands (`annihilation.admin`)
- `/nexus create <teamName>`: Creates a nexus for the specified team at your current location.
- `/nexus delete <teamName>`: Deletes the nexus for the specified team.
- `/nexus setnexushp <teamName> <amount>`: Sets the health of a team's nexus.
- `/anni reload`: Reloads the plugin configuration.
- `/anni friendlyfire <on|off|true|false>`: Enables or disables friendly fire.
- `/classregion [create|delete|list]`: Manages class-restricted regions.

## Permissions
- `annihilation.admin`: Grants access to all admin commands (`/nexus`, `/anni`, `/classregion`).

## Configuration
The plugin generates configuration files in the `plugins/AnnihilationNexus/` directory. The `config.yml` allows you to customize nexus health, ability parameters, and more. A new `gameplay.friendly-fire` option is now available.

## Installation
1.  Place the `AnnihilationNexus-X.X-SNAPSHOT.jar` file into your server's `plugins` directory.
2.  Start or restart your server.
3.  Configure the `config.yml` file to your liking.
4.  Use the in-game commands to set up your nexuses and teams.

## Building from Source
This project uses Apache Maven.
1.  Clone the repository.
2.  Navigate to the project directory.
3.  Run `mvn clean install`.
4.  The compiled JAR file will be located in the `target` directory.