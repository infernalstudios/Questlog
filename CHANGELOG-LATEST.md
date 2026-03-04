### Added

- Added `quest_read` objective.
- Added `block_interact` objective.
- Added `advancement` objective.
- Added `trigger_all` command.
- Added JSON fields for left panel width, right panel width, and panel height.
    - Works on a per-quest basis.

### Removed

- Removed `quests.json`.
    - Quests will now load recursively from valid JSONs in `config/questlog/quests`.

### Changed

- Improved quest details page to a multipanel layout.
- Added field for searching.
- Added categories to the questlog.
- Improved error logging.
- Switched from datapacks to a config folder.
- Invalid quests no longer prevent you from loading worlds.
- Simplified quest definitions to reduce nesting.
    - These will be automatically converted from the legacy format on first load.