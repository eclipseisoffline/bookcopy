package xyz.eclipseisoffline.bookcopy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BookContent;
import net.minecraft.world.item.component.WrittenBookContent;
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
                                                .executes(context -> importBook(context, StringArgumentType.getString(context, "name"), false))
                                                .then(ClientCommandManager.argument("sign",
                                                        BoolArgumentType.bool())
                                                        .executes(context -> importBook(context, StringArgumentType.getString(context, "name"),
                                                                BoolArgumentType.getBool(context, "sign")))
                                                )
                                        )
                                )
                                .then(ClientCommandManager.literal("export")
                                        .then(ClientCommandManager.argument("name",
                                                        StringArgumentType.word())
                                                .executes(context -> exportBook(context, StringArgumentType.getString(context, "name"), false))
                                                .then(ClientCommandManager.argument("overwrite",
                                                        BoolArgumentType.bool())
                                                        .executes(context -> exportBook(context, StringArgumentType.getString(context, "name"),
                                                                BoolArgumentType.getBool(context, "overwrite")))
                                                )
                                        )
                                )
                )));
    }

    private static int importBook(CommandContext<FabricClientCommandSource> context, String name, boolean sign) throws CommandSyntaxException {
        ItemStack book = context.getSource().getPlayer()
                .getMainHandItem();
        if (!book.is(Items.WRITABLE_BOOK)) {
            throw new SimpleCommandExceptionType(Component.literal("Must hold a book and quill")).create();
        }

        SavedBook savedBook;
        try {
            Dynamic<?> input;
            Path path = getBookSavePath().resolve(name);
            if (!Files.exists(path) || Files.isDirectory(path)) {
                throw new SimpleCommandExceptionType(Component.literal("Failed reading book file (file doesn't exist)")).create();
            }

            if (path.toString().endsWith(".json")) {
                try (Reader reader = new FileReader(path.toFile())) {
                    input = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(reader));
                }
            } else {
                CompoundTag bookNBT = NbtIo.read(
                        getBookSavePath().resolve(
                                StringArgumentType.getString(
                                        context, "name")));
                if (bookNBT == null) {
                    throw new SimpleCommandExceptionType(Component.literal("Failed reading book file (no NBT data found)")).create();
                }
                input = new Dynamic<>(NbtOps.INSTANCE, bookNBT);
            }

            savedBook = SavedBook.CODEC.decode(input).getOrThrow(s -> new SimpleCommandExceptionType(
                    Component.literal("Failed reading saved book data! (" + s + ")")).create()).getFirst();
        } catch (IOException exception) {
            LOGGER.error("Failed reading book file!", exception);
            throw new SimpleCommandExceptionType(
                    Component.literal("Failed reading book file (an error occurred while reading, please check your Minecraft logs)")).create();
        }

        List<String> pageStrings = savedBook.pages();
        int slot = context.getSource().getPlayer().getInventory().selected;
        context.getSource().getPlayer().connection.send(new ServerboundEditBookPacket(slot, pageStrings, sign ? savedBook.title() : Optional.empty()));
        context.getSource().sendFeedback(
                Component.literal("Read book from file"));
        if (sign && savedBook.title().isEmpty()) {
            context.getSource().sendError(Component.literal("Your book wasn't signed because the saved copy didn't have a title saved!"));
            context.getSource().sendError(Component.literal("Please sign the book with the title you'd like, and save it again, for the sign feature to work."));
        }
        return pageStrings.size();
    }

    private static int exportBook(CommandContext<FabricClientCommandSource> context, String name, boolean overwrite) throws CommandSyntaxException {
        ItemStack book = context.getSource().getPlayer()
                .getMainHandItem();
        if (!book.is(Items.WRITABLE_BOOK) && !book.is(Items.WRITTEN_BOOK)) {
            throw new SimpleCommandExceptionType(Component.literal("Must hold a book and quill or written book")).create();
        }

        BookContent<?, ?> bookContent;
        if (book.is(Items.WRITABLE_BOOK)) {
            bookContent = book.get(DataComponents.WRITABLE_BOOK_CONTENT);
        } else {
            bookContent = book.get(DataComponents.WRITTEN_BOOK_CONTENT);
        }

        if (bookContent == null
                || bookContent.pages().isEmpty()) {
            throw new SimpleCommandExceptionType(Component.literal("Book has no content")).create();
        }

        List<?> rawPages = bookContent.pages();
        List<String> pages = new ArrayList<>();
        for (Object page : rawPages) {
            String pageString;
            if (page instanceof Filterable<?> filterablePage) {
                Object notFiltered = filterablePage.get(false);
                if (notFiltered instanceof Component pageComponent) {
                    pageString = pageComponent.getString();
                } else if (notFiltered instanceof String rawString) {
                    pageString = rawString;
                } else {
                    LOGGER.warn("Found unexpected filtered page type {}! If you are not a developer, report this issue on the issue tracker at Github",
                            notFiltered.getClass());
                    continue;
                }
            } else if (page instanceof Component pageComponent) {
                pageString = pageComponent.getString();
            } else if (page instanceof String rawString) {
                pageString = rawString;
            } else {
                LOGGER.warn("Found unexpected page type {}! If you are not a developer, report this issue on the issue tracker at Github",
                        page.getClass());
                continue;
            }
            pages.add(pageString);
        }

        SavedBook saved = new SavedBook(bookContent instanceof WrittenBookContent written ? Optional.of(written.title().raw()) : Optional.empty(), pages);
        try {
            Path savePath = getBookSavePath().resolve(StringArgumentType.getString(context, "name"));
            if (Files.exists(savePath)) {
                if (Files.isDirectory(savePath)) {
                    throw new SimpleCommandExceptionType(Component.literal("Given save name is a directory!")).create();
                } else if (!overwrite) {
                    String command = "/bookcopy export " + name + " true";
                    throw new SimpleCommandExceptionType(Component
                            .literal("Given save name already exists!\n")
                            .append(Component.literal("Run ")
                                    .append(Component.literal(command)
                                            .withStyle(style -> style
                                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                                                    .withColor(ChatFormatting.BLUE)
                                                    .withUnderlined(true))))
                            .append(Component.literal(" to overwrite the saved book"))).create();
                }
            }

            if (savePath.toString().endsWith(".json")) {
                try (Writer writer = new FileWriter(savePath.toFile())) {
                    DataResult<JsonElement> encoded = SavedBook.CODEC.encodeStart(JsonOps.INSTANCE, saved);

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(encoded.getOrThrow(s -> new SimpleCommandExceptionType(Component.literal("Failed to save book file to JSON file (" + s + ")")).create()), writer);
                }
            } else {
                DataResult<Tag> encoded = SavedBook.CODEC.encodeStart(NbtOps.INSTANCE, saved);
                NbtIo.write((CompoundTag) encoded.getOrThrow(s -> new SimpleCommandExceptionType(Component.literal("Failed to save book file to NBT file (" + s + ")")).create()), savePath);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed saving book file!", exception);
            throw new SimpleCommandExceptionType(Component.literal("Failed saving book to file (an error occurred while saving, please check your Minecraft logs)")).create();
        }

        context.getSource().sendFeedback(Component.literal("Saved book to file"));
        return 0;
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
