package org.infernalstudios.questlog.core.quests.rewards;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.core.quests.Quest;
import org.infernalstudios.questlog.core.quests.display.RewardDisplayData;
import org.infernalstudios.questlog.core.quests.display.WithDisplayData;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.NbtSaveable;
import org.jetbrains.annotations.Nullable;

public abstract class Reward implements NbtSaveable, WithDisplayData<RewardDisplayData> {

    private final RewardDisplayData display;
    private final boolean autoClaim;
    private Quest parent;
    private boolean rewarded = false;

    public Reward(JsonObject definition) {
        if (definition.has("auto_claim")) {
            if (!definition.get("auto_claim").isJsonPrimitive()) {
                throw new IllegalStateException("Reward auto_claim must be a boolean");
            }
            this.autoClaim = JsonUtils.getBoolean(definition, "auto_claim");
        } else {
            this.autoClaim = false;
        }

        this.display = new RewardDisplayData(definition);
        this.display.setReward(this);
    }

    @Nullable
    public Quest getParent() {
        return this.parent;
    }

    public final void setParent(Quest parent) {
        this.parent = parent;
    }

    @Override
    @Nullable
    public RewardDisplayData getDisplay() {
        return this.display;
    }

    public boolean isAutoClaim() {
        return this.autoClaim;
    }

    public void applyReward(ServerPlayer player) {
        this.rewarded = true;
        if (this.getParent() != null) {
            this.getParent().markForUpdate();
        }
    }

    public void revokeReward() {
        this.rewarded = false;
        if (this.getParent() != null) {
            this.getParent().markForUpdate();
        }
    }

    public boolean hasRewarded() {
        return this.rewarded;
    }

    @Override
    public void writeInitialData(CompoundTag data) {
        data.putBoolean("rewarded", false);
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("rewarded", this.rewarded);
        return tag;
    }

    @Override
    public void deserialize(CompoundTag data) {
        this.rewarded = data.getBoolean("rewarded");
    }
}