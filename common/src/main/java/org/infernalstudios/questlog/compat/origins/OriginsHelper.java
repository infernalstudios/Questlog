package org.infernalstudios.questlog.compat.origins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.infernalstudios.questlog.platform.Services;

public final class OriginsHelper {

    private OriginsHelper() {
    }

    public static boolean hasOrigin(ServerPlayer player, ResourceLocation originId) {
        return Services.PLATFORM.hasOrigin(player, originId);
    }
}
