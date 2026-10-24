package org.infernalstudios.questlog.compat.origins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.platform.Services;

/**
 * Server-side helper for checking whether a player has a specific origin.
 */
public final class OriginsHelper {

    private OriginsHelper() {
    }

    public static boolean hasOrigin(ServerPlayer player, ResourceLocation originId) {
        return Services.PLATFORM.hasOrigin(player, originId);
    }
}
