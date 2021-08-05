package club.sk1er.mods.autogg.handlers.gg;


import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import gg.essential.api.utils.Multithreading;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;


/**
 * Where the magic happens... We handle which server's triggers should be used and how to detect which server the player is currently on.
 *
 * @author ChachyDev
 */
public class AutoGGHandler {
    private final AutoGG autoGG;
    
    @Nullable
    private volatile Server server;
    
    public AutoGGHandler(AutoGG autoGG) {
        this.autoGG = autoGG;
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity == Minecraft.getMinecraft().thePlayer && autoGG.getAutoGGConfig().isAutoGGEnabled()) {
            Multithreading.runAsync(() -> {
                for (Server triggerServer : autoGG.getTriggers().getServers()) {
                    try {
                        if (triggerServer.getDetector().detect(triggerServer.getData())) {
                            this.server = triggerServer;
                            return;
                        }
                    } catch (Throwable e) {
                        // Stop log spam
                    }
                }
                
                // In case if it's not null and we couldn't find the triggers for the current server.
                server = null;
            });
        }
    }
    
    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        String stripped = EnumChatFormatting.getTextWithoutFormattingCodes(event.message.getUnformattedText());
    
        Server currentServer = this.server;
    
        if (autoGG.getAutoGGConfig().isAutoGGEnabled() && currentServer != null) {
            for (Trigger trigger : currentServer.getTriggers()) {
                switch (trigger.getType()) {
                    case ANTI_GG:
                        if (autoGG.getAutoGGConfig().isAntiGGEnabled()) {
                            if (autoGG.getPatternHandler().getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                event.setCanceled(true);
                                return;
                            }
                        }
                        break;
                    case ANTI_KARMA:
                        if (autoGG.getAutoGGConfig().isAntiKarmaEnabled()) {
                            if (autoGG.getPatternHandler().getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                event.setCanceled(true);
                                return;
                            }
                        }
                        break;
                }
            }
            
            Multithreading.runAsync(() -> {
                // Casual GG feature
                for (Trigger trigger : currentServer.getTriggers()) {
                    switch (trigger.getType()) {
                        case NORMAL:
                            if (autoGG.getPatternHandler().getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                invokeGG();
                                return;
                            }
                            break;
    
                        case CASUAL:
                            if (autoGG.getAutoGGConfig().isCasualAutoGGEnabled()) {
                                if (autoGG.getPatternHandler().getOrRegisterPattern(trigger.getPattern()).matcher(stripped).matches()) {
                                    invokeGG();
                                    return;
                                }
                            }
                            break;
                    }
                }
            });
        }
    }
    
    private void invokeGG() {
        // Better safe than sorry
        Server currentServer = server;
        
        if (currentServer != null) {
            String prefix = currentServer.getMessagePrefix();
    
            String ggMessage = autoGG.getPrimaryGGMessage();
            int delay = autoGG.getAutoGGConfig().getAutoGGDelay();
    
            Multithreading.schedule(() -> {
                EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        
                player.sendChatMessage(prefix.isEmpty() ? ggMessage : String.format("%s %s", prefix, ggMessage));
            }, delay, TimeUnit.SECONDS);
    
            if (autoGG.getAutoGGConfig().isSecondaryEnabled()) {
                String secondGGMessage = autoGG.getRandomSecondaryGGMessage();
                int secondaryDelay = autoGG.getAutoGGConfig().getSecondaryDelay();
        
                Multithreading.schedule(() -> {
                    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
            
                    player.sendChatMessage(prefix.isEmpty() ? ggMessage : String.format("%s %s", prefix, secondGGMessage));
                }, secondaryDelay, TimeUnit.SECONDS);
            }
        }
    }
}
