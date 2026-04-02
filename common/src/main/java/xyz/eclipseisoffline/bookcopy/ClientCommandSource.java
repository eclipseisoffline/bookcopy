package xyz.eclipseisoffline.bookcopy;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public interface ClientCommandSource extends SharedSuggestionProvider {

    default void sendSuccess(Component message) {
        getClient().gui.getChat().addClientSystemMessage(message);
        getClient().getNarrator().saySystemChatQueued(message);
    }

    default void sendError(Component message) {
        sendSuccess(Component.empty().append(message).withStyle(ChatFormatting.RED));
    }

    default Minecraft getClient() {
        return Minecraft.getInstance();
    }

    default LocalPlayer getPlayer() {
        return Objects.requireNonNull(getClient().player, "player must not be null");
    }

    default ClientLevel getLevel() {
        return Objects.requireNonNull(getClient().level, "level must not be null");
    }

    static LiteralArgumentBuilder<ClientCommandSource> literal(String literal) {
        return LiteralArgumentBuilder.literal(literal);
    }

    static <T> RequiredArgumentBuilder<ClientCommandSource, T> argument(String argument, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(argument, type);
    }
}
