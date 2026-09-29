# Questlog Examples

This folder contains a variety of example JSON files for Questlog. These are designed to show you common use-cases and
patterns.

## How to use these examples

1. Open your game's config folder.
2. Navigate to `config/questlog/`.
3. Put the quest JSONs (from `quests/`) into the `quests/` directory.
4. Put the chapter JSONs (from `chapters/`) into the `chapters/` directory.
5. Use `/questlog reload` in-game to see the changes.

## Important Concepts

### Resource Locations

Quest and chapter IDs are derived from their file paths within their respective folders.
For example:

- `quests/linear_1_start.json` -> `questlog:linear_1_start`
- `quests/subfolder/my_quest.json` -> `questlog:subfolder/my_quest`

### Linear Questlines (Quest Dependencies)

To make a quest depend on another, add a `questlog:quest_complete` objective to the `requirements` array of the
dependent quest.

**Example from `linear_2_followup.json`:**

```json
  "requirements": [
    {
      "type": "questlog:quest_complete",
      "quest": "questlog:linear_1_start"
    }
  ]
```

The followup quest will only appear and start tracking once `linear_1_start` is completed.

## Folder Overview

- `chapters/`: Examples for custom UI tabs.
- `quests/`: Examples for different objective types, rewards, fail states, and quest patterns.

