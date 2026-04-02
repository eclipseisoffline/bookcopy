package xyz.eclipseisoffline.bookcopy.fabric.mixin;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.eclipseisoffline.bookcopy.ClientCommandSource;

@Mixin(FabricClientCommandSource.class)
public interface FabricClientCommandSourceMixin extends ClientCommandSource {

    @Shadow
    LocalPlayer getPlayer();

    @Shadow
    ClientLevel getLevel();

    @Override
    default LocalPlayer getLocalPlayer() {
        return getPlayer();
    }

    @Override
    default ClientLevel getClientLevel() {
        return getLevel();
    }
}
