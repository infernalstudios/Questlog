package org.infernalstudios.questlog.mixin.fabric;

//? if fabric {
/*import net.minecraft.server.MinecraftServer;
//? if >=1.21 {
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
//?} else
//import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=1.21 {
@Mixin(ServerCommonPacketListenerImpl.class)
//?} else
//@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerGamePacketListenerImplAccessor {
    @Accessor("server")
    MinecraftServer getServer();
}
*///?}
