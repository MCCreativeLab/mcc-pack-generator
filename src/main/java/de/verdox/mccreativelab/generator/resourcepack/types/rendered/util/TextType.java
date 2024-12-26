package de.verdox.mccreativelab.generator.resourcepack.types.rendered.util;

import de.verdox.mccreativelab.wrapper.inventory.MCCMenuType;
import de.verdox.mccreativelab.wrapper.inventory.MCCMenuTypes;
import de.verdox.mccreativelab.wrapper.inventory.types.menu.MCCChestContainerMenu;

import java.util.HashMap;
import java.util.Map;

public record TextType(int xOffset, int yOffset) {
    private static final Map<MCCMenuType, TextType> invTypes = new HashMap<>();
    private static final Map<Integer, TextType> chestSizes = new HashMap<>();
    private static final Map<TextType, ScreenPosition> topLeftCorner = new HashMap<>();
    public static TextType ACTION_BAR = new TextType(0, 64);
    // Anvil - Left
    public static TextType ANVIL = new TextType(28, 70);
    // Barrel - Left
    public static TextType BARREL = createChestTextType(3);
    // Blast_Furnace - Mid
    public static TextType BLAST_FURNACE = new TextType(0, -72 + 4);
    // Brewing - Mid
    public static TextType BREWING = new TextType(0, -70);
    // Cartography - Left
    public static TextType CARTOGRAPHY_TABLE = new TextType(-80, -70);
    // Chest - Left
    public static TextType CHEST_9x_1 = createChestTextType(1);
    public static TextType CHEST_9x_2 = createChestTextType(2);
    public static TextType CHEST_9x_3 = createChestTextType(3);
    public static TextType CHEST_9x_4 = createChestTextType(4);
    public static TextType CHEST_9x_5 = createChestTextType(5);
    public static TextType CHEST_9x_6 = createChestTextType(6);
    // Dispenser - Mid
    public static TextType DISPENSER = new TextType(0, 70);
    // Dropper - Mid
    public static TextType DROPPER = DISPENSER;
    // Enchanting - Left
    public static TextType ENCHANTING_TABLE = new TextType(-80, -70);
    // EnderChest - Left
    public static TextType ENDER_CHEST = CHEST_9x_3;
    // Furnace - Mid
    public static TextType FURNACE = BLAST_FURNACE;
    // Grindstone - Left
    public static TextType GRIND_STONE = new TextType(-80, -70);
    // Hopper - Left
    public static TextType HOPPER = CHEST_9x_1;
    // Loom - Left
    public static TextType LOOM = new TextType(-80, -72);
    // Merchant - Mid
    public static TextType MERCHANT = new TextType(-10, -70);
    // Player - Left
    public static TextType PLAYER = CHEST_9x_3;
    // ShulkerBox - Left
    public static TextType SHULKER_BOX = CHEST_9x_3;
    // Smithing - Left
    public static TextType SMITHING = new TextType(-28, -70);
    // Smoker - Mid
    public static TextType SMOKER = FURNACE;
    // Stonecutter - Left
    public static TextType STONE_CUTTER = new TextType(-80, -70);
    // Workbench - Left
    public static TextType WORKBENCH = new TextType(40, guiRowHeight(3));


    private static TextType createChestTextType(int size) {
        var type = new TextType(80, guiRowHeight(size));
        chestSizes.put(size, type);
        return type;
    }

    public static TextType getByInventoryType(MCCMenuType type) {
        if(!invTypes.containsKey(type)) {
            throw new IllegalArgumentException("Chest type " + type + " not available");
        }
        return invTypes.get(type);
    }

    public static TextType getByChestSize(int size) {
        if(!chestSizes.containsKey(size))
            throw new IllegalArgumentException("Chest size "+size+" not available");
        return chestSizes.get(size);
    }

    public static ScreenPosition getTopLeftCorner(TextType type) {
        if (type == null)
            return new ScreenPosition(0, 0, 0, 0, 1);
        return topLeftCorner.get(type);
    }

    public static ScreenPosition getTopLeftCorner(MCCMenuType type) {
        return topLeftCorner.get(getByInventoryType(type));
    }

    public static ScreenPosition getTopLeftCorner(int size) {
        return topLeftCorner.get(getByChestSize(size));
    }

    private static int guiRowHeight(int rows) {
        return 45 + (9 * rows) - 2;
    }

