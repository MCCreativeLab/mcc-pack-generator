package de.verdox.mccreativelab.generator.resourcepack.types.gui;

import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import de.verdox.mccreativelab.wrapper.inventory.MCCContainerCloseReason;
import de.verdox.mccreativelab.wrapper.item.MCCItemStack;
import de.verdox.mccreativelab.wrapper.item.MCCItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ClickableItem {

    void tick(int tick, ActiveGUI activeGUI, int index);

    void onAdd(ActiveGUI activeGUI, int index);

    void click(GUIClickAction clickAction, ActiveGUI activeGUI);

    class Impl implements ClickableItem {
        private final List<MCCItemStack> variants;
        private final BiConsumer<GUIClickAction, ActiveGUI> onClick;
        private final Builder builder;
        private transient int lastVariantId;

        protected Impl(List<MCCItemStack> variants, BiConsumer<GUIClickAction, ActiveGUI> onClick, Builder builder) {
            this.variants = variants;
            this.onClick = onClick;
            this.builder = builder;
        }

        public void click(GUIClickAction clickAction, ActiveGUI activeGUI) {
            if (builder.clearGUIStackAndClose) {
                PlayerGUIStack.load(clickAction.getEntityClicking()).clear();
                clickAction.getEntityClicking().closeCurrentInventory(new MCCContainerCloseReason("close_active_gui"));
            } else if (builder.popGUIStack) {
                PlayerGUIStack.load(clickAction.getEntityClicking()).popAndOpenLast(clickAction.getEntityClicking(), activeGUI);
            }
            onClick.accept(clickAction, activeGUI);
        }

        @Override
        public void tick(int tick, ActiveGUI activeGUI, int index) {
            if (variants.size() == 1) {
                return;
            }
            if (tick % 20 == 0) {
                lastVariantId += 1;
                if (lastVariantId >= variants.size()) {
                    lastVariantId = 0;
                }
                showInGui(activeGUI, index, lastVariantId);
            }
        }

        @Override
        public void onAdd(ActiveGUI activeGUI, int index) {
            showInGui(activeGUI, index, 0);
        }

        private void showInGui(ActiveGUI activeGUI, int slot, int variantId) {
            MCCItemStack stack = variants.get(variantId).copy();
            builder.itemSetup.accept(stack);
            activeGUI.getVanillaInventory().setItem(slot, stack);
        }
    }

    class Builder {
        private BiConsumer<GUIClickAction, ActiveGUI> onClick = (inventoryClickEvent, activeGUI) -> {
        };
        private final List<MCCItemStack> variants = new ArrayList<>();
        public boolean popGUIStack = false;
        public boolean clearGUIStackAndClose = false;
        private Consumer<MCCItemStack> itemSetup = mccItemStack -> {
        };

        public Builder(MCCItemStack stack) {
            withItem(stack.copy());
        }

        public Builder(MCCItemType material) {
            withItem(material.createItem());
        }

        public Builder() {

        }

        public Builder withClick(BiConsumer<GUIClickAction, ActiveGUI> onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder edit(Consumer<MCCItemStack> itemSetup) {
            this.itemSetup = itemSetup;
            return this;
        }

        public Builder withItem(MCCItemStack stack) {
            return withItems(stack);
        }

        public Builder withItems(MCCItemStack... stackVariants) {
            for (MCCItemStack stackVariant : stackVariants) {
                this.variants.add(stackVariant.copy());
            }
            return this;
        }

        public Builder createCopy() {
            var copy = new Builder();
            copy.onClick = this.onClick;
            for (MCCItemStack variant : variants) {
                copy.variants.add(variant.copy());
            }
            copy.popGUIStack = this.popGUIStack;
            copy.clearGUIStackAndClose = this.clearGUIStackAndClose;
            copy.itemSetup = this.itemSetup;
            return copy;
        }

        public Builder backToLastScreenOnClick() {
            popGUIStack = true;
            return this;
        }

        public Builder openGUI(Supplier<CustomGUIBuilder> supplyGUI) {
            return withClick((clickAction, activeGUI) -> {
                CustomGUIBuilder customGUIBuilder = supplyGUI.get();
                customGUIBuilder.asNestedGUI(clickAction.getEntityClicking(), activeGUI, activeGUI::copyTemporaryDataFromGUI);
            });
        }

        public Builder openGUI(CustomGUIBuilder customGUIBuilder) {
            return openGUI(() -> customGUIBuilder);
        }

        public Builder closeGUI() {
            clearGUIStackAndClose = true;
            return this;
        }

        public ClickableItem build() {
            if(variants.isEmpty()) {
                withItem(CustomResourcePack.EMPTY_ITEM().createItem());
            }
            return new Impl(variants, onClick, this);
        }
    }

}
