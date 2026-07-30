# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.3.1] - 2026-07-30

### Fixed

- Fixed issues with `name` in Rewards.
- Fixed issues with inconsistent sort order.

## [3.3.0] - 2026-07-23

### Added

- Added Component requirements for items.
- Added NBT predicates for entities.

### Fixed

- Fixed Quest order not always properly applying.
- Fixed "icon" field missing from objectives and rewards in in-game quest editor.

## [3.2.0] - 2026-07-01

### Added

- Made the settings tab scrollable to expose the missing quest display options:
    - Panel sizing and offsets
    - Background/right-panel/peripheral/overlay textures
    - Completed/triggered sounds
    - Text colors
    - Button text overrides
    - Toast/popup notification toggles
    - `translatable`
    - Alternate descriptions for completed/failed states

### Fixed

- Fixed the quest editor capping quest descriptions at 256 characters.
- Fixed the quest editor overwriting unsupported fields (e.g. `background_texture`, `toast_on_complete`) with
  defaults on save.
- Fixed the quest editor's "Details Default"/"Details Disabled" settings.
- Fixed the quest editor's "Order" field.

## [3.1.0] - 2026-06-29

### Added

- Added `repeatable` quests.
- Added `global` quests.

### Changed

- Renamed `requirements` to `prerequisites`.
    - Existing quests should still function as expected.

### Fixed

- Fixed issues with `visit_position` quests.
- Fixed issues with player name placeholders in command rewards.

## [3.0.0] - 2026-06-25

### Added

- Added an in-game quest editor.
- Added a `questlog:choice` reward type.
- Item rewards now support NBT tags.
- Added a `questlog:origin` objective for specific Origins mod origins.
- Added a `questlog:and` objective type.
- Added a `/ql reset_all_progress_and_reload` command.

### Changed

- `/ql edit_mode` status now persists on a per-world basis.
- `/ql edit_mode` (with no additional arguments) now toggles edit mode on/off.

### Fixed

- Fixed issues with quest progress not properly resetting.
- Fixed nested objectives not displaying child objectives.
- Fixed issues with Cursors Extended.
- Fixed background double rendering.

## [2.3.1] - 2026-06-02

### Fixed

- Fixed issues with `OR` and `NOT` objectives in `requirements`.

## [2.3.0] - 2026-05-03

### Added

- Added a button to hide completed quests.
- Added `disable_details_button` and `details_open_by_default` fields to the quest definition.

### Changed

- Textures now have additional padding for resource pack purposes.
- `quest_complete` objectives now work retroactively.
- Text that's too long to fit in the UI will now be truncated and show the full text in a tooltip.

### Fixed

- Fixed `quest_complete` objectives being triggerable early.
- Re-implemented per-quest peripheral textures.

## [2.2.1] - 2026-04-09

### Fixed

- Fixed Quests sometimes not loading on dedicated servers.

## [2.2.0] - 2026-04-04

### Added

- Added section for text color configuration.

### Fixed

- Fixed missing default reward and objective localizations.

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