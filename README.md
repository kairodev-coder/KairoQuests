# KairoQuests

KairoQuests is a Kotlin quest system for Paper servers. It provides configurable daily, weekly, repeatable, and one-time quests with GUI menus, SQLite storage, reward claiming, streaks, and optional PlaceholderAPI and Vault support.

## Features

- Configurable quest objectives, rewards, reset rules, and GUI items
- Daily, weekly, seasonal, repeatable, and one-time quest categories
- SQLite storage with cached player progress
- Reward claiming with commands, messages, sounds, titles, action bars, money, experience, and items
- Main, daily, weekly, and quest detail menus
- Tracked quest support with internal placeholders
- Optional PlaceholderAPI and Vault hooks
- Public API for progress updates from other plugins

## Quest Types

`BREAK_BLOCK`, `PLACE_BLOCK`, `KILL_MOB`, `KILL_PLAYER`, `FISH_ITEM`, `CRAFT_ITEM`, `CONSUME_ITEM`, `ENCHANT_ITEM`, `WALK_DISTANCE`, `PLAY_TIME`, `LOGIN`, `COMMAND`, and `CUSTOM`.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/quests` | Open the main quest menu | `kairoquests.command.quests` |
| `/quest` | Alias for `/quests` | `kairoquests.command.quests` |
| `/daily` | Open daily quests | `kairoquests.command.daily` |
| `/weekly` | Open weekly quests | `kairoquests.command.weekly` |
| `/quest progress` | Show current progress | `kairoquests.command.progress` |
| `/quest claim <quest>` | Claim a completed quest | `kairoquests.command.claim` |
| `/quest track <quest>` | Track a quest | `kairoquests.command.track` |
| `/quest untrack` | Clear the tracked quest | `kairoquests.command.track` |
| `/kairoquests reload` | Reload configuration files | `kairoquests.admin.reload` |
| `/kairoquests reset <player> [quest]` | Reset all or one quest | `kairoquests.admin.reset` |
| `/kairoquests giveprogress <player> <quest> <amount>` | Add progress | `kairoquests.admin.progress` |
| `/kairoquests setprogress <player> <quest> <amount>` | Set progress | `kairoquests.admin.progress` |
| `/kairoquests complete <player> <quest>` | Complete a quest | `kairoquests.admin.complete` |
| `/kairoquests stats <player>` | Show player stats | `kairoquests.admin.stats` |
| `/kairoquests debug` | Show storage, hook, and quest status | `kairoquests.admin` |

## Permissions

| Permission | Description |
| --- | --- |
| `kairoquests.command.quests` | Use `/quests` and `/quest` |
| `kairoquests.command.daily` | Use `/daily` |
| `kairoquests.command.weekly` | Use `/weekly` |
| `kairoquests.command.progress` | View progress |
| `kairoquests.command.claim` | Claim rewards |
| `kairoquests.command.track` | Track and untrack quests |
| `kairoquests.admin` | Access admin commands |
| `kairoquests.admin.reload` | Reload configs |
| `kairoquests.admin.reset` | Reset player progress |
| `kairoquests.admin.progress` | Change progress |
| `kairoquests.admin.complete` | Complete quests for players |
| `kairoquests.admin.stats` | View player stats |

## quests.yml Example

```yaml
quests:
  daily_miner:
    enabled: true
    display-name: "<gold>Daily Miner"
    description: "Break 250 stone."
    category: DAILY
    type: BREAK_BLOCK
    target: STONE
    amount: 250
    reset-type: DAILY
    reset-interval: 86400
    auto-start: true
    auto-claim: false
    rewards:
      - type: MONEY
        value: "500"
      - type: EXPERIENCE
        value: "250"
```

## GUI Customization

`guis.yml` controls titles, sizes, border material, quest slots, and item actions. GUI items support materials, names, lore, slots, amounts, custom model data, enchanted glint, hidden flags, player heads, permissions, and click actions such as `OPEN_GUI:daily`, `CLAIM_QUEST:%quest_id%`, `TRACK_QUEST:%quest_id%`, `NEXT_PAGE`, `PREVIOUS_PAGE`, `BACK`, `CLOSE`, `REFRESH`, `RUN_COMMAND:command`, `SEND_MESSAGE:message`, and `PLAY_SOUND:sound`.

## Optional Hooks

- PlaceholderAPI adds `%kairoquests_*%` placeholders when installed.
- Vault enables `MONEY` rewards when an economy provider is available.

## PlaceholderAPI

When PlaceholderAPI is installed and enabled in `config.yml`, these placeholders are available:

| Placeholder | Value |
| --- | --- |
| `%kairoquests_daily_completed%` | Completed daily quests |
| `%kairoquests_weekly_completed%` | Completed weekly quests |
| `%kairoquests_total_completed%` | Total completed quests |
| `%kairoquests_daily_streak%` | Current daily streak |
| `%kairoquests_best_daily_streak%` | Best daily streak |
| `%kairoquests_tracked%` | Tracked quest display name |
| `%kairoquests_tracked_progress%` | Tracked quest progress |
| `%kairoquests_tracked_percent%` | Tracked quest percent |

## API Usage

```kotlin
val plugin = server.pluginManager.getPlugin("KairoQuests") as KairoQuestsPlugin
plugin.addProgress(player.uniqueId, "daily_miner", 5)
plugin.completeQuest(player.uniqueId, "one_time_first_steps")
```

## Installation

1. Build the plugin with `./gradlew clean build` or `.\\gradlew.bat clean build` on Windows.
2. Copy `build/libs/KairoQuests-1.0.0.jar` into the server `plugins` folder.
3. Start or restart the Paper server.
4. Edit the created files in `plugins/KairoQuests`.
5. Run `/kairoquests reload` after changing configs.

## Supported Versions

- Java 21
- Paper 1.21 and newer
- Kotlin 2.x

## Roadmap

- MySQL storage option
- Additional quest objective hooks
- More GUI action types
- Seasonal rotation helpers

## License

KairoQuests is released under the MIT License.
