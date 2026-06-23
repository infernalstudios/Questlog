package org.infernalstudios.questlog.core.quests.display;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.infernalstudios.questlog.core.quests.rewards.Reward;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.texture.Blittable;
import org.jetbrains.annotations.Nullable;

public class RewardDisplayData {

    private final Component name;
    @Nullable
    private final Blittable icon;
    @Nullable
    private final ResourceLocation claimSound;
    @Nullable
    private Reward reward;
    private int indentLevel = 0;
    private Component lazyName;

    public RewardDisplayData(JsonObject data) {
        String name = JsonUtils.getOrDefault(data, "name", (String) null);

        if (name == null) {
            this.name = this.generateSmartName(data);
        } else {
            this.name = JsonUtils.getOrDefault(data, "translatable", false) ? Component.translatable(name) : Component.literal(name);
        }
        this.icon = JsonUtils.getIcon(data, "icon");

        String sound = JsonUtils.getOrDefault(data, "claim_sound", (String) null);
        this.claimSound = sound == null ? null : ResourceLocation.tryParse(sound);
    }

    private Component generateSmartName(JsonObject data) {
        String typeStr = JsonUtils.getOrDefault(data, "type", "");
        if (typeStr.isEmpty()) return Component.translatable("questlog.reward.default");

        if (!typeStr.contains(":")) typeStr = "questlog:" + typeStr;
        ResourceLocation type = ResourceLocation.tryParse(typeStr);

        if (type != null) {
            String path = type.getPath();

            if (path.equals("item") && data.has("item") && data.get("item").isJsonPrimitive()) {
                String itemStr = data.get("item").getAsString();
                ResourceLocation itemId = ResourceLocation.tryParse(itemStr);
                if (itemId != null && BuiltInRegistries.ITEM.containsKey(itemId)) {
                    Component itemName = BuiltInRegistries.ITEM.get(itemId).getDescription();
                    int count = JsonUtils.getOrDefault(data, "count", 1);
                    if (count > 1) {
                        return Component.literal(count + "x ").append(itemName);
                    }
                    return itemName;
                }
            } else if (path.equals("experience")) {
                int amount = JsonUtils.getOrDefault(data, "experience", 0);
                boolean levels = JsonUtils.getOrDefault(data, "levels", false);
                if (levels) {
                    return Component.translatable("questlog.reward.default.experience.levels", amount);
                } else {
                    return Component.translatable("questlog.reward.default.experience.points", amount);
                }
            } else if (path.equals("choice")) {
                int pickCount = JsonUtils.getOrDefault(data, "pick_count", 1);
                return Component.translatable("questlog.reward.default.questlog.choice.pick", pickCount);
            }

            return Component.translatable("questlog.reward.default." + type.getNamespace() + "." + path);
        }

        return Component.translatable("questlog.reward.default");
    }

    public void setReward(@Nullable Reward reward) {
        this.reward = reward;
    }

    @Nullable
    public Reward getReward() {
        return this.reward;
    }

    public int getIndentLevel() {
        return this.indentLevel;
    }

    public void setIndentLevel(int indentLevel) {
        this.indentLevel = indentLevel;
    }

    public Component getName() {
        if (this.lazyName == null) {
            if (this.reward instanceof org.infernalstudios.questlog.core.quests.rewards.ItemReward itemReward) {
                net.minecraft.world.item.ItemStack stack = itemReward.getStack();
                if (!stack.isEmpty()) {
                    int count = stack.getCount();
                    Component itemName = stack.getHoverName();
                    if (count > 1) {
                        this.lazyName = Component.literal(count + "x ").append(itemName);
                    } else {
                        this.lazyName = itemName;
                    }
                }
            }
            if (this.lazyName == null) {
                this.lazyName = this.name;
            }
        }

        if (this.reward != null && this.reward.getContainer() != null) {
            boolean isSelected = this.reward.isSelected();
            if (isSelected) {
                int greenColor = 0x529E52;
                try {
                    greenColor = org.infernalstudios.questlog.Questlog.getConfig().colors.completedTextColor;
                } catch (Exception ignored) {}
                final int finalColor = greenColor;
                Component xComponent = Component.literal("x").withStyle(style -> style.withColor(net.minecraft.network.chat.TextColor.fromRgb(finalColor)));
                return Component.literal("[").append(xComponent).append("] ").append(this.lazyName);
            } else {
                return Component.literal("[ ] ").append(this.lazyName);
            }
        }

        return this.lazyName;
    }

    public boolean hasRewarded() {
        if (this.reward == null) {
            throw new IllegalStateException("RewardDisplayData has not been assigned a reward");
        }
        return this.reward.hasRewarded();
    }

    @Nullable
    public Blittable getIcon() {
        if (this.icon == null && this.reward instanceof org.infernalstudios.questlog.core.quests.rewards.ItemReward itemReward) {
            net.minecraft.world.item.ItemStack stack = itemReward.getStack();
            if (!stack.isEmpty()) {
                return new org.infernalstudios.questlog.util.texture.ItemRenderable(stack);
            }
        }
        return this.icon;
    }

    @Nullable
    public SoundEvent getClaimSound() {
        return BuiltInRegistries.SOUND_EVENT.get(this.claimSound);
    }
}