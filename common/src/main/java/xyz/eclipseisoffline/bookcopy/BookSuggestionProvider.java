package xyz.eclipseisoffline.bookcopy;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;

public class BookSuggestionProvider implements SuggestionProvider<ClientCommandSource> {
    private final Path bookSavePath;

    public BookSuggestionProvider(Path bookSavePath) {
        this.bookSavePath = bookSavePath;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ClientCommandSource> context, SuggestionsBuilder builder) {
        List<String> books;
        books = Arrays.stream(Objects.requireNonNull(bookSavePath.toFile().listFiles()))
                .filter(File::isFile)
                .map(File::getName).toList();
        return SharedSuggestionProvider.suggest(books, builder);
    }
}
