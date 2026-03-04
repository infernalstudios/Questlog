package org.infernalstudios.questlog.core.quests.objectives.misc;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.infernalstudios.questlog.core.quests.objectives.Objective;
import org.infernalstudios.questlog.event.QuestlogEventBus;
import org.infernalstudios.questlog.event.events.QLPlayerEvent;
import org.infernalstudios.questlog.util.JsonUtils;
import org.infernalstudios.questlog.util.Util;

import java.util.Objects;

public class StatisticObjective extends Objective {

    private final ResourceLocation stat;
    private int statAtStart = 0;
    private boolean retroactive = true;

    public StatisticObjective(JsonObject definition) {
        super(definition);

        ResourceLocation parsedLocation = ResourceLocation.parse(JsonUtils.getString(definition, "stat"));
        this.stat = BuiltInRegistries.CUSTOM_STAT.get(parsedLocation);

        if (definition.has("retroactive")) {
            this.retroactive = JsonUtils.getBoolean(definition, "retroactive");
        }
    }

    @Override
    public void registerEventListeners(QuestlogEventBus bus) {
        super.registerEventListeners(bus);
        bus.addListener(this::onStatAward);
    }

    private void onStatAward(QLPlayerEvent.StatAward event) {
        if (this.isCompleted() || this.getParent() == null) return;

        if (event.player instanceof ServerPlayer player &&
                player == this.getParent().manager.player &&
                event.stat == this.getStat()
        ) {
            this.setUnits(this.getUnits() + event.amount);
        }
    }

    private Stat<ResourceLocation> getStat() {
        return Stats.CUSTOM.get(this.stat);
    }

    private int getStatValue() {
        return Objects.requireNonNull(Util.getStats(Objects.requireNonNull(this.getParent()).manager.player)).getValue(this.getStat());
    }

    @Override
    public void writeInitialData(CompoundTag data) {
        super.writeInitialData(data);
        if (this.retroactive) {
            this.statAtStart = this.getStatValue();
            data.putInt("statAtStart", this.statAtStart);
        }
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag data = super.serialize();
        if (this.retroactive) {
            data.putInt("statAtStart", this.statAtStart);
        }
        return data;
    }

    @Override
    public void deserialize(CompoundTag data) {
        super.deserialize(data);
        if (this.retroactive) {
            this.statAtStart = data.getInt("statAtStart");
        }
    }
}