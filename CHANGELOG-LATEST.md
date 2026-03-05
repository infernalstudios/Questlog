### Added

- Added `quest_read` objective.
- Added `block_interact` objective.
- Added `advancement` objective.
- Added JSON fields for left panel width, right panel width, and panel height.
    - Works on a per-quest basis.
- Added support for hot-reloading quests with `/ql reload`.
- Added `ql open` command to force a player to open their questlog (optionally to a specific entry).
- Added `ql progress` command to reset and manage questlog progress. Supports target selectors (`@s`, `@a`, etc.)
- Added `ql trigger` command to trigger quests/chapters. Supports target selectors.
- Added automatic conversion from datapack to config format for quests.

### Removed

- Removed `quests.json`.
    - Quests will now load recursively from valid JSONs in `config/questlog/quests`.
- Fully removed `item_pickup` objective/trigger.
    - This was previous deprecated and redirected to `item_obtain`. Any existing quests using `item_pickup` will be
      automatically converted.

### Changed

- Fully reworked commands for better autofill and additional functionality.
- Improved quest details page to a multipanel layout.
- Added field for searching.
- Added categories to the questlog, which are defined in `config/questlog/chapters`.
- Quests without rewards or objectives will only show the description.
- Improved default objective and reward names to be more context-aware than "Unnamed Objective".
- Improved error logging to be more descriptive.
- Switched from datapacks to a config folder.
- Simplified quest definitions to reduce nesting.
    - These will be automatically converted from the legacy format on first load.

### Fixed

- Fixed invalid quests causing player data to not save, resulting in being kicked from worlds.