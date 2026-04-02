# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-04-01

### Added

- Added `failures` to quest definitions, to define triggers to enter a fail state.
- Added optional `description_completed` and `description_failed` fields to change Quest descriptions depending on
  state.

### Changed

- Requirements, objectives, and rewards are now all optional.

## [2.0.6] - 2026-03-26

### Fixed

- Fixed Not and Or objectives not triggering with commands.

## [2.0.5] - 2026-03-13

### Fixed

- Fixed dedicated server crash on NeoForge.

## [2.0.4] - 2026-03-12

### Fixed

- Fixed certain quest IDs not migrating from pre-2.0.0 properly.

## [2.0.3] - 2026-03-12

### Fixed

- Fixed crash with certain objectives on Fabric.
- Fixed chapter tab x-offset not working.

## [2.0.2] - 2026-03-11

### Fixed

- Fixed crash on dedicated servers.

## [2.0.1] - 2026-03-10

### Added

- Added new fields for overlay width and offsets in quest definitions.
- Added tooltip when hovering over questlog chapters.

## [2.0.0] - 2026-03-10

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
    - Also converts old player save data to the new format.
- Added field for searching.
- Added categories to the questlog, which are defined in `config/questlog/chapters`.
- Added `overlay` field to quest definitions.
- Added `offset` fields for both right and left panel positioning on a per-quest basis.
- Added support for clickable links in quest descriptions.
- Added support for configurable tooltips and hover effects in quest descriptions.
    - Also supports animated textures.
- Added option for explicit quest sorting order.
- Added a notification badge to the quest button for when you get a new quest.
- Added offset options for positioning all UI elements.
- Added an in-game config screen using Cloth Config.

### Removed

- Removed `quests.json`.
    - Quests will now load recursively from valid JSONs in `config/questlog/quests`.
- Fully removed `item_pickup` objective/trigger.
    - This was previous deprecated and redirected to `item_obtain`. Any existing quests using `item_pickup` will be
      automatically converted.

### Changed

- Fully reworked commands for better autofill and additional functionality.
- Improved quest details page to a multipanel layout.
- Quests without rewards or objectives will only show the description.
- Improved default objective and reward names to be more context-aware than "Unnamed Objective".
- Improved error logging to be more descriptive.
- Switched from datapacks to a config folder.
- Simplified quest definitions to reduce nesting.
- Quests are now sorted alphabetically by default.

### Fixed

- Fixed invalid quests causing player data to not save, resulting in being kicked from worlds.

## [1.1.3] - 2026-02-26

### Fixed

- `item_obtain` objectives no longer cause items to be unstackable.

### Changed

- Deprecated `item_pickup` in favor of `item_obtain`.

## [1.1.2] - 2026-02-21

### Hotfix

- Removed debug quest.

## [1.1.1] - 2026-02-21

### Fixed

- Fixed player data persistence.

## [1.1.0] - 2026-02-11

### Added

- Added `not` and `or` filtering for objectives.

## [1.0.8] - 2026-02-09

### Fixed

- Fixed `/questlog reset` causing server disconnects.