# UltimateMobCoins

UltimateMobCoins is an open-source paper/folia 1.19, 1.20 and 1.21 only MobCoin plugin. It is built to be fully customizable with many features. Check out the features list down below.

## Quick Start

- **Download**: Available on [Polymart](https://polymart.org/resource/ultimatemobcoins.4806), [Hangar](https://hangar.papermc.io/ChimpGamer/UltimateMobCoins), and [Modrinth](https://modrinth.com/plugin/ultimatemobcoins)
- **Documentation**: [Complete setup guide](https://networkmanager.gitbook.io/ultimatemobcoins/)
- **Support**: [Discord server](https://discord.gg/HvaY4QY) or contact **pilske** directly

## Core Features

### Economy System
- **Customizable shop system** with integrated menus
- **Withdrawable MobCoins** - Convert coins to physical items
- **Permission-based multipliers** for different player groups
- **Transaction logging** for all purchases and payments
- **Spinner rewards system**

### Mob Configuration
- **Per-mob drop configuration** with range and decimal support
- **Looting enchant support** for increased drops
- **World-specific restrictions** to prevent drops in certain areas
- **Auto-pickup feature** for seamless collection (When enabled, no mobcoin item will be dropped)

### User Experience
- **Fully customizable menus** and messages
- **Sound effects** for enhanced gameplay
- **Skull texture support** for custom skull items

## Database Support

- SQLite
- MySQL
- MariaDB
- PostgreSQL
- MongoDB

## Plugin Integrations

### Tested Integrations
- **MythicMobs** - Custom mob support
- **EcoMobs** - Custom mob support
- **ItemsAdder** - Custom items integration
- **PlaceholderAPI** - Legacy placeholder system
- **MiniPlaceholders** - Modern placeholder system
- **UpgradableHoppers** & **EpicHoppers** - Prevents coin collection conflicts
- **WorldGuard** - Region-based restrictions
- **HeadDatabase** - Custom head support

### Untested Integrations
- **Oraxen** - Custom items (untested)
- **Nexo** - Item framework (untested)

## Building from Source

**Requirements**
- Internet connection
- Java Development Kit (JDK) 21 or newer
- Git

**Compiling from source**
```shell
git clone https://github.com/ChimpGamer/UltimateMobCoins.git
cd UltimateMobCoins
./gradlew build
```

## Screenshots

### Command Interface
![Commands Overview](https://github.com/ChimpGamer/UltimateMobCoins/assets/19960733/eb66f5ce-e921-4ac6-beb9-2cc5b1053d62)
![Command Details](https://github.com/ChimpGamer/UltimateMobCoins/assets/19960733/b2d57df5-6ef5-4dba-b568-46b9791312ab)
![Command Usage](https://github.com/ChimpGamer/UltimateMobCoins/assets/19960733/d15a2358-df48-475b-9477-973e6396baca)

### Shop Interfaces
**Standard Shop**:
![Normal Shop](https://user-images.githubusercontent.com/19960733/236314990-c6e78d16-6827-467a-bf87-12181ad76660.png)

**Rotating Shop**:
![Rotating Shop](https://user-images.githubusercontent.com/19960733/236315060-8d3102d1-4452-4640-ab2d-8903c4925970.png)
