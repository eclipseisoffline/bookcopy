package xyz.eclipseisoffline.bookcopy;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;

public class BookSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        List<String> books;
        try {
            books = Arrays.stream(
                            Objects.requireNonNull(
                                    new File(BookCopy.getBookSavePath().toUri()).listFiles()))
                    .filter(File::isFile)
                    .map(File::getName).toList();
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return SharedSuggestionProvider.suggest(books, builder);
    }
}
