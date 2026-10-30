package de.verdox.mccreativelab.generator.resourcepack.types.font;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

/** Finite pixel advances using the bundled space font's existing glyph allocation. */
public final class SpaceFont {
    private SpaceFont() {}

    public static Component advance(int pixels) {
        StringBuilder glyphs = new StringBuilder();
        long remaining = pixels;
        do {
            int part = (int) Math.max(-8192, Math.min(8192, remaining));
            glyphs.appendCodePoint(0xD0000 + part);
            remaining -= part;
        } while (remaining != 0);
        return Component.text(glyphs.toString()).font(Key.key("space", "default"));
    }
}