    static {
        invTypes.put(MCCMenuTypes.ANVIL, ANVIL);
        invTypes.put(MCCMenuTypes.GENERIC_9x3, BARREL);
        invTypes.put(MCCMenuTypes.BLAST_FURNACE, BLAST_FURNACE);
        invTypes.put(MCCMenuTypes.BREWING_STAND, BREWING);
        invTypes.put(MCCMenuTypes.CARTOGRAPHY_TABLE, CARTOGRAPHY_TABLE);
        invTypes.put(MCCMenuTypes.GENERIC_3x3, DISPENSER);
        invTypes.put(MCCMenuTypes.ENCHANTMENT, ENCHANTING_TABLE);
        invTypes.put(MCCMenuTypes.GRINDSTONE, GRIND_STONE);
        invTypes.put(MCCMenuTypes.HOPPER, HOPPER);
        invTypes.put(MCCMenuTypes.LOOM, LOOM);
        invTypes.put(MCCMenuTypes.MERCHANT, MERCHANT);
        invTypes.put(MCCMenuTypes.SHULKER_BOX, SHULKER_BOX);
        invTypes.put(MCCMenuTypes.SMITHING, SMITHING);
        invTypes.put(MCCMenuTypes.SMOKER, SMOKER);
        invTypes.put(MCCMenuTypes.STONECUTTER, STONE_CUTTER);
        invTypes.put(MCCMenuTypes.CRAFTING, WORKBENCH);

        invTypes.put(MCCMenuTypes.GENERIC_9x1, CHEST_9x_1);
        invTypes.put(MCCMenuTypes.GENERIC_9x2, CHEST_9x_2);
        invTypes.put(MCCMenuTypes.GENERIC_9x4, CHEST_9x_4);
        invTypes.put(MCCMenuTypes.GENERIC_9x5, CHEST_9x_5);
        invTypes.put(MCCMenuTypes.GENERIC_9x6, CHEST_9x_6);

        topLeftCorner.put(CHEST_9x_6, new ScreenPosition(50, 50, -(CHEST_9x_6.xOffset + 8), CHEST_9x_6.yOffset() + 13, 1));
        topLeftCorner.put(CHEST_9x_5, new ScreenPosition(50, 50, -(CHEST_9x_5.xOffset + 8), CHEST_9x_5.yOffset() + 13, 1));
        topLeftCorner.put(CHEST_9x_4, new ScreenPosition(50, 50, -(CHEST_9x_4.xOffset + 8), CHEST_9x_4.yOffset() + 13, 1));
        topLeftCorner.put(CHEST_9x_3, new ScreenPosition(50, 50, -(CHEST_9x_3.xOffset + 8), CHEST_9x_3.yOffset() + 13, 1));
        topLeftCorner.put(CHEST_9x_2, new ScreenPosition(50, 50, -(CHEST_9x_2.xOffset + 8), CHEST_9x_2.yOffset() + 13, 1));
        topLeftCorner.put(CHEST_9x_1, new ScreenPosition(50, 50, -(CHEST_9x_1.xOffset + 8), CHEST_9x_1.yOffset() + 13, 1));


        topLeftCorner.put(ANVIL, new ScreenPosition(50, 50, -(ANVIL.xOffset + 60), ANVIL.yOffset() + 13, 1));
        topLeftCorner.put(BARREL, new ScreenPosition(50, 50, -(BARREL.xOffset + 8), BARREL.yOffset() + 13, 1));
        topLeftCorner.put(BLAST_FURNACE, new ScreenPosition(50, 50, -(BLAST_FURNACE.xOffset + 88), BLAST_FURNACE.yOffset() + 13, 1));
        topLeftCorner.put(BREWING, new ScreenPosition(50, 50, -(BREWING.xOffset + 88), BREWING.yOffset() + 13, 1));
        topLeftCorner.put(CARTOGRAPHY_TABLE, new ScreenPosition(50, 50, -(CARTOGRAPHY_TABLE.xOffset + 8), CARTOGRAPHY_TABLE.yOffset() + 13, 1));
        topLeftCorner.put(DISPENSER, new ScreenPosition(50, 50, -(DISPENSER.xOffset + 88), DISPENSER.yOffset() + 13, 1));
        topLeftCorner.put(DROPPER, new ScreenPosition(50, 50, -(DROPPER.xOffset + 88), DROPPER.yOffset() + 13, 1));
        topLeftCorner.put(ENCHANTING_TABLE, new ScreenPosition(50, 50, -(ENCHANTING_TABLE.xOffset + 8), ENCHANTING_TABLE.yOffset() + 13, 1));
        topLeftCorner.put(ENDER_CHEST, new ScreenPosition(50, 50, -(ENDER_CHEST.xOffset + 8), ENDER_CHEST.yOffset() + 13, 1));
        topLeftCorner.put(GRIND_STONE, new ScreenPosition(50, 50, -(GRIND_STONE.xOffset + 8), GRIND_STONE.yOffset() + 13, 1));
        topLeftCorner.put(HOPPER, new ScreenPosition(50, 50, -(HOPPER.xOffset + 8), HOPPER.yOffset() + 13, 1));
        topLeftCorner.put(LOOM, new ScreenPosition(50, 50, -(LOOM.xOffset + 8), LOOM.yOffset() + 13, 1));

        topLeftCorner.put(MERCHANT, new ScreenPosition(50, 50, -(MERCHANT.xOffset + 88), MERCHANT.yOffset() + 13, 1));

        topLeftCorner.put(PLAYER, new ScreenPosition(50, 50, -(PLAYER.xOffset + 8), PLAYER.yOffset() + 13, 1));
        topLeftCorner.put(SHULKER_BOX, new ScreenPosition(50, 50, -(SHULKER_BOX.xOffset + 8), SHULKER_BOX.yOffset() + 13, 1));

        topLeftCorner.put(SMITHING, new ScreenPosition(50, 50, -(SMITHING.xOffset + 60), SMITHING.yOffset() + 25, 1));

        topLeftCorner.put(STONE_CUTTER, new ScreenPosition(50, 50, -(STONE_CUTTER.xOffset + 8), STONE_CUTTER.yOffset() + 13, 1));
        topLeftCorner.put(WORKBENCH, new ScreenPosition(50, 50, -(WORKBENCH.xOffset + 8), WORKBENCH.yOffset() + 13, 1));
    }
}
