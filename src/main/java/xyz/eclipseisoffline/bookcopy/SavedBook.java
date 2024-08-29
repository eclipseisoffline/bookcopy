package xyz.eclipseisoffline.bookcopy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public record SavedBook(Optional<String> title, List<String> pages) {
    public static final Codec<SavedBook> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.optionalFieldOf("title").forGetter(SavedBook::title),
                    Codec.STRING.listOf().fieldOf("pages").forGetter(SavedBook::pages)
            ).apply(instance, SavedBook::new)
    );
}
