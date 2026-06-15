# Testing KairoQuests

Use a Paper 1.21+ server running Java 21. Start with a clean `plugins/KairoQuests` folder when testing default files.

## Startup

- Start the server with `KairoQuests-1.0.0.jar` in `plugins`.
- Confirm `config.yml`, `messages.yml`, `quests.yml`, `guis.yml`, and `kairoquests.db` are created.
- Confirm the console shows no startup errors.

## Player Flow

- Join the server and run `/quests`.
- Open daily and weekly quest menus.
- Track a quest with `/quest track daily_miner`.
- Clear tracking with `/quest untrack`.
- Complete `daily_miner` by breaking stone.
- Complete `daily_hunter` by killing zombies.
- Complete `daily_fisher` by catching fish.
- Add a temporary crafting quest in `quests.yml`, reload, and craft the target item.
- Claim a completed reward with `/quest claim <quest>`.
- Restart the server and confirm progress is still saved.

## Admin Flow

- Run `/kairoquests reload`.
- Run `/kairoquests stats <player>`.
- Run `/kairoquests giveprogress <player> <quest> <amount>`.
- Run `/kairoquests setprogress <player> <quest> <amount>`.
- Run `/kairoquests complete <player> <quest>`.
- Run `/kairoquests reset <player>` and `/kairoquests reset <player> <quest>`.
- Run `/kairoquests debug` with and without optional hooks installed.

## Optional Hooks

- Test once without PlaceholderAPI or Vault installed.
- Install PlaceholderAPI and confirm `%kairoquests_total_completed%` resolves.
- Install Vault with an economy plugin and confirm `MONEY` rewards deposit.

## Invalid Configs

- Set an invalid material on a GUI item and confirm the menu still opens with a fallback item.
- Set an invalid sound in a reward and confirm it fails quietly.
- Set an invalid quest type and confirm it loads as `CUSTOM`.
