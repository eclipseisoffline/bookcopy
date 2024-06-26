# Book Copy

![Modrinth Version](https://img.shields.io/modrinth/v/jkOtP64i?logo=modrinth&color=008800)
![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/jkOtP64i?logo=modrinth&color=008800)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/jkOtP64i?logo=modrinth&color=008800)

This mod adds a simple client-side `/bookcopy` command, which allows you to save written books and
import them later in another book.

Feel free to report any bugs, or suggest new features, at the issue tracker.

## License

This mod is licensed under GNU GPLv3.

## Usage

Mod builds can be found [here](https://github.com/eclipseisoffline/bookcopy/packages/2096411) and on [Modrinth](https://modrinth.com/mod/book-copy).

This mod is currently available for Minecraft 1.21, 1.20.5+6/1.20.4 (no longer updated) and 1.20.1 with Fabric loader 0.15.11 or later.
Version port requests can be made at the issue tracker. The Fabric API is required.

This mod adds one simple command, `/bookcopy`. It can be used as follows:

- `/bookcopy export <name>` - exports the contents of the book you're holding to the given filename.
- `/bookcopy import <name>` - reads the contents of given filename and writes it to the book you're holding.

When exporting, you can use a written book or a book and quill to read from. When importing, you can
only use a book and quill, since the contents of a written book can't be updated.

Book contents are stored in the NBT format at the `.minecraft/config/bookcopy` folder. Book contents
can be transferred across worlds.
