package xyz.eclipseisoffline.bookcopy.neoforge.mixin;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientCommandSourceStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import xyz.eclipseisoffline.bookcopy.ClientCommandSource;

@Mixin(ClientCommandSourceStack.class)
public abstract class ClientCommandSourceStackMixin extends CommandSourceStack implements ClientCommandSource {

    public ClientCommandSourceStackMixin(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName,
                                         Component displayName, MinecraftServer server, @Nullable Entity entity) {
        super(source, position, rotation, level, permissions, textName, displayName, server, entity);
    }

    @Override
    public void sendFeedback(Component message) {
        sendSuccess(() -> message, false);
    }

    @Override
    public void sendError(Component message) {
        sendFailure(message);
    }
}
