package de.verdox.mccreativelab.generator.resourcepack.debug;

import de.verdox.mccreativelab.generator.resourcepack.types.hud.ActiveHud;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.ActiveComponentRendered;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.RenderedElementBehavior;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.element.single.SingleHudText;
import de.verdox.mccreativelab.generator.resourcepack.types.rendered.util.ScreenPosition;
import de.verdox.mccreativelab.util.io.StringAlign;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.util.logging.Logger;

public class DebugHud extends CustomHud {
    private static final Logger LOGGER = Logger.getLogger(DebugHud.class.getSimpleName());

    public DebugHud(Key namespacedKey) {
        super(namespacedKey);

        withText("debug_test", new ScreenPosition(50, 50, 0, 0, 1), StringAlign.Alignment.CENTER, 1f, new RenderedElementBehavior<ActiveHud, SingleHudText.RenderedSingleHudText>() {
            @Override
            public void whileOpen(ActiveHud parentElement, SingleHudText.RenderedSingleHudText element, Audience audience) {
                audience.sendMessage(Component.text("whileOpen"));
                element.setRenderedText(Component.text("Centered Alignment at 50% 50%"));
            }

            @Override
            public void onOpen(ActiveHud parentElement, SingleHudText.RenderedSingleHudText element, Audience audience) {
                audience.sendMessage(Component.text("onOpen"));
            }
        });

        withInitialHudSetup(ActiveComponentRendered::showAll);
    }
}
