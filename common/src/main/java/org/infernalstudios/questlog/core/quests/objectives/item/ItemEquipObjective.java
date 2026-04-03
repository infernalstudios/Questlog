package org.infernalstudios.questlog.core.quests.objectives.item;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.infernalstudios.questlog.util.JsonUtils;

public class ItemEquipObjective extends AbstractItemObjective {

    private final EquipmentSlot slot;

    private int ticksUntilCheck = 0;

    public ItemEquipObjective(JsonObject definition) {
        super(definition);
        this.slot = EquipmentSlot.byName(JsonUtils.getString(definition, "slot"));
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            if (this.test(player.getItemBySlot(this.slot))) {
                this.setUnits(this.getUnits() + 1);
            }
            ticksUntilCheck = 20;
        }
    }
}
