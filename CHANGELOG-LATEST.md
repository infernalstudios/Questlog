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