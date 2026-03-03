### Added

- Added `quest_read` objective.
- Added `advancement` objective.
- Added `trigger_all` command.

### Removed

- Removed `quests.json`.
    - Quests will now load recursively from valid JSONs in `questlog/quests`

### Changed

- Improved error logging.
- Invalid quests no longer prevent you from loading worlds.
- Simplified quest definitions.
    - These will be automatically converted from the legacy format on first load.