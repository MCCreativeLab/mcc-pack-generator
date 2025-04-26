package de.verdox.mccreativelab.generator.resourcepack.debug;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.types.menu.CustomMenu;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public class DebugMenu extends CustomMenu {
    public DebugMenu(@NotNull Key namespacedKey) {
        super(namespacedKey);

        withBackgroundPicture("test_background", new Asset<>(() -> DebugMenu.class.getResourceAsStream("/resolution/debug_menu.png")));
    }
}
