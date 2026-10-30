package de.verdox.mccreativelab.generator.resourcepack.types.hud.renderer;

import de.verdox.mccreativelab.generator.resourcepack.types.hud.ActiveHud;
import de.verdox.mccreativelab.generator.resourcepack.types.hud.CustomHud;
import net.kyori.adventure.audience.Audience;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

public class HudRendererImpl extends Thread implements HudRenderer {
    private final Map<Object, PlayerHudRendererData> renderingDataCache = new ConcurrentHashMap<>();
    private final LinkedBlockingQueue<TickData> ticksToProcess = new LinkedBlockingQueue<>();
    private volatile boolean isRunning = true;

    public HudRendererImpl() {
        super(null, null, "HudRendererThread");
    }

    public void printLastRender(Audience player){
        if(renderingDataCache.containsKey(player))
            LOGGER.info(renderingDataCache.get(player).getLastRendered().compact().toString());
    }

    @Override
    public ActiveHud getActiveHud(Audience player, CustomHud customHud) {
        PlayerHudRendererData playerHudRendererData = getRendererData(player);
        return playerHudRendererData.getActiveHud(customHud);
    }

    @Override
    public ActiveHud getOrStartActiveHud(Audience player, CustomHud customHud) {
        PlayerHudRendererData playerHudRendererData = getRendererData(player);
        return playerHudRendererData.getOrStartActiveHud(customHud);
    }

    public boolean stopActiveHud(Audience player, CustomHud customHud) {
        return getRendererData(player).removeFromRendering(customHud);
    }

    @Override
    public void forceUpdate(Audience player) {
        getRendererData(player).forceUpdate();
    }

    public void addTickToRenderQueue(Collection<? extends Audience> serverPlayers) {
        ticksToProcess.offer(new TickData(Set.copyOf(serverPlayers)));
    }

    private PlayerHudRendererData getRendererData(Audience player) {
        var helper = de.verdox.mccreativelab.platform.GeneratorPlatformHelper.INSTANCE.get();
        Audience normalized = helper.normalizeHudAudience(player);
        return renderingDataCache.computeIfAbsent(audienceKey(normalized), key -> new PlayerHudRendererData(normalized));
    }

    private Object audienceKey(Audience audience) {
        var uuid = de.verdox.mccreativelab.platform.GeneratorPlatformHelper.INSTANCE.get().getUUIDOfAudience(audience);
        return uuid == null ? audience : uuid;
    }

    @Override
    public void interrupt() {
        isRunning = false;
        super.interrupt();
    }

    @Override
    public void run() {
        while(isRunning){

            try {
                var list = ticksToProcess.take().serverPlayers;
                for (Audience player : list) {
                    getRendererData(player).sendUpdate();
                }
                Set<Object> onlineKeys = new HashSet<>();
                for (Audience player : list)
                    onlineKeys.add(audienceKey(player));
                renderingDataCache.keySet().removeIf(key -> !onlineKeys.contains(key));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, "An error occured in the HudRenderer", e);
            }
        }
    }

    public record TickData(Set<Audience> serverPlayers) {

    }
}
