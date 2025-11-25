# AnnihilationNexus

AnnihilationNexus is a custom plugin for Minecraft servers running Spigot/Paper, designed for the Annihilation game mode. It provides a robust Nexus system, custom classes with unique abilities, and team-based gameplay mechanics.

## Features

### Team-Based Gameplay
- **Nexus System**: Create and manage nexuses for different teams. When a team's nexus is destroyed, that team is eliminated.
- **Team Management**: Players can join teams using the `/team` command. Their team choice is saved, and they will automatically rejoin upon logging back in.
- **Friendly Fire Control**: Administrators can enable or disable friendly fire for all teams using the `/anni friendlyfire` command. This setting is persistent.
- **Live Scoreboard**: A sidebar scoreboard displays the real-time health of all nexuses.

### Changelog
- **Spy "Flee" Ability Fix**: The decoy NPC spawned by the Spy's "Flee" ability now correctly displays the Spy's team color, making it more convincing. The Spy's team is also properly restored after the ability wears off.

## Chat Translation
This plugin introduces a robust chat translation system, allowing players to communicate across different languages seamlessly.

### Features
-   **Player-Specific Language Settings**: Each player can choose their preferred target language.
-   **Toggle Translation**: Players can enable or disable chat translation for incoming messages.
-   **Multiple Translator Support**:
    -   **DeepL**: High-quality translation service.
    -   **Azure Translator**: Reliable fallback option or primary translator.
    -   **Hybrid Mode**: Automatically falls back to Azure if DeepL's quota is exceeded.

### Commands
-   `/chat lang <EN|JA>`: Sets your target language for translations (e.g., `/chat lang JA` for Japanese).
-   `/chat on`: Enables incoming chat message translation.
-   `/chat off`: Disables incoming chat message translation.

### Configuration (config.yml)
-   `deepl-api-key`: Your DeepL API key (required for DeepL or Hybrid mode).
-   `azure-api-key`: Your Azure Translator API key (required for Azure or Hybrid mode).
-   `azure-region`: The Azure region for your Azure Translator service (e.g., `japaneast`).
-   `default-language`: The default language for players if not set (`EN`, `JA`, etc.).
-   `translator-mode`: Choose between `DEEPL`, `AZURE`, or `HYBRID`.

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

### Admin Commands (Requires Operator/OP status)
- `/nexus create <teamName>`: Creates a nexus for the specified team at your current location.
- `/nexus delete <teamName>`: Deletes the nexus for the specified team.
- `/nexus setnexushp <teamName> <amount>`: Sets the health of a team's nexus.
- `/anni reload`: Reloads the plugin configuration.
- `/anni friendlyfire <on|off|true|false>`: Enables or disables friendly fire.
- `/classregion [create|delete|list]`: Manages class-restricted regions.

## Permissions
- `annihilation.admin`: Grants access to all admin commands (`/nexus`, `/anni`, `/classregion`).

## Configuration
The plugin generates configuration files in the `plugins/AnnihilationNexus/` directory. The `config.yml` allows you to customize various aspects:
-   **Chat Translation Settings**: Refer to the "Chat Translation" section above for details on `deepl-api-key`, `azure-api-key`, `azure-region`, `default-language`, and `translator-mode`.
-   **Nexus Settings**: Customize `nexus-material`, `nexus-health`, `xp-message`, `nexus-destruction-delay`, and `nexus-hit-delay`.
-   **Ability Settings**: Fine-tune parameters for Grapple, Launcher Pad, Scorpio, Assassin, Farmer, Transporter, and Dasher abilities. This includes new options for Farmer's custom drops.
-   **Gameplay Settings**: A new `gameplay.friendly-fire` option is available.
-   **Scoreboard Customization**: Adjust `title` and `lines`.
-   **Achievement Messages**: Configure `netherite-hoe-break-message`.

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