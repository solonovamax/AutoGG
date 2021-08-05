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

package club.sk1er.autogg;


import club.sk1er.autogg.command.AutoGGCommand;
import club.sk1er.autogg.config.AutoGGConfig;
import club.sk1er.autogg.listener.AutoGGListener;
import club.sk1er.modcore.ModCoreInstaller;
import club.sk1er.mods.core.universal.ChatColor;
import club.sk1er.mods.core.util.MinecraftUtils;
import club.sk1er.mods.core.util.Multithreading;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


@Mod(modid = "autogg", name = "AutoGG", version = AutoGG.VERSION)
public class AutoGG {
    public static final String VERSION = "4.0.3";
    
    public static final Map<String, List<Pattern>> ggRegexes = new HashMap<>();
    
    public static final Map<String, Pattern> otherRegexes = new HashMap<>();
    
    public static final Map<String, String> other = new HashMap<>();
    
    public static final String[] secondaryGGStrings = {
            "Have a good day!",
            "<3",
            "AutoGG By Sk1er!",
            "AutoGG By Sk1er! (modifications by solonovamax)",
            "Good Game",
            "Good Fight",
            "UwU",
            "uwu",
            "Meow :3",
            "OwO",
            "owo",
            "Layla is my pretty princess",
            "Layla is a cutie <3",
            "Saku is a cutie <3",
            "ily Kohi, my bestie :>",
            "Aurora is a cutie <3",
            "ily nymph <3",
            "Trans Rights!",
            "Trans Rights are Human Rights!",
            "Be gay, do crime.",
            "Imagine being a c*shet :face_vomitting:",
            "If you don't respect my trans homies, I'm gonna identify as a fucking problem.",
            "If you don't respect my trans friends, we gonna have a fuckin problem.",
            ":3",
            "I like my men how I like my women, cute, hot, or sexy :)",
            "Why are both men and women so hot?",
//            "Oh, you're a transphobe? Well I was doin your mom last night.",
            "Gay and emotionally unavailable :sunglasses:",
            "Bisexual and emotionally unavailable :sunglasses:",
            "Oh, you're straight? More like cringe.",
            "Yes, there are 93 genders, and every time you complain we add 2 more.",
            "dick ducks and nonbinary fucks",
            "Bitches be like \"gender is what's in your pants\", okay the",
            "Catboys are hot.",
            "If God hated gays, why are they so cute?",
            "LGBTQ+ Rights!",
            "Trans women are women and trans men are men :)",
            "They say being bi doubles your chances to find someone, but in reality it just doubles the rejections.",
            "Give me a sec, gotta recharge the gay."
            // "Do your handlers know you're on the internet unsupervised?",
            // "How the fuck did they get a computer into the monkey enclosure?",
            // "Sorry for your loss, people ain't the best when angry.",
            // "Close fight",
            // "You'd think you're playing fortnite with how you build a whole ass hotel in 0.5 seconds.",
            // "Go back to fortnite.",
            // "If the human body is 75% water, how can you be 100% salt?",
            // "Even a quadriplegic sloth is mechanically better than you",
            // "Your family tree looks more like a ladder.",
            // "More does not mean better, as seen by the number of your chromosomes",
            // "I bet you eat toothpaste.",
            //"Who's Joe?",
            // "DN",
    };
    
    private static final String[] ACCEPTED_CONFIG_VERSIONS = { "2" };
    
    public static boolean validConfigVersion, triggerFetchSuccess = true; // independent of config
    
    public static Map<String, String> triggerMeta;
    
    @Mod.Instance("autogg")
    public static AutoGG instance;
    
    private static JsonObject triggerJson;
    
    private final Logger logger = LogManager.getLogger("AutoGG");
    
    private final String prefix = String.format("%s[AutoGG] %s", ChatColor.BLUE, ChatColor.RESET);
    
    private AutoGGConfig autoGGConfig;
    
    private boolean running;
    
    public static void downloadTriggers(boolean sendChatMsg) {
        Multithreading.runAsync(() -> {
            try {
                validConfigVersion = triggerFetchSuccess = true;
                
                triggerJson = new JsonParser().parse(fetchString("https://static.sk1er.club/autogg/regex_triggers_new.json"))
                                              .getAsJsonObject();
                
                assert Arrays.asList(ACCEPTED_CONFIG_VERSIONS).contains(triggerJson.get("triggers_format").toString());
                
                // black magic; https://stackoverflow.com/a/21720953
                triggerMeta = new Gson().fromJson(triggerJson.get("meta"), new TypeToken<HashMap<String, String>>() {
                }.getType());
                
            } catch (IOException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED +
                                                                       "Unable to fetch triggers! Do you have an internet connection?");
                }
                
                AutoGG.instance.logger.error("Failed to fetch triggers.", e);
                triggerFetchSuccess = false;
                return;
            } catch (JsonSyntaxException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED +
                                                                       ChatColor.BOLD.toString() +
                                                                       "JSON Syntax Error! Contact the mod authors at https://sk1er.club/support-discord if you see this message!");
                }
                
