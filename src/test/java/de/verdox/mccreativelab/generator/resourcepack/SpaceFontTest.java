package de.verdox.mccreativelab.generator.resourcepack;

import com.google.gson.JsonParser;
import de.verdox.mccreativelab.generator.resourcepack.types.font.SpaceFont;
import net.kyori.adventure.text.TextComponent;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

class SpaceFontTest {
    @Test
    void emittedSpacingResolvesToExactFiniteAdvancesWithoutTranslations() throws Exception {
        var advances = new HashMap<Integer, Double>();
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/space/font/default.json"), StandardCharsets.UTF_8)) {
            for (var value : JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("providers")) {
                var provider = value.getAsJsonObject();
                assertEquals("space", provider.get("type").getAsString());
                provider.getAsJsonObject("advances").entrySet().forEach(entry -> {
                    double width = entry.getValue().getAsDouble();
                    assertTrue(Double.isFinite(width));
                    advances.put(entry.getKey().codePointAt(0), width);
                });
            }
        }
        for (int pixels : new int[]{-20000, -8192, -101, -1, 0, 1, 101, 8192, 20000}) {
            var component = (TextComponent) SpaceFont.advance(pixels);
            assertEquals("space:default", component.font().asString());
            assertEquals((double) pixels, component.content().codePoints().mapToDouble(code -> advances.get(code)).sum());
        }
    }
}
