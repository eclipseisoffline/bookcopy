package xyz.eclipseisoffline.bookcopy;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.ParserUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookCopy implements ClientModInitializer {

    public static final String MOD_ID = "bookcopy";
    public static final Path BOOK_SAVE_PATH = Path.of(MOD_ID);
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(
                ((dispatcher, registryAccess) -> dispatcher.register(
                        ClientCommandManager.literal("bookcopy")
                                .then(ClientCommandManager.literal("import")
                                        .then(ClientCommandManager.argument("name",
                                                        StringArgumentType.word())
                                                .suggests(new BookSuggestionProvider())
                                                .executes(context -> {
                                                    ItemStack book = context.getSource().getPlayer()
                                                            .getMainHandItem();
                                                    if (!book.is(Items.WRITABLE_BOOK)) {
                                                        Message errorMessage = Component.literal("Must hold a book and quill");
                                                        throw new SimpleCommandExceptionType(errorMessage).create();
                                                    }

                                                    ListTag pages;
                                                    try {
                                                        CompoundTag bookNBT = NbtIo.read(
                                                                getBookSavePath().resolve(
                                                                        StringArgumentType.getString(
                                                                                context, "name")));
                                                        if (bookNBT == null) {
                                                            Message errorMessage = Component.literal("Failed reading book file (no NBT data found)");
                                                            throw new SimpleCommandExceptionType(errorMessage).create();
                                                        }

                                                        pages = (ListTag) bookNBT.get("pages");
                                                        if (pages == null) {
                                                            Message errorMessage = Component.literal("Failed reading book file (no page content found)");
                                                            throw new SimpleCommandExceptionType(errorMessage).create();
                                                        }
                                                    } catch (IOException exception) {
                                                        Message errorMessage = Component.literal("Failed reading book file (an error occurred while reading, please check your Minecraft logs)");
                                                        LOGGER.error("Failed reading book file!",
                                                                exception);
                                                        throw new SimpleCommandExceptionType(errorMessage).create();
                                                    }

                                                    List<String> pageStrings = pages.stream()
                                                            .map(Tag::getAsString).toList();
                                                    int slot = context.getSource().getPlayer()
                                                            .getInventory().selected;
                                                    context.getSource().getPlayer().connection.send(
                                                            new ServerboundEditBookPacket(slot,
                                                                    pageStrings, Optional.empty()));
                                                    context.getSource().sendFeedback(
                                                            Component.literal(
                                                                    "Read book from file"));
                                                    return pageStrings.size();
                                                })
                                        )
                                )
                                .then(ClientCommandManager.literal("export")
                                        .then(ClientCommandManager.argument("name",
                                                        StringArgumentType.word())
                                                .executes(context -> {
                                                    ItemStack book = context.getSource().getPlayer()
                                                            .getMainHandItem();
                                                    if (!book.is(Items.WRITABLE_BOOK) && !book.is(
                                                            Items.WRITTEN_BOOK)) {
                                                        Message errorMessage = Component.literal("Must hold a book and quill or written book");
                                                        throw new SimpleCommandExceptionType(errorMessage).create();
                                                    }

                                                    CompoundTag bookCompound = book.getTag();
                                                    if (bookCompound == null
                                                            || bookCompound.get("pages") == null) {
                                                        Message errorMessage = Component.literal("Book has no content");
                                                        throw new SimpleCommandExceptionType(errorMessage).create();
                                                    }

                                                    if (book.is(Items.WRITTEN_BOOK)) {
                                                        ListTag pages = (ListTag) bookCompound.get(
                                                                "pages");
                                                        ListTag newPages = new ListTag();
                                                        assert pages != null;
                                                        pages.forEach(tag -> newPages.add(StringTag.valueOf(
                                                                ParserUtils.parseJson(
                                                                                new StringReader(
                                                                                        tag.getAsString()),
                                                                                ComponentSerialization.CODEC)
                                                                        .getString())));
                                                        bookCompound.remove("pages");
                                                        bookCompound.put("pages", newPages);
                                                    }

                                                    try {
                                                        NbtIo.write(bookCompound,
                                                                getBookSavePath().resolve(
                                                                        StringArgumentType.getString(
                                                                                context, "name")));
                                                    } catch (IOException exception) {
                                                        Message errorMessage = Component.literal("Failed saving book to file (an error occurred while saving, please check your Minecraft logs)");
                                                        LOGGER.error("Failed saving book file!",
                                                                exception);
                                                        throw new SimpleCommandExceptionType(errorMessage).create();
                                                    }

                                                    context.getSource().sendFeedback(
                                                            Component.literal(
                                                                    "Saved book to file"));
                                                    return 0;
                                                })
                                        )
                                )
                )));
    }

    public static Path getBookSavePath() throws IOException {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(BOOK_SAVE_PATH);
        File directory = new File(path.toUri());
        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create book save directory");
            }
        }
        return path;
    }
}
