# GUIs and HUDs for Paper plugins

The pack generator is platform-independent. Current GUI, HUD, menu, inventory, item and player APIs use MCC wrapper types. Java 25 and MCC 26.3 are the current targets. These notes were derived from `CustomGUIBuilder`, `ActiveGUI`, `GUIButton`, `GUIElementBehavior` and the extension's integration while migrating OneBlock.

## Construction and registration

Construct a `CustomGUIBuilder` with an Adventure `Key` and either a supported `MCCMenuType` (for example `MCCMenuTypes.HOPPER`) or a chest row count from 1 to 6. Bukkit's `InventoryType` is not the constructor's type. Register the GUI with the shared resource pack during the plugin's data setup, before pack generation.

A button's `withButtonItem` accepts `ItemTextureData`. Its click callback receives `GUIClickAction` and `ActiveGUI`, rather than a Bukkit `InventoryClickEvent`. `GUIElementBehavior<T extends ActiveGUIElement<?>>` has `onOpen` and `whileOpen` callbacks; preserve conditional visibility and permission checks when converting existing GUIs.

## Player and inventory boundaries

`ActiveGUI.getPlayer()` returns `MCCPlayer`. For Bukkit-specific island, permissions or teleport APIs, convert in the consuming Paper plugin:

```java
Player player = (Player) BukkitAdapter.toBukkit(activeGUI.getPlayer());
```

To open a GUI for a Bukkit player:

```java
gui.createMenuForPlayer(BukkitAdapter.toMcc(player));
```

Nested navigation uses `asNestedGUI(MCCPlayer, ActiveGUI)`; retain the predecessor so back navigation works. Wrapper inventories and items likewise require explicit conversion at Bukkit boundaries. Keep these adapter calls out of the platform-independent generator module.

GUI `whileOpen`, element callbacks and clickable-item animation run on the server
tick thread. They can query permissions and update native inventory slots there;
background work must hand its results back before editing an ActiveGUI. Reopening
an existing ActiveGUI resets the initial-render guard even if its title did not
change, and switching screens removes the viewer from the previous GUI.

For dynamic lists, refresh the source data and remove obsolete clickable slots.
Retaining the original snapshot indefinitely leaves revoked invitations visible.

## Preserving behavior during migration

For each screen, retain its container type and size, blocked slots, visible items, permission checks, click action, update interval, nested predecessor and close behavior. Check both rendering and actions: a visible button can still invoke the wrong action or expose a protected inventory slot. HUD tests should include updates as state changes and stopping after disconnect or plugin shutdown.

Keep asset paths, translation keys and shader layers explicit. Missing textures cannot be repaired by a successful compile. A generated ZIP and correct format metadata prove packaging, while a graphical client is needed to verify layout, textures and fonts. See the sibling extension's `docs/guides/PLUGIN_DEVELOPMENT.md` for module ownership, item setup and lifecycle rules.

## Per-button native item metadata

`GUIButton.Builder.edit(Consumer<MCCItemStack>)` configures the freshly created item before ActiveGUIButton inserts it into each button slot. Configure this in the builder, rather than modifying a shared cached stack. A Paper consumer can call `BukkitAdapter.toBukkit(stack).editMeta(...)` inside the callback; the generator itself remains independent of Bukkit. Each visible slot receives an independent stack. Reusing a button must not duplicate lore or share mutable metadata between players.

OneBlock’s isolated native fixture checks that callbacks run for separate native items and preserve independent lore. These checks cover item metadata; graphical layout and real clicks remain client integration checks.

## Full-screen menu backgrounds

`CustomMenu.withBackgroundPicture(id, asset)` packages the PNG under the existing
`assets/<namespace>/textures/menu/<menu>/background/<id>.png` key. Continue to use
`ActiveMenu.setBackgroundPicture(id)` and `clearBackgroundPicture()` to switch it.
The runtime creates an invisible item with `minecraft:equippable`, `slot=head` and
`camera_overlay` pointing at that texture. This uses Minecraft's camera overlay
in first-person view; the HUD remains separate.

MenuBehaviour sends a client-only inventory every tick and immediately on background
changes. The player-inventory packet uses helmet index 39 (container index 5 is
incorrect here), with 41 inventory entries. The original server inventory, including
the helmet, is never replaced. Closing cancels the refresh tasks and synchronizes
the current real inventory. PaperMenuBehavior cancels inventory clicks, creative
inventory edits, drags, drops and hand swaps while the menu is active. Death and
plugin shutdown close active menus; disconnect cleanup also cancels the tasks.

Verification: MenuOverlayTest covers packet contents, repeated refreshes, image
switching/clearing, close cleanup, offline/dead guards and texture packaging. A
graphical client is still required to check scaling, first-person rendering and
visible behavior during inventory interactions and respawn.

Component reference: [Minecraft 1.21.2 release notes](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-2).
