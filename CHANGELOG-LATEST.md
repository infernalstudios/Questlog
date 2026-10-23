### Fixed

- Fixed the player sometimes getting stuck (unable to move or look around) after reloading quests, toggling edit mode, or another player joining a LAN world.
- Fixed quest objectives from before a reload or relog continuing to listen for events in the background.
- Fixed `clickEvent` in JSON text component descriptions doing nothing when clicked (e.g. `run_command`, `open_url`, `copy_to_clipboard`) (@Theawsomelamp).
- Fixed resizing the game window wiping unsaved changes in the quest and chapter editors (sorry Chakyl!).
- Fixed scrollbar appearing on quest descriptions that are exactly 11 lines long.
- Fixed quests using item `components`, entity predicates or component item rewards failing to load on dedicated servers.