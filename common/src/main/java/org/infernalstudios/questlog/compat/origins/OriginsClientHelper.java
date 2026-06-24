package org.infernalstudios.questlog.compat.origins;

import net.minecraft.resources.ResourceLocation;
import org.infernalstudios.questlog.platform.Services;

import java.util.Collection;

/**
 * Client-side helper for retrieving all registered origin IDs for editor auto-complete.
 */
public final class OriginsClientHelper {

    private OriginsClientHelper() {
    }

    public static Collection<ResourceLocation> getOriginKeys() {
        return Services.PLATFORM.getOriginIds();
    }
}
