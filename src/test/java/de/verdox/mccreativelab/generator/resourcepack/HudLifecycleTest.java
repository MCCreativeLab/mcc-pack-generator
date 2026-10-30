package de.verdox.mccreativelab.generator.resourcepack;

import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.renderer.PlayerHudRendererData;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HudLifecycleTest {
    @Test
    void repeatedHudLookupKeepsActiveState() {
        var hud = new CustomHud(Key.key("test", "hud")) {
            @Override public void checkInstalled() {}
        };
        var data = new PlayerHudRendererData(Audience.empty());
        var active = data.getOrStartActiveHud(hud);
        for (int i = 0; i < 10; i++)
            assertSame(active, data.getOrStartActiveHud(hud));
        assertTrue(data.removeFromRendering(hud));
        assertNotSame(active, data.getOrStartActiveHud(hud));
    }
}
