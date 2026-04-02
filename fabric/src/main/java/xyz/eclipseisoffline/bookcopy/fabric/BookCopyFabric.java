package xyz.eclipseisoffline.bookcopy.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import xyz.eclipseisoffline.bookcopy.BookCopy;
import xyz.eclipseisoffline.bookcopy.ClientCommandSource;

import java.nio.file.Path;
import java.util.function.Consumer;

public class BookCopyFabric extends BookCopy implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        initializeClient();
    }

    @Override
    protected void registerClientCommands(Consumer<CommandDispatcher<ClientCommandSource>> registerer) {
        //noinspection unchecked,rawtypes - FabricClientCommandSource implements ClientCommandSource via mixin
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> registerer.accept((CommandDispatcher) dispatcher));
    }

    @Override
    protected Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
