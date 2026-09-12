package org.infernalstudios.questlog.core.quests.objectives.item;

import com.evandev.triggers.Triggers;
import com.evandev.triggers.event.events.TriggerPlayerEvent;
import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.infernalstudios.questlog.Questlog;
import org.infernalstudios.questlog.util.JsonUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ItemEquipObjective extends AbstractItemObjective {

    @Nullable
    private final EquipmentSlot slot;

    private int ticksUntilCheck = 0;

    public ItemEquipObjective(JsonObject definition) {
        super(definition);
        if (definition.has("slot") && !JsonUtils.getString(definition, "slot").trim().isEmpty()) {
            String slotStr = JsonUtils.getString(definition, "slot").trim().toLowerCase(Locale.ROOT);
            EquipmentSlot matched = null;
            for (EquipmentSlot s : EquipmentSlot.values()) {
                if (s.getName().equalsIgnoreCase(slotStr)) {
                    matched = s;
                    break;
                }
            }
            if (matched == null) {
                Questlog.LOGGER.warn("Unknown equipment slot '{}' in objective definition", slotStr);
            }
            this.slot = matched;
        } else {
            this.slot = null;
        }
    }

    @Nullable
    public EquipmentSlot getSlot() {
        return this.slot;
    }

    @Override
    public void registerEventListeners() {
        super.registerEventListeners();
        Triggers.EVENTS.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(TriggerPlayerEvent.Tick event) {
        if (this.isCompleted() || this.getParent() == null) return;
        if (event.player instanceof ServerPlayer player && this.getParent().manager.player.equals(player) && --ticksUntilCheck <= 0) {
            if (this.slot != null) {
                if (this.test(player.getItemBySlot(this.slot))) {
                    this.setUnits(this.getUnits() + 1);
                }
            } else {
                for (EquipmentSlot s : EquipmentSlot.values()) {
                    if (this.test(player.getItemBySlot(s))) {
                        this.setUnits(this.getUnits() + 1);
                        break;
                    }
                }
            }
            ticksUntilCheck = 20;
        }
    }
}
