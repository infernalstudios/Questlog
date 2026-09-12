### Added

- Added a `questlog:random` reward type, which rolls one of several weighted entries when the reward is claimed.

### Changed

- `slot` is now an optional field in the item equip quests.

### Fixed

- Fixed player quest progress being wiped when another mod saves the player during login.
- Fixed missing item equip slot field in the in-game editor.
- Fixed repeatable quests not resetting when quest rewards are collected.
- Fixed quest rewards and choices being hidden when the details button is disabled.
- Fixed details button displaying when details are open by default.
- Fixed raw `%s` placeholder displaying for effect and quest completed objectives.
- Fixed "Toast on Unlock" triggering on objective progress instead of when requirements are met.
- Fixed "Popup on Unlock" not triggering.
- Fixed "Effect Added" objectives not completing.
- Fixed quest membership desync in the chapter editor when "In Main Chapter" is enabled.