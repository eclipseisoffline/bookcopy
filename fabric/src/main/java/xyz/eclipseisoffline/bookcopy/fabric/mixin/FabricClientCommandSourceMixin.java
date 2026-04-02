package xyz.eclipseisoffline.bookcopy.fabric.mixin;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import xyz.eclipseisoffline.bookcopy.ClientCommandSource;

@Mixin(FabricClientCommandSource.class)
public interface FabricClientCommandSourceMixin extends ClientCommandSource {}
