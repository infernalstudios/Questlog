package org.infernalstudios.questlog.mixin.fabric.client;

//? if fabric {
/*import net.minecraft.client.Minecraft;
//? if >=1.21 {
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
//?} else
//import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//? if >=1.21 {
@Mixin(ClientCommonPacketListenerImpl.class)
//?} else
//@Mixin(ClientPacketListener.class)
public interface ClientPacketListenerAccessor {
    @Accessor("minecraft")
    Minecraft getMinecraft();
}
*///?}
