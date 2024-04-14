package xyz.eclipseisoffline.bookcopy;

import com.mojang.brigadier.Message;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BookContent;
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
                                                        LOGGER.error("Failed reading book file!", exception);
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

                                                    CompoundTag bookCompound = new CompoundTag();

                                                    BookContent<?, ?> bookContent;
                                                    if (book.is(Items.WRITABLE_BOOK)) {
                                                        bookContent = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
                                                    } else {
                                                        bookContent = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
                                                    }

                                                    if (bookContent == null
                                                            || bookContent.pages().isEmpty()) {
                                                        Message errorMessage = Component.literal("Book has no content");
                                                        throw new SimpleCommandExceptionType(errorMessage).create();
                                                    }


                                                    List<?> pages = bookContent.pages();
                                                    ListTag pagesTag = new ListTag();
                                                    for (Object page : pages) {
                                                        String pageString;
                                                        if (page instanceof Filterable<?> filterablePage) {
                                                            Object notFiltered = filterablePage.get(false);
                                                            if (notFiltered instanceof Component pageComponent) {
                                                                pageString = pageComponent.getString();
                                                            } else if (notFiltered instanceof String rawString) {
                                                                pageString = rawString;
                                                            } else {
                                                                LOGGER.warn(
                                                                        "Found unexpected filtered page type {}! If you are not a developer, report this issue on the issue tracker at Github",
                                                                        notFiltered.getClass());
                                                                continue;
                                                            }
                                                        } else if (page instanceof Component pageComponent) {
                                                            pageString = pageComponent.getString();
                                                        } else if (page instanceof String rawString) {
                                                            pageString = rawString;
                                                        } else {
                                                            LOGGER.warn(
                                                                    "Found unexpected page type {}! If you are not a developer, report this issue on the issue tracker at Github",
                                                                    page.getClass());
                                                            continue;
                                                        }
                                                        pagesTag.add(StringTag.valueOf(pageString));
                                                    }
                                                    bookCompound.put("pages", pagesTag);

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
