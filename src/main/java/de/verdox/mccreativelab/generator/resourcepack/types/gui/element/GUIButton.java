package de.verdox.mccreativelab.generator.resourcepack.types.gui.element;

import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.generator.resourcepack.types.ItemTextureData;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.ActiveGUI;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.GUIClickAction;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.element.active.ActiveGUIButton;
import de.verdox.mccreativelab.generator.resourcepack.types.gui.element.active.ActiveGUIElement;
import de.verdox.mccreativelab.util.io.StringAlign;
import de.verdox.mccreativelab.wrapper.item.MCCItemType;
import de.verdox.mccreativelab.wrapper.typed.MCCItems;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;

public class GUIButton implements GUIElement {
    private final @NotNull ItemTextureData buttonItem;
    private final Asset<CustomResourcePack> buttonTexture;
    private final int startIndex;
    private final int xSize;
    private final int ySize;
    private final StringAlign.Alignment buttonTextAlignment;
    private final float textScale;
    private final boolean playClickSound;
    @Nullable
    private final BiConsumer<GUIClickAction, ActiveGUI> onClick;
    private final String textureID;
    private final String textID;

    private GUIButton(Builder builder, String textureID, String textID){
        this.playClickSound = builder.playClickSound;
        this.buttonItem = builder.buttonItem;
        this.buttonTextAlignment = builder.buttonTextAlignment;
        this.textScale = builder.textScale;
        this.buttonTexture = builder.buttonTexture;
        this.startIndex = builder.startIndex;
        this.xSize = builder.xSize;
        this.ySize = builder.ySize;
        this.onClick = builder.onClick;
        this.textureID = textureID;
        this.textID = textID;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public ItemTextureData getButtonItem() {
        return buttonItem;
    }

    public String getTextID() {
        return textID;
    }

    public String getTextureID() {
        return textureID;
    }

    public StringAlign.Alignment getButtonTextAlignment() {
        return buttonTextAlignment;
    }

    public float getTextScale() {
        return textScale;
    }

    public Asset<CustomResourcePack> getButtonTexture() {
        return buttonTexture;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public BiConsumer<GUIClickAction, ActiveGUI> getOnClick() {
        return onClick;
    }

    @Override
    public ActiveGUIElement<?> toActiveElement(ActiveGUI activeGUI) {
        return new ActiveGUIButton(activeGUI, this);
    }

    public static class Builder {
        private @NotNull ItemTextureData buttonItem = new ItemTextureData(Key.key("mccreativelab","dummy"), MCCItems.STONE.get(), 0, null, null);
        private final int startIndex;
        private @Nullable Asset<CustomResourcePack> buttonTexture;
        private int xSize = 1;
        private int ySize = 1;
        private StringAlign.Alignment buttonTextAlignment = StringAlign.Alignment.LEFT;
        private float textScale = 1f;
        private boolean playClickSound = true;
        @Nullable
        private BiConsumer<GUIClickAction, ActiveGUI> onClick;
        public Builder(int startIndex){
            this.startIndex = startIndex;
        }

        public Builder withButtonItem(@NotNull ItemTextureData buttonItem) {
            Objects.requireNonNull(buttonItem);
            this.buttonItem = buttonItem;
            return this;
        }

        public Builder playClickSound(boolean playClickSound) {
            this.playClickSound = playClickSound;
            return this;
        }

        public Builder withButtonTextAlignment(StringAlign.Alignment buttonTextAlignment) {
            this.buttonTextAlignment = buttonTextAlignment;
            return this;
        }

        public Builder withTextScale(float textScale) {
            this.textScale = textScale;
            return this;
        }

        public Builder withButtonTexture(Asset<CustomResourcePack> buttonTexture) {
            this.buttonTexture = buttonTexture;
            return this;
        }

        public Builder withXSize(int xSize) {
            this.xSize = xSize;
            return this;
        }

        public Builder withYSize(int ySize) {
            this.ySize = ySize;
            return this;
        }

        public Builder withClick(BiConsumer<GUIClickAction, ActiveGUI> onClick) {
            this.onClick = onClick;
            return this;
        }

        public GUIButton build(String textureID, String textID){
            return new GUIButton(this, textureID, textID);
        }
    }
}
