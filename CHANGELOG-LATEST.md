### Added

- Added `quest_read` objective.
- Added `block_interact` objective.
- Added `advancement` objective.
- Added `trigger_all` command.

### Removed

- Removed `quests.json`.
    - Quests will now load recursively from valid JSONs in `questlog/quests`

### Changed

- Improved quest details page to a multipanel layout.
- Improved error logging.
- Invalid quests no longer prevent you from loading worlds.
- Simplified quest definitions to reduce nesting.
    - These will be automatically converted from the legacy format on first load.