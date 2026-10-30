package de.verdox.mccreativelab.generator.resourcepack.types.menu;

import de.verdox.mccreativelab.wrapper.entity.MCCEffect;
import de.verdox.mccreativelab.wrapper.entity.MCCEntityType;
import de.verdox.mccreativelab.wrapper.entity.types.MCCPlayer;
import de.verdox.mccreativelab.wrapper.entity.player.Input;
import de.verdox.mccreativelab.wrapper.item.MCCItemStack;
import de.verdox.mccreativelab.wrapper.platform.MCCPlatform;
import de.verdox.mccreativelab.wrapper.platform.MCCTask;
import de.verdox.mccreativelab.wrapper.typed.MCCEffects;
import de.verdox.mccreativelab.wrapper.typed.MCCEntityTypes;
import de.verdox.mccreativelab.wrapper.world.MCCLocation;
import de.verdox.mccreativelab.wrapper.world.Weather;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class MenuBehaviour {
    private static final Logger LOGGER = Logger.getLogger(MenuBehaviour.class.getSimpleName());

    private static final int tickCooldown = 3;
    private final MCCPlayer player;
    private final ActiveMenu activeMenu;
    private final BiConsumer<PlayerKeyInput, ActiveMenu> consumer;
    private final Runnable onEnd;
    private MCCTask menuViewTask;
    private MCCTask effectTask;
    private MCCTask checkerTask;
    private MCCLocation locationBefore;
    private final java.util.concurrent.atomic.AtomicBoolean closed = new java.util.concurrent.atomic.AtomicBoolean();
    private int lastTickButtonPressed;
    private int lastScrollTick;
    private int currentScrollMode = 0;
    private int heldSlotBefore;

    public MenuBehaviour(MCCPlayer player, ActiveMenu activeMenu, BiConsumer<PlayerKeyInput, ActiveMenu> consumer, Runnable onEnd) {
        this.onEnd = onEnd;
        this.player = player;
        this.activeMenu = activeMenu;
        this.consumer = consumer;
    }

    public void start() {
        heldSlotBefore = player.getInventory().getHeldItemSlot();
        locationBefore = player.getLocation();

        player.getTempData().storeData("hasMenuOpen", true);

        this.checkerTask = MCCPlatform.getInstance().getTaskManager().runTimerOnTickThread(mccTask -> {
            if (player.isDead() || !player.isOnline()) {
                close();
                return;
            }
            try {
                if (activeMenu.getCustomMenu().doFakeTime)
                    player.getTimeProperty().set(6000L);
                if (activeMenu.getCustomMenu().doFakeWeather)
                    player.getWeatherProperty().set(Weather.CLEAR);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An error occurred in the checker task: " + e.getMessage());
                activeMenu.abort(e);
            }
        }, 0L, 1L, TimeUnit.SECONDS);

        this.menuViewTask = MCCPlatform.getInstance().getTaskManager().runTimerOnTickThread((task) -> {
            try {
                player.getInventory().setHeldItemSlot(4);
                refreshInventory();
            }
            catch (Exception e) {
                LOGGER.log(Level.SEVERE, "An error occurred in the menuViewTask task: " + e.getMessage());
                activeMenu.abort(e);
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS);

        if (activeMenu.getCustomMenu().doEffects) {
            this.effectTask = MCCPlatform.getInstance().getTaskManager().runTimerOnTickThread(mccTask -> {
                try {
                    MCCEffect slowness = MCCEffects.SLOWNESS.get().create(20, 3, false, false, false, null);
                    MCCEffect fatigue = MCCEffects.MINING_FATIGUE.get().create(20, -1, false, false, false, null);
                    MCCEffect blind = MCCEffects.BLINDNESS.get().create(40, 1, false, false, false, null);
                    slowness.apply(player);
                    fatigue.apply(player);
                    blind.apply(player);
                }
                catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "An error occurred in the effect task: " + e.getMessage());
                    activeMenu.abort(e);
                }
            }, 0L, 1, TimeUnit.SECONDS);
        }

        // Put Player in spectator mode
        // Set Spectator target of player. Prevent Sneak Exit.
        if (activeMenu.getCustomMenu().hideOtherPlayers) {
            for (MCCPlayer onlinePlayer : MCCPlatform.getInstance().getOnlinePlayers()) {
                player.getHideProperty().add(onlinePlayer);
            }
        }
    }

    /**
     * Reapplies the client-only menu inventory after server/client inventory updates.
     * sendFakeContents uses player-inventory indices (helmet = 39), not container
     * menu indices (helmet = 5). The real inventory is never modified.
     */
    public void refreshInventory() {
        if (closed.get() || !player.isOnline() || player.isDead())
            return;
        MCCItemStack[] fakeContents = new MCCItemStack[41];
        fakeContents[39] = activeMenu.getActiveBackgroundPicture();
        player.getInventory().sendFakeContents(fakeContents);
    }

    private @Nullable MCCLocation getLocationOnOpen() {
        MCCLocation location = null;

        if (activeMenu.getCustomMenu().doYawPitchLock)
            location = new MCCLocation(player.getLocation().world(), player.getLocation().x() + 0.5, player.getLocation().y(), player.getLocation().z() + 0.5, 0, -90);
        else if (activeMenu.getCustomMenu().doPositionLoc)
            location = new MCCLocation(player.getLocation().world(), player.getLocation().x() + 0.5, player.getLocation().y(), player.getLocation().z() + 0.5, player.getLocation().yaw(), player.getLocation().pitch());
        return location;
    }

    public void close() {
        if (!closed.compareAndSet(false, true))
            return;
        if (menuViewTask != null)
            menuViewTask.cancel();
        if (checkerTask != null)
            checkerTask.cancel();
        if (effectTask != null)
            effectTask.cancel();

        try {
            if (player.isOnline()) {
                if (activeMenu.getCustomMenu().doEffects) {
                    removeMenuEffect(MCCEffects.BLINDNESS.get(), 1, 40);
                    removeMenuEffect(MCCEffects.SLOWNESS.get(), 3, 20);
                    removeMenuEffect(MCCEffects.MINING_FATIGUE.get(), -1, 20);
                }
                player.getInventory().setHeldItemSlot(heldSlotBefore);
                player.syncInventory();
                if (!player.isDead())
                    player.teleport(locationBefore);
                if (activeMenu.getCustomMenu().hideOtherPlayers) {
                    for (MCCPlayer onlinePlayer : MCCPlatform.getInstance().getOnlinePlayers())
                        player.getHideProperty().remove(onlinePlayer);
                }
                if (activeMenu.getCustomMenu().doFakeTime)
                    player.getTimeProperty().sync();
                if (activeMenu.getCustomMenu().doFakeWeather)
                    player.getWeatherProperty().sync();
            }
        } finally {
            player.getTempData().removeData("hasMenuOpen");
            if (onEnd != null)
                onEnd.run();
        }
    }

    private void removeMenuEffect(de.verdox.mccreativelab.wrapper.entity.MCCEffectType type, int amplifier, int duration) {
        MCCEffect effect = type.getActiveEffect(player);
        if (effect != null && effect.getAmplifier() == amplifier && effect.getDuration() >= 0 && effect.getDuration() <= duration)
            type.removeEffect(player);
    }

    public void handleInput(Input input) {
        if (input.isForward())
            triggerKeyInput(PlayerKeyInput.FORWARD);
        if (input.isBackward())
            triggerKeyInput(PlayerKeyInput.BACKWARD);
        if (input.isLeft())
            triggerKeyInput(PlayerKeyInput.LEFT);
        if (input.isRight())
            triggerKeyInput(PlayerKeyInput.RIGHT);
        if (input.isJump())
            triggerKeyInput(PlayerKeyInput.SPACE);
        if (input.isSneak())
            triggerKeyInput(PlayerKeyInput.SNEAK);
        if (input.isSprint())
            triggerKeyInput(PlayerKeyInput.SPRINT);
    }

    public void handleHotBarScrolling(int currentTick, int oldIndex, int newIndex) {
        var difference = currentTick - lastScrollTick;
        lastScrollTick = currentTick;
        if (difference <= 2) {
            if (currentScrollMode == 1)
                consumer.accept(PlayerKeyInput.SCROLL_UP, activeMenu);
            else if (currentScrollMode == -1)
                consumer.accept(PlayerKeyInput.SCROLL_DOWN, activeMenu);
        } else {
            var slotDifference = newIndex - oldIndex;

            if (slotDifference >= 0 && slotDifference <= 3) {
                consumer.accept(PlayerKeyInput.SCROLL_DOWN, activeMenu);
                currentScrollMode = -1;
            } else if (slotDifference <= 0 && slotDifference >= -3) {
                consumer.accept(PlayerKeyInput.SCROLL_UP, activeMenu);
                currentScrollMode = 1;
            } else
                currentScrollMode = 0;
        }
    }

    private void triggerKeyInput(PlayerKeyInput playerKeyInput) {
        consumer.accept(playerKeyInput, activeMenu);
    }

    /**
     * The platform that implements the behavior must make sure that the following is guaranteed:
     * <p> - The player is not allowed to leave the entity they are spectating
     * <p> - When the player leaves the server the original game mode has to be restored
     * <p> -
     */
    protected abstract void capturePlayerInSpectatorMode(MCCPlayer player);

    /**
     * Indicates the implementation that this player is not marked anymore
     */
    protected abstract void freePlayerFromSpectatorMode(MCCPlayer player);

}
