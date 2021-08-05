/*
 * AutoGG - Automatically say a selectable phrase at the end of a game on supported servers.
 * Copyright (C) 2020  Sk1er LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package club.sk1er.autogg.listener;


import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import club.sk1er.vigilance.data.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class AutoGGListener {
    private static final Random random = new Random();
    
    private static String[] autoGGPhrases;
    
    private static String[] autoGGPhrases2;
    
    private final Pattern mineplexPattern = Pattern.compile("^(?:us|eu\\.)?mineplex\\.com$");
    
    private boolean invoked;
    
    private boolean deferGG;
    
    private boolean useDelay;
    
    private boolean mineplex;
    
    public AutoGGListener() {
        try {
            autoGGPhrases = AutoGGConfig.class.getDeclaredField("autoGGPhrase")
                                              .getAnnotation(Property.class)
                                              .options();
        } catch (NoSuchFieldException e) {
            AutoGG.instance.getLogger().error("autoGGPhrase does not exist.", e);
        }
        
        try {
            autoGGPhrases2 = AutoGGConfig.class.getDeclaredField("autoGGPhrase2")
                                               .getAnnotation(Property.class)
                                               .options();
        } catch (NoSuchFieldException e) {
            AutoGG.instance.getLogger().error("autoGGPhrase does not exist.", e);
        }
    }
    
    public static void switchTriggerset() {
        AutoGG.instance.getDataFromDownloadedTriggers();
        Multithreading.schedule(() -> {
            if (!AutoGG.instance.works()) {
                MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(), ChatColor.RED + "" + ChatColor.BOLD +
                                                                        (!AutoGG.validConfigVersion
                                                                         ?
                                                                         "WARNING! Unsupported AutoGG version! Please update AutoGG or it will not work!"
                                                                         :
                                                                         "Warning! Failed fetching triggers! Check your internet connection, and try running " +
                                                                         "/autogg refresh")
                                          );
            }
        }, 300, TimeUnit.MILLISECONDS);
    }
    
    @NotNull
    public static String[] getStrings(boolean second) {
        try {
            return AutoGGConfig.class.getDeclaredField("autoGGPhrase" + (second ? "2" : ""))
                                     .getAnnotation(Property.class).options();
        } catch (NoSuchFieldException e) {
            AutoGG.instance.getLogger().error("autoGGPhrase{} does not exist.", second ? "2" : "", e);
            return new String[0];
        }
    }
    
    public static String getFirstAutoGGMessage() {
        int phrase = AutoGG.instance.getAutoGGConfig().getAutoGGPhrase();
        if (phrase >= 0 && phrase < autoGGPhrases.length) {
            return autoGGPhrases[phrase];
        } else { // invalid config
            throw new RuntimeException("An unknown error occurred parsing config. Try deleting " +
                                       ".minecraft/config/autogg.toml or contacting the mod authors.");
        }
    }
    
    public static String getSecondAutoGGMessage() {
        return AutoGG.secondaryGGStrings[random.nextInt(AutoGG.secondaryGGStrings.length)];
    }
    
    @SubscribeEvent
    public void switchTriggersetWrapper(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        switchTriggerset();
    }
    
    @SubscribeEvent
    public void worldSwap(WorldEvent.Load event) {
        invoked = false; // we can set it on Load since it always follows a load, unless the player disconnects
        Multithreading.schedule(() -> { // in which case it no longer matters
            String ip = AutoGG.getServerIP();
            if (ip == null) {
                mineplex = false;
            } else {
                mineplex = mineplexPattern.matcher(ip).matches();
            }
            String scoreboardTitle;
            try { // this always fails on mineplex but doesn't really matter ¯\_(ツ)_/¯
                scoreboardTitle = EnumChatFormatting.getTextWithoutFormattingCodes(event.world.getScoreboard()
                                                                                              .getObjectiveInDisplaySlot(1)
                                                                                              .getDisplayName());
            } catch (Exception e) {
                end();
                deferGG = false;
                useDelay = false;
                return;
            }
            
            deferGG = "HOLE IN THE WALL".equals(scoreboardTitle) || "CAPTURE THE WOOL".equals(scoreboardTitle);
            useDelay = "SPEED UHC".equals(scoreboardTitle) || "PIXEL PAINTERS".equals(scoreboardTitle);
            
            
            end();
        }, 300, TimeUnit.MILLISECONDS);
    }
    
    @SubscribeEvent // this is where the magic happens
    public void onChat(ClientChatReceivedEvent event) {
        String unformattedText = EnumChatFormatting.getTextWithoutFormattingCodes(
                event.message.getUnformattedText());
        
        if (AutoGG.instance.getAutoGGConfig().isAntiKarmaEnabled() &&
            AutoGG.otherRegexes.get("anti_karma").matcher(unformattedText).matches()) {
            event.setCanceled(true);
            return;
        }
        
        if (invoked && AutoGG.instance.getAutoGGConfig().isAntiGGEnabled() &&
            AutoGG.otherRegexes.get("antigg").matcher(unformattedText).matches()) {
            event.setCanceled(true);
            return;
        }
        
        if (!AutoGG.instance.isRunning()) {
            if (AutoGG.instance.getAutoGGConfig().isCasualAutoGGEnabled()) {
                for (Pattern trigger : AutoGG.ggRegexes.get("casual_triggers")) {
                    if (trigger.matcher(unformattedText).matches()) {
                        AutoGG.instance.setRunning(true);
                        invoked = true;
                        sayGG(false, 0);
                        AutoGG.instance.setRunning(false);
                        Multithreading.schedule(() -> {
                            invoked = false;
                            end();
                        }, 60, TimeUnit.SECONDS);
                        return;
                    }
                }
            }
            
            if (AutoGG.instance.getAutoGGConfig().isAutoGGEnabled()) {
                for (Pattern trigger : AutoGG.ggRegexes.get("triggers")) {
                    if (trigger.matcher(unformattedText).matches()) {
                        if (deferGG) {
                            deferGG = false;
                            return;
                        }
                        AutoGG.instance.setRunning(true);
                        invoked = true;
                        sayGG(true, useDelay ? 240 : 0);
                        return;
                    }
                }
            }
        }
    }
    
    private void sayGG(boolean doSecond, int addedTime) {
        Multithreading.schedule(() -> {
            try {
                Minecraft.getMinecraft()
                        .thePlayer
                        .sendChatMessage(String.format("%s%s", AutoGG.other.get("msg"), getFirstAutoGGMessage()));
                if (AutoGG.instance.getAutoGGConfig().isSecondaryEnabled() && doSecond) {
                    Multithreading.schedule(() -> {
                        try {
                            Minecraft.getMinecraft()
                                    .thePlayer
                                    .sendChatMessage(String.format("%s%s", AutoGG.other.get("msg"), getSecondAutoGGMessage()));
                        } catch (RuntimeException e) {
                            MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(),
                                                       String.format(
                                                               "%sAn error occurred getting secondary string. Check logs for more information.",
                                                               ChatColor.RED));
                            AutoGG.instance.getLogger().error("Failed to get secondary string.", e);
                        } finally {
                            end();
                        }
                    }, AutoGG.instance.getAutoGGConfig().getSecondaryDelay() + 10 + (mineplex ? 590 : 0), TimeUnit.MILLISECONDS);
                }
            } catch (RuntimeException e) {
                MinecraftUtils.sendMessage(AutoGG.instance.getPrefix(),
                                           String.format("%sAn error occurred getting primary string. Check logs for more information.",
                                                         ChatColor.RED));
                AutoGG.instance.getLogger().error("Failed to get primary string.", e);
            } catch (Exception e) {
                AutoGG.instance.getLogger().error("Failed to send AutoGG messages.", e);
            } finally {
                end();
            }
        }, AutoGG.instance.getAutoGGConfig().getAutoGGDelay() + addedTime + (mineplex ? 3000 : 0), TimeUnit.MILLISECONDS);
    }
    
    private void end() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        AutoGG.instance.setRunning(false);
    }
}
