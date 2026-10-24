package org.infernalstudios.questlog.core.quests;

public record EditorMetadata(
        String targetFieldKey,
        String targetFieldLabel,
        String amountFieldKey,
        String amountFieldLabel,
        SuggestionType suggestionType
) {
    public EditorMetadata(String targetFieldKey, String targetFieldLabel, String amountFieldKey, SuggestionType suggestionType) {
        this(targetFieldKey, targetFieldLabel, amountFieldKey, null, suggestionType);
    }

    public EditorMetadata(String targetFieldKey, String targetFieldLabel, String amountFieldKey, String amountFieldLabel) {
        this(targetFieldKey, targetFieldLabel, amountFieldKey, amountFieldLabel, SuggestionType.NONE);
    }

    public EditorMetadata(String targetFieldKey, String targetFieldLabel, String amountFieldKey) {
        this(targetFieldKey, targetFieldLabel, amountFieldKey, null, SuggestionType.NONE);
    }

    public enum SuggestionType {
        NONE, BLOCK, ITEM, ENTITY_TYPE, BIOME, DIMENSION, MOB_EFFECT, ENCHANTMENT, QUEST, STRUCTURE, LOOT_TABLE, CUSTOM_STAT, ADVANCEMENT, ORIGIN
    }
}
