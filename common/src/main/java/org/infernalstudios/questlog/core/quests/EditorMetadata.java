package org.infernalstudios.questlog.core.quests;

public record EditorMetadata(
        String targetFieldKey,
        String targetFieldLabel,
        String amountFieldKey,
        SuggestionType suggestionType
) {
    public EditorMetadata(String targetFieldKey, String targetFieldLabel, String amountFieldKey) {
        this(targetFieldKey, targetFieldLabel, amountFieldKey, SuggestionType.NONE);
    }

    public enum SuggestionType {
        NONE, BLOCK, ITEM, ENTITY_TYPE, BIOME, DIMENSION, MOB_EFFECT, ENCHANTMENT, QUEST, STRUCTURE, LOOT_TABLE, CUSTOM_STAT, ADVANCEMENT, ORIGIN
    }
}
