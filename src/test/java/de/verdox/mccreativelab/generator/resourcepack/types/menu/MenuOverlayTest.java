package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import de.verdox.mccreativelab.wrapper.entity.types.MCCPlayer;
import de.verdox.mccreativelab.wrapper.inventory.types.container.MCCPlayerInventory;
import de.verdox.mccreativelab.wrapper.item.MCCItemStack;
import de.verdox.mccreativelab.wrapper.platform.MCCPlatform;
import de.verdox.mccreativelab.wrapper.platform.MCCTask;
import de.verdox.mccreativelab.wrapper.platform.MCCTaskManager;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import de.verdox.mccreativelab.generator.Asset;
import de.verdox.mccreativelab.generator.AssetPath;
import de.verdox.mccreativelab.generator.resourcepack.CustomResourcePack;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MenuOverlayTest {
    @Test
    void inventoryRefreshFailureAbortsMenuInsteadOfLeavingItsTasksRunning() {
        var platform = mock(MCCPlatform.class);
        var tasks = mock(MCCTaskManager.class);
        var player = mock(MCCPlayer.class, RETURNS_DEEP_STUBS);
        var menu = mock(ActiveMenu.class);
        var custom = new CustomMenu(Key.key("test", "broken_overlay"))
                .setDoEffects(false).setDoFakeTime(false).setDoFakeWeather(false);
        when(menu.getCustomMenu()).thenReturn(custom);
        when(player.isOnline()).thenReturn(true);
        when(platform.getTaskManager()).thenReturn(tasks);
        var callbacks = new ArrayList<Consumer<MCCTask>>();
        when(tasks.runTimerOnTickThread(any(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(call -> { callbacks.add(call.getArgument(0)); return mock(MCCTask.class); });
        var error = new IllegalStateException("inventory packet failed");
        var inventory = player.getInventory();
        doThrow(error).when(inventory).sendFakeContents(any());
        try (var globals = mockStatic(MCCPlatform.class)) {
            globals.when(MCCPlatform::getInstance).thenReturn(platform);
            behaviour(player, menu, () -> {}).start();
            callbacks.get(1).accept(mock(MCCTask.class));
            verify(menu).abort(error);
        }
    }

    @Test
    void refreshUsesHelmetSlotAndNeverWritesRealInventory() {
        var player = mock(MCCPlayer.class);
        var inventory = mock(MCCPlayerInventory.class);
        var menu = mock(ActiveMenu.class);
        var first = mock(MCCItemStack.class);
        var second = mock(MCCItemStack.class);
        when(player.isOnline()).thenReturn(true);
        when(player.getInventory()).thenReturn(inventory);
        when(menu.getActiveBackgroundPicture()).thenReturn(first, second, null);
        var behaviour = behaviour(player, menu, () -> {});

        behaviour.refreshInventory();
        behaviour.refreshInventory();
        behaviour.refreshInventory();

        var contents = ArgumentCaptor.forClass(MCCItemStack[].class);
        verify(inventory, times(3)).sendFakeContents(contents.capture());
        List<MCCItemStack[]> frames = contents.getAllValues();
        assertSame(first, frames.get(0)[39]);
        assertSame(second, frames.get(1)[39]);
        assertNull(frames.get(2)[39]);
        for (var frame : frames) {
            assertEquals(41, frame.length);
            for (int slot = 0; slot < frame.length; slot++)
                if (slot != 39) assertNull(frame[slot]);
        }
        verifyNoMoreInteractions(inventory);
    }

    @Test
    void tickReappliesOverlayAndCloseRestoresInventoryOnce() {
        var platform = mock(MCCPlatform.class);
        var tasks = mock(MCCTaskManager.class);
        var player = mock(MCCPlayer.class, RETURNS_DEEP_STUBS);
        var menu = mock(ActiveMenu.class);
        var onEnd = mock(Runnable.class);
        var checker = mock(MCCTask.class);
        var view = mock(MCCTask.class);
        var custom = new CustomMenu(Key.key("test", "overlay"))
                .setDoEffects(false).setDoFakeTime(false).setDoFakeWeather(false);
        when(menu.getCustomMenu()).thenReturn(custom);
        when(player.isOnline()).thenReturn(true);
        when(player.getInventory().getHeldItemSlot()).thenReturn(2);
        when(platform.getTaskManager()).thenReturn(tasks);
        List<Consumer<MCCTask>> callbacks = new ArrayList<>();
        when(tasks.runTimerOnTickThread(any(), anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(call -> {
                    callbacks.add(call.getArgument(0));
                    return callbacks.size() == 1 ? checker : view;
                });
        try (var globals = mockStatic(MCCPlatform.class)) {
            globals.when(MCCPlatform::getInstance).thenReturn(platform);
            var behaviour = behaviour(player, menu, onEnd);
            behaviour.start();
            var overlay = mock(MCCItemStack.class);
            when(menu.getActiveBackgroundPicture()).thenReturn(overlay);
            callbacks.get(1).accept(view);
            callbacks.get(1).accept(view);
            verify(player.getInventory(), times(2)).sendFakeContents(any());

            behaviour.close();
            behaviour.close();
            behaviour.refreshInventory();
            verify(view).cancel();
            verify(checker).cancel();
            verify(player.getInventory()).setHeldItemSlot(2);
            verify(player).syncInventory();
            verify(onEnd).run();
            verify(player.getInventory(), times(2)).sendFakeContents(any());
        }
    }

    @Test
    void deadOrOfflinePlayersReceiveNoFakeInventory() {
        var player = mock(MCCPlayer.class);
        var behaviour = behaviour(player, mock(ActiveMenu.class), () -> {});
        behaviour.refreshInventory();
        when(player.isOnline()).thenReturn(true);
        when(player.isDead()).thenReturn(true);
        behaviour.refreshInventory();
        verify(player, never()).getInventory();
    }

    @Test
    void backgroundRegistrationAndPackagingNeedNoPlatform(@TempDir Path directory) throws Exception {
        var pack = mock(CustomResourcePack.class);
        when(pack.mainFolder()).thenReturn("assets");
        when(pack.getPathToSavePackDataTo()).thenReturn(AssetPath.buildPath(directory.toString()));
        when(pack.getPathOfOldPack()).thenReturn(AssetPath.buildPath(directory.resolve("old").toString()));
        byte[] png = java.util.Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jF9kAAAAASUVORK5CYII=");
        var menu = new CustomMenu(Key.key("test", "main"));
        menu.withBackgroundPicture("first", new Asset<>(() -> new ByteArrayInputStream(png)));
        assertEquals(Key.key("test", "menu/main/background/first"), menu.getBackgroundPictureKey().get("first"));
        assertThrows(IllegalArgumentException.class,
                () -> menu.withBackgroundPicture("first", new Asset<>(() -> new ByteArrayInputStream(png))));
        menu.build(pack);
        assertArrayEquals(png, Files.readAllBytes(directory.resolve("assets/test/textures/menu/main/background/first.png")));
    }

    private MenuBehaviour behaviour(MCCPlayer player, ActiveMenu menu, Runnable onEnd) {
        return new MenuBehaviour(player, menu, (input, active) -> {}, onEnd) {
            @Override protected void capturePlayerInSpectatorMode(MCCPlayer player) {}
            @Override protected void freePlayerFromSpectatorMode(MCCPlayer player) {}
        };
    }
}
