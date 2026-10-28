package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.resourcepack.types.lang.LanguageInfo;
import de.verdox.mccreativelab.generator.resourcepack.types.lang.Translation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageStorageTest {
    @Test
    void initializesCurrentAdventureStoreAndPreservesUnicodeAndFallbacks() {
        var storage = new LanguageStorage(null);
        storage.addTranslation(new Translation(LanguageInfo.GERMAN, "mcc.test", "Grüße aus Köln"));
        assertEquals("Grüße aus Köln", storage.translate("mcc.test", LanguageInfo.GERMAN));
        assertEquals("Grüße aus Köln", storage.translate("mcc.test", LanguageInfo.ENGLISH_US));
        assertEquals("Grüße aus Köln", storage.translate("mcc.test", new LanguageInfo("fr_fr", "", "", false)));
        assertEquals("missing.key", storage.translate("missing.key", LanguageInfo.GERMAN));
    }
}
