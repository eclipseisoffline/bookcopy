# Book Copy

[![Modrinth Version](https://img.shields.io/modrinth/v/jkOtP64i?logo=modrinth&color=008800)](https://modrinth.com/mod/book-copy)
[![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/jkOtP64i?logo=modrinth&color=008800)](https://modrinth.com/mod/book-copy)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/jkOtP64i?logo=modrinth&color=008800)](https://modrinth.com/mod/book-copy)
[![Discord Badge](https://img.shields.io/badge/chat-discord-%235865f2)](https://discord.gg/CNNkyWRkqm)
[![Github Badge](https://img.shields.io/badge/github-bookcopy-white?logo=github)](https://github.com/eclipseisoffline/bookcopy)
![GitHub License](https://img.shields.io/github/license/eclipseisoffline/bookcopy)

This mod adds a simple client-side `/bookcopy` command, which allows you to save written books and
import them later in another book.

Feel free to report any bugs, or suggest new features, at the issue tracker.

## License

This mod is licensed under GNU LGPLv3.

## Donating

If you like this mod, consider [donating](https://buymeacoffee.com/eclipseisoffline).

## Discord

For support and/or any questions you may have, feel free to join [my discord](https://discord.gg/CNNkyWRkqm).

## Version support

| Minecraft Version | Status       |
|-------------------|--------------|
| 1.21.9            | ✅ Current    |
| 1.21.6+7+8        | ✔️ Available |
| 1.21.5            | ✔️ Available |
| 1.21.4            | ✔️ Available |
| 1.21.2+3          | ✔️ Available |
| 1.21+1            | ✅ Current    |
| 1.20.5+6          | ✔️ Available |
| 1.20.4            | ✔️ Available |

I try to keep support up for the latest major and latest minor release of Minecraft. Updates to newer Minecraft
versions may be delayed from time to time, as I do not always have the time to immediately update my mods.

Unsupported versions are still available to download, but they won't receive new features or bugfixes.

## Usage

Mod builds can be found on the releases page, as well as on [Modrinth](https://modrinth.com/mod/book-copy).

The Fabric API is required.

This mod adds one simple command, `/bookcopy`. It can be used as follows:

- `/bookcopy export <name> [<overwrite>]` - exports the contents of the book you're holding to the given filename.
  - Set `overwrite` to true to overwrite books.
- `/bookcopy import <name> [<sign>]` - reads the contents of given filename and writes it to the book you're holding.
  - Set `sign` to true to automatically sign the book after importing it. Only works if the book was saved with a title.

When exporting, you can use a written book or a book and quill to read from. When importing, you can
only use a book and quill, since the contents of a written book can't be updated.

Book contents are stored in the `.minecraft/config/bookcopy` folder. Book contents can be
transferred across worlds.

Below version `0.1.2-1.21.1`, book contents are stored in NBT. In version `0.1.2-1.21.1` support for
reading JSON files was added, by adding `.json` at the end of your save names. JSON books are stored
in the following format:

```json
{
  "title": "Signed book title",
  "pages": [
    "This is a signed book.",
    "This is the second page!",
    "",
    "An empty page then this!",
    "Wow."
  ]
}
```
