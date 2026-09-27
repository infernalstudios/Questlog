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

### Item tooltip example

`quests/item_tooltip_quest.json` shows inline item text and an item objective.
Hover either the linked item label or the objective name to see the default item tooltip.
No recipe viewer or additional objective-name markup is required.

### Item recipe example

`quests/item_recipe_quest.json` opens wooden axe recipes from the description label
or the objective name. Install EMI or JEI to try it; without either, clicks have no action.
`itemLinks.openRecipes` in the client config can disable these clicks.