                AutoGG.instance.logger.error(
                        "JSON Syntax Error! Open a ticket in our support server at https://sk1er.club/support-discord.", e);
                triggerFetchSuccess = false;
                return;
            } catch (AssertionError | NullPointerException e) {
                if (sendChatMsg) {
                    MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.RED +
                                                                       "Unsupported triggers version! Please update AutoGG!");
                }
                
                AutoGG.instance.logger.error("Unsupported triggers version! Please update AutoGG!");
                validConfigVersion = false;
                return;
            }
            
            if (sendChatMsg) {
                MinecraftUtils.sendMessage(AutoGG.instance.prefix, ChatColor.GREEN +
                                                                   "Successfully fetched triggers!");
            }
        });
    }
    
    public static Set<String> keySet(JsonObject json) throws NullPointerException {
        try { // some people don't have this function for some reason
            return json.keySet();
        } catch (NoSuchMethodError e) {
            Set<String> keySet = new HashSet<>();
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                keySet.add(entry.getKey());
            }
            return keySet;
        }
    }
    
    @Nullable
    public static String getServerIP() {
        final ServerData serverData = Minecraft.getMinecraft().getCurrentServerData();
        if (serverData != null) {
            // Retrieve the host from the server IP
            return serverData.serverIP.replaceAll("^(.*):\\d{1,5}$", "$1")
                                      .toLowerCase(Locale.ENGLISH);
        }
        
        return null;
    }
    
    @NotNull
    public static String fetchString(String url) throws IOException {
        String content;
        
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (Sk1er AutoGG)");
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setDoOutput(true);
            
            try (InputStream setup = connection.getInputStream()) {
                content = IOUtils.toString(setup, Charset.defaultCharset());
            }
        } catch (Exception e) {
            AutoGG.instance.logger.error("Failed to fetch string.", e);
            throw new IOException("Failed to fetch triggers!");
        }
        
        return content;
    }
    
    @SuppressWarnings("RegExpUnexpectedAnchor")
    private static void setDefaultTriggerData() {
        Pattern nonMatching = Pattern.compile("$^");
        otherRegexes.put("antigg", nonMatching);
        otherRegexes.put("anti_karma", nonMatching);
        other.put("msg", "");
    }
    
    // The following function includes code from org.apache.commons.lang.ArrayUtils
    //
    // They are used within the terms of the Apache License v2.0, which can be viewed at
    // https://apache.org/licenses/LICENSE-2.0.txt
    // Copyright (C) Apache Foundation 2020
    //
    // Modifications: strip out everything that isn't the actual copying part and make it work on internal variables
    private static String[] getAntiGGStrings() {
        String[] primaryStrings = AutoGGListener.getStrings(false);
        String[] secondaryStrings = AutoGGListener.getStrings(true);
        String[] joinedArray = (String[]) Array.newInstance(primaryStrings.getClass().getComponentType(),
                                                            primaryStrings.length + secondaryStrings.length);
        System.arraycopy(primaryStrings, 0, joinedArray, 0, primaryStrings.length);
        System.arraycopy(secondaryStrings, 0, joinedArray, primaryStrings.length, secondaryStrings.length);
        return joinedArray;
    }
    
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModCoreInstaller.initializeModCore(Minecraft.getMinecraft().mcDataDir);
        
        autoGGConfig = new AutoGGConfig();
        autoGGConfig.preload();
        
        ClientCommandHandler.instance.registerCommand(new AutoGGCommand());
        MinecraftForge.EVENT_BUS.register(new AutoGGListener());
        
        downloadTriggers(false);
    }
    
    public boolean works() {
        return validConfigVersion && triggerFetchSuccess;
    }
    
    @NotNull
    public AutoGGConfig getAutoGGConfig() {
        return autoGGConfig;
    }
    
    public void getDataFromDownloadedTriggers() {
        ggRegexes.clear();
        otherRegexes.clear();
        other.clear();
        final JsonObject firstServerObject;
        try {
            firstServerObject = triggerJson.get("servers").getAsJsonObject().get(keySet(
                    triggerJson.get("servers").getAsJsonObject()).iterator().next()).getAsJsonObject();
        } catch (NullPointerException e) {
            setDefaultTriggerData();
            return;
        }
        
        final Set<String> ggOptions = keySet(firstServerObject.get("gg_triggers").getAsJsonObject());
        final Set<String> otherPatternOptions = keySet(firstServerObject.get("other_patterns").getAsJsonObject());
        final Set<String> otherOptions = keySet(firstServerObject.get("other").getAsJsonObject());
        
        for (String s : ggOptions) {
            ggRegexes.put(s, new ArrayList<>());
        }
        
        final String ip = getServerIP();
        if (ip == null) {
            setDefaultTriggerData();
            return;
        }
        
        Set<String> keySet;
        
        try {
            keySet = keySet(triggerJson.get("servers").getAsJsonObject());
        } catch (NullPointerException e) { // if download silently failed
            AutoGG.instance.logger.error("Trigger download silently failed.");
            return;
        }
        
        for (String a : keySet) { // could be made more efficient by pre-building list and compiling on download but
            if (Pattern.compile(a).matcher(ip).matches()) { // there is not an easy way to do it i don't think
                JsonObject data = triggerJson.get("servers").getAsJsonObject().get(a).getAsJsonObject();
                for (String s : ggOptions) {
                    for (JsonElement j : data.get("gg_triggers").getAsJsonObject().get(s).getAsJsonArray()) {
                        ggRegexes.get(s).add(Pattern.compile(j.getAsString()));
                    }
                }
                for (String s : otherPatternOptions) {
                    otherRegexes.put(s, Pattern.compile(data.get("other_patterns")
                                                            .getAsJsonObject()
                                                            .get(s)
                                                            .getAsString()
                                                            .replaceAll("(?<!\\\\)\\$\\{antigg_strings}",
                                                                        String.join("|", getAntiGGStrings()))));
                }
                for (String s : otherOptions) {
                    String p = data.get("other").getAsJsonObject().get(s).toString();
                    other.put(s, p.substring(1, p.length() - 1));
                }
                
                return;
            }
        }
        
        setDefaultTriggerData();
        
    }
    
    @NotNull
    public Logger getLogger() {
        return logger;
    }
    
    @NotNull
    public String getPrefix() {
        return prefix;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void setRunning(boolean running) {
        this.running = running;
    }
}
