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

package club.sk1er.autogg.command;


import club.sk1er.autogg.AutoGG;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.mods.core.ModCore;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;


public class AutoGGCommand extends CommandBase {
    
    private static final SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");
    
    private static final DateFormat LOCALE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
    
    private final String prefix = AutoGG.instance.getPrefix();
    
    private static Date parseDate(String date) {
        try {
            return ISO_8601.parse(date);
        } catch (ParseException e) {
            return new Date(0);
        }
    }
    
    @Override
    public String getCommandName() {
        return "autogg";
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            ModCore.getInstance().getGuiHandler().open(AutoGG.instance.getAutoGGConfig().gui());
        } else {
            switch (args[0]) {
                case "refresh": {
                    MinecraftUtils.sendMessage(prefix, ChatColor.YELLOW + "Fetching triggers...");
                    AutoGG.downloadTriggers(true);
                    AutoGGListener.switchTriggerset();
                    break;
                }
                case "triggers": {
                    for (Map.Entry<String, List<Pattern>> entry : AutoGG.ggRegexes.entrySet()) {
                        if (!entry.getValue().isEmpty()) {
                            MinecraftUtils.sendMessage(prefix, ChatColor.AQUA +
                                                               entry.getKey().replaceAll("_", " ").toUpperCase(Locale.ENGLISH) + ":");
                            for (Pattern pattern : entry.getValue()) {
                                MinecraftUtils.sendMessage("  ", pattern.toString());
                            }
                        }
                    }
                    
                    for (Map.Entry<String, Pattern> entry : AutoGG.otherRegexes.entrySet()) {
                        if (!"$^".equals(entry.getValue().toString())) {
                            MinecraftUtils.sendMessage(prefix, ChatColor.AQUA +
                                                               entry.getKey().replaceAll("_", " ").toUpperCase(Locale.ENGLISH) + ": " +
                                                               ChatColor.RESET +
                                                               entry.getValue());
                        }
                    }
                    
                    break;
                }
                case "info": {
                    MinecraftUtils.sendMessage(prefix, String.format("%sMod Version: %s", ChatColor.GREEN, AutoGG.VERSION));
                    try {
                        int triggersSize = AutoGG.ggRegexes.get("triggers").size();
                        int casualTriggersSize = AutoGG.ggRegexes.get("casual_triggers").size();
                        MinecraftUtils.sendMessage(prefix, String.format("%sTriggers Version: %s", ChatColor.GREEN,
                                                                         AutoGG.triggerMeta.get("version")
                                                                                           .replaceAll("\"", "")));
                        MinecraftUtils.sendMessage(prefix, String.format("%sTriggers last updated on %s", ChatColor.GREEN,
                                                                         LOCALE_FORMAT.format(parseDate(
                                                                                 AutoGG.triggerMeta.get("upload_date")
                                                                                                   .replaceAll("\"", "")))));
                        MinecraftUtils.sendMessage(prefix, String.format("%sTriggers info message: %s", ChatColor.GREEN,
                                                                         AutoGG.triggerMeta.get("note").replaceAll("\"", "")));
                        MinecraftUtils.sendMessage(prefix, String.format("%s%s Trigger%s, %d Casual Trigger%s", ChatColor.GREEN,
                                                                         triggersSize, triggersSize == 1 ? "" : "s",
                                                                         casualTriggersSize, casualTriggersSize == 1 ? "" : "s"));
                    } catch (NullPointerException e) {
                        MinecraftUtils.sendMessage(prefix,
                                                   String.format("%sCould not get Trigger Meta! Were the triggers downloaded properly?",
                                                                 ChatColor.RED));
                        AutoGG.instance.getLogger().error("Could not get trigger meta.", e);
                    }
                    
                    break;
                }
                case "credits": {
                    MinecraftUtils.sendMessage(prefix, String.format("%sAutoGG Originally created by 2Pi, continued by Sk1er LLC. " +
                                                                     "Regex update & multi-server support by SirNapkin1334." +
                                                                     "Some random shit by solonovamax.",
                                                                     ChatColor.GREEN));
                    MinecraftUtils.sendMessage(prefix, String.format("%sAdditional special thanks to: LlamaLad7, FalseHonesty," +
                                                                     " DJTheRedstoner, Pluggs and Unextracted!",
                                                                     ChatColor.GREEN));
                    break; // Lots of general help x3, General help, Getting antigg strings x2
                }
                case "toggle": {
                    MinecraftUtils.sendMessage(prefix, (AutoGG.instance.getAutoGGConfig().toggle() ? "En" : "Dis") + "abled AutoGG.");
                    break;
                }
                default: { // thank you asbyth!
                    IChatComponent supportDiscordLink = new ChatComponentText(
                            String.format("%s%sFor support with AutoGG, go to https://sk1er.club/support-discord.", prefix,
                                          ChatColor.GREEN));
                    supportDiscordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                                                                       "https://sk1er.club/support-discord"));
                    supportDiscordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                                       new ChatComponentText(
                                                                                               "Click to join our support Discord.")));
                    
                    IChatComponent discordLink = new ChatComponentText(
                            String.format("%s%sFor the community server for all Sk1er mods, go to https://discord.gg/sk1er.", prefix,
                                          ChatColor.GREEN));
                    discordLink.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                                                                "https://discord.gg/sk1er"));
                    discordLink.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                                new ChatComponentText(
                                                                                        "Click to join our community Discord.")));
                    
                    
                    IChatComponent autoGGConfig = new ChatComponentText(
                            String.format("%s%sTo configure AutoGG, run /autogg.", prefix, ChatColor.GREEN));
                    autoGGConfig.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                                                                 "/autogg"));
                    autoGGConfig.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                                                                 new ChatComponentText("Click to run /autogg.")));
                    
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(supportDiscordLink);
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(discordLink);
                    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(autoGGConfig);
                    MinecraftUtils.sendMessage(prefix, String.format("%sAutoGG Commands: refresh, info, credits, help", ChatColor.GREEN));
                    // help doesn't actually exist but that's our secret
                    break;
                }
            }
        }
    }
    
    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "refresh", "info", "credits", "help") : null;
    }
    
    @Override
    public int getRequiredPermissionLevel() {
        return -1;
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return String.format("/%s [refresh|triggers|info|credits|help]", getCommandName());
    }
}
