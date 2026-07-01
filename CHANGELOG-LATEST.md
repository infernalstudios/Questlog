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