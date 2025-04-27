package de.verdox.mccreativelab.generator.resourcepack.extensions;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.AssetBasedResourcePackResource;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.ResourcePackAssetTypes;
import de.verdox.mccreativelab.generator.resourcepack.types.font.BitMap;
import de.verdox.mccreativelab.generator.resourcepack.types.font.Font;
import de.verdox.mccreativelab.wrapper.entity.types.MCCPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class Fade {
    public static final Key FADE_FONT_KEY = Key.key("mccreativelab", "fade");
    public static final Component FADE_COMPONENT = Component.text("\uE001").font(FADE_FONT_KEY);

    public static void register(CustomResourcePack customResourcePack) {
        Font font = new Font(FADE_FONT_KEY);
        AssetBasedResourcePackResource assetBasedResourcePackResource = new AssetBasedResourcePackResource(FADE_FONT_KEY, new Asset<>(() -> Fade.class.getResourceAsStream("/fade//fade.png")), ResourcePackAssetTypes.TEXTURES, "png");
        font.addBitMap(new BitMap(assetBasedResourcePackResource, 1280, 128, "\uE001"));
        customResourcePack.register(font);
        customResourcePack.register(assetBasedResourcePackResource);
    }

    public static void sendFade(MCCPlayer player, int length) {
        int fadeInLengthSeconds = 1;
        int fadeOutLengthSeconds = 1;
        Title title = Title.title(FADE_COMPONENT, Component.empty(), Title.Times.times(Duration.ofSeconds(fadeInLengthSeconds), Duration.ofSeconds(length), Duration.ofSeconds(fadeOutLengthSeconds)));
        player.showTitle(title);
    }

    public static void sendFade(MCCPlayer player, long ticksStart, long ticksStay, long ticksEnd) {
        Title title = Title.title(FADE_COMPONENT, Component.empty(), Title.Times.times(Duration.ofMillis(50 * ticksStart), Duration.ofMillis(50 * ticksStay), Duration.ofMillis(50 * ticksEnd)));
        player.showTitle(title);
    }
}
