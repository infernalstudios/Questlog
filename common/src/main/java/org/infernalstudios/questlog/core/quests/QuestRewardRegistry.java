package org.infernalstudios.questlog.core.quests;

import com.google.gson.JsonObject;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.core.quests.rewards.*;
import org.infernalstudios.questlog.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class QuestRewardRegistry {

    private static final Map<ResourceLocation, Function<JsonObject, Reward>> REGISTRY = new HashMap<>();
    private static final Map<ResourceLocation, EditorMetadata> METADATA = new HashMap<>();

    static {
        register(ResourceLocation.fromNamespaceAndPath("questlog", "item"), ItemReward::new,
                new EditorMetadata("item", "Item ID:", "count", EditorMetadata.SuggestionType.ITEM));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "command"), CommandReward::new,
                new EditorMetadata("command", "Command String:", null));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "experience"), ExperienceReward::new,
                new EditorMetadata(null, null, "experience"));
        register(ResourceLocation.fromNamespaceAndPath("questlog", "loot_table"), LootTableReward::new,
                new EditorMetadata("loot_table", "Loot Table ID:", null, EditorMetadata.SuggestionType.LOOT_TABLE));
    }

    public static Set<ResourceLocation> getRegisteredTypes() {
        return REGISTRY.keySet();
    }

    public static void register(ResourceLocation id, Function<JsonObject, Reward> factory, EditorMetadata metadata) {
        REGISTRY.put(id, factory);
        METADATA.put(id, metadata);
    }

    public static EditorMetadata getMetadata(ResourceLocation id) {
        return METADATA.get(id);
    }

    public static Reward create(JsonObject definition) {
        String typeString = JsonUtils.getString(definition, "type");
        if (!typeString.contains(":")) {
            typeString = "questlog:" + typeString;
        }

        ResourceLocation type;
        try {
            type = ResourceLocation.parse(typeString);
        } catch (ResourceLocationException e) {
            throw new IllegalStateException("Invalid reward type: " + typeString);
        }

        return QuestRewardRegistry.create(type, definition);
    }

    public static Reward create(ResourceLocation type, JsonObject definition) {
        if (!REGISTRY.containsKey(type)) {
            throw new NullPointerException("Reward type not found: " + type);
        }

        try {
            return REGISTRY.get(type).apply(definition);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create reward of type " + type, e);
        }
    }
}
