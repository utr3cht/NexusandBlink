# AnnihilationNexus

A Spigot/Paper plugin that implements a Nexus system for the Annihilation game mode, featuring a custom Blink ability for the Dasher class.

## Features

*   **Configurable Nexus**: Customize the Nexus block material, its initial health, and the message displayed when players hit it.
*   **Nexus Hit Delay**: Configure a minimum delay between hits on a Nexus to prevent rapid destruction.
*   **Blink Ability (Dasher Class)**:
    *   **Dynamic Cooldown**: Cooldown scales with the distance blinked, capped at 20 seconds.
    *   **Visualizer Block**: A temporary block appears at the target blink location, embedded in the ground. The block's color changes based on the blink distance (Green for short, Yellow for medium, Red for long).
    *   **Direction Preservation**: Your facing direction (pitch and yaw) is maintained after blinking.
    *   **Exclusive Item**: Only a specially named "Blink" purple dye can activate the ability.
    *   **Safe Blinking**: Prevents blinking into unsafe locations (e.g., mid-air).
*   **Custom Command**: All plugin commands are accessible via `/anni`.

## Installation

1.  Download the latest `AnnihilationNexus-1.0-SNAPSHOT-shaded.jar` from the `target` folder of this project.
2.  Place the downloaded `.jar` file into your Minecraft server's `plugins` folder.
3.  Start or restart your Minecraft server.

## Configuration (`plugins/AnnihilationNexus/config.yml`)

Upon first run, a `config.yml` file will be generated in the `plugins/AnnihilationNexus/` folder. You can modify the following options:

```yaml
# The material to be used for the Nexus block.
# A list of valid materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html
nexus-material: END_STONE

# The initial health of the Nexus.
nexus-health: 75

# The message to be displayed when a player hits a nexus.
# Use '&' for color codes.
xp-message: "&a+12 Shotbow XP"

# The delay in ticks before a nexus is destroyed after its health reaches 0.
nexus-destruction-delay: 1

# The minimum delay in ticks between a player hitting a nexus.
nexus-hit-delay: 20
```

## Commands

All commands start with `/anni`.

*   `/anni getblinkitem`: Gives you the special "Blink" item required to use the Dasher's Blink ability.

### Permissions

*   `annihilationnexus.admin`: Required to use the `/anni` commands.

## Usage (Blink Ability)

1.  Obtain the "Blink" item using `/anni getblinkitem`.
2.  Hold the "Blink" item in your hand.
3.  Sneak (shift) and right-click to activate the Blink ability.
4.  A visualizer block will appear at your target location, indicating where you will blink. Its color will change based on distance.
5.  Upon blinking, your facing direction will be preserved.

## Building from Source (Optional)

If you wish to build the plugin from its source code, follow these steps:

### Prerequisites

*   Java Development Kit (JDK) 21 or newer
*   Apache Maven 3.9.11 or newer

### Build Steps

1.  Navigate to the project's root directory in your terminal.
2.  Run the following Maven command:
    ```bash
    mvn clean package
    ```
3.  The compiled plugin JAR (`AnnihilationNexus-1.0-SNAPSHOT-shaded.jar`) will be located in the `target/` directory.