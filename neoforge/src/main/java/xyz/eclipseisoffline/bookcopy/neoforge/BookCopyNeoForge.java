package xyz.eclipseisoffline.bookcopy.neoforge;

import com.mojang.brigadier.CommandDispatcher;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;
import xyz.eclipseisoffline.bookcopy.BookCopy;
import xyz.eclipseisoffline.bookcopy.ClientCommandSource;

import java.nio.file.Path;
import java.util.function.Consumer;

@Mod(value = BookCopy.MOD_ID, dist = Dist.CLIENT)
public class BookCopyNeoForge extends BookCopy {

    public BookCopyNeoForge() {
        initializeClient();
    }

    @Override
    protected void registerClientCommands(Consumer<CommandDispatcher<ClientCommandSource>> registerer) {
        //noinspection unchecked,rawtypes - ClientCommandSourceStack implements ClientCommandSource via mixin
        NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, event -> registerer.accept((CommandDispatcher) event.getDispatcher()));
    }

    @Override
    protected Path getConfigDir() {
        return FMLLoader.getCurrent().getGameDir().resolve("config");
    }
}
