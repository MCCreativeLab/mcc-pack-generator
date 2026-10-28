package de.verdox.mccreativelab.generator;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PackFormatTest {
    @Test
    void resourcePackHasExactMajorAndMinorRange() {
        var actual = PackFormat.RESOURCE_26_3.metadata("Resources", 64);
        assertEquals(JsonParser.parseString("{\"description\":\"Resources\",\"min_format\":[97,1],\"max_format\":[97,1]}"), actual);
        assertFalse(actual.has("pack_format"));
    }

    @Test
    void dataPackHasAnExactZeroMinorVersion() {
        var actual = PackFormat.DATA_26_3.metadata("Data", 81);
        assertEquals(JsonParser.parseString("{\"description\":\"Data\",\"min_format\":[121,0],\"max_format\":[121,0]}"), actual);
    }

    @Test
    void legacyFormatsKeepTheirOriginalRepresentation() {
        assertEquals(JsonParser.parseString("{\"description\":\"Old\",\"pack_format\":18}"), new PackFormat(18, 0).metadata("Old", 64));
        assertThrows(IllegalArgumentException.class, () -> new PackFormat(18, 1).metadata("Old", 64));
        assertThrows(IllegalArgumentException.class, () -> new PackFormat(-1, 0));
    }
}
