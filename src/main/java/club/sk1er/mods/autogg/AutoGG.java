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

package club.sk1er.mods.autogg;


import club.sk1er.mods.autogg.command.AutoGGCommand;
import club.sk1er.mods.autogg.config.AutoGGConfig;
import club.sk1er.mods.autogg.handlers.gg.AutoGGHandler;
import club.sk1er.mods.autogg.handlers.patterns.PlaceholderAPI;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import gg.essential.api.EssentialAPI;
import gg.essential.api.utils.JsonHolder;
import gg.essential.api.utils.Multithreading;
import gg.essential.api.utils.WebUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;


/**
 * Contains the main class for AutoGG which handles trigger schema setting/getting and the main initialization code.
 *
 * @author ChachyDev
 */
@Mod(modid = "autogg", name = "AutoGG", version = "4.1.0")
public class AutoGG {
    private static final Random random = new Random();
    
    private static final String[] secondaryGGStrings = {
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
    };
    
    private static final String[] primaryGGStrings = {
            "gf",
            "gg",
            "GG",
            "Good Game",
            "Good Fight",
            "Good Round! :D"
    };
    
    private static final String[] oldSecondaryGGStrings = {
            "<3",
            "AutoGG By Sk1er!",
            "Have a good day!"
    };
    
    @Mod.Instance
    @SuppressWarnings({ "StaticVariableMayNotBeInitialized", "StaticNonFinalField" })
    public static AutoGG INSTANCE;
    
    private TriggersSchema triggers = null;
    
    private AutoGGConfig autoGGConfig;
    
    @SuppressWarnings("BooleanVariableAlwaysNegated")
    private boolean usingEnglish = false;
    
    public static String[] getSecondaryGGStrings() {
        return secondaryGGStrings;
    }
    
    public static String[] getPrimaryGGStrings() {
        return primaryGGStrings;
    }
    
    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    public static String getPrimaryGGMessage() {
        return primaryGGStrings[INSTANCE.autoGGConfig.getAutoGGPhrase()];
    }
    
    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    public static String getSecondaryGGMessage() {
        return oldSecondaryGGStrings[INSTANCE.autoGGConfig.getAutoGGPhrase2()];
    }
    
    public static String getRandomSecondaryGGMessage() {
        return secondaryGGStrings[random.nextInt(secondaryGGStrings.length)];
    }
    
    @Mod.EventHandler
    public void onFMLPreInitialization(FMLPreInitializationEvent event) {
        Multithreading.runAsync(this::checkUserLanguage);
    }
    
    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        autoGGConfig = new AutoGGConfig();
        autoGGConfig.preload();
        
        Collection<String> joined = new HashSet<>();
        joined.addAll(Arrays.asList(primaryGGStrings));
        joined.addAll(Arrays.asList(oldSecondaryGGStrings));
        
        PlaceholderAPI.INSTANCE.registerPlaceHolder("antigg_strings", String.join("|", joined));
        
        Multithreading.runAsync(new RetrieveTriggersTask());
        MinecraftForge.EVENT_BUS.register(new AutoGGHandler());
        EssentialAPI.getCommandRegistry().registerCommand(new AutoGGCommand());
        
        // fix settings that were moved from milliseconds instead of seconds
        // so users aren't waiting 5000 seconds to send GG
        if (autoGGConfig.getAutoGGDelay() > 5) autoGGConfig.setAutoGGDelay(1);
        if (autoGGConfig.getSecondaryDelay() > 5) autoGGConfig.setSecondaryDelay(1);
    }
    
    @Mod.EventHandler
    public void loadComplete(FMLLoadCompleteEvent event) {
        if (!usingEnglish) {
            EssentialAPI.getNotifications()
                        .push("AutoGG",
                              "We've detected your Hypixel language is not set to English! AutoGG will not work on other languages.\n" +
                              "If this is a mistake, feel free to ignore it.",
                              6);
        }
    }
    
    private void checkUserLanguage() {
        final String username = Minecraft.getMinecraft().getSession().getUsername();
        final JsonHolder json = WebUtil.fetchJSON("https://api.sk1er.club/player/" + username);
        final String language = json.optJSONObject("player").defaultOptString("userLanguage", "ENGLISH");
        this.usingEnglish = "ENGLISH".equals(language);
    }
    
    public TriggersSchema getTriggers() {
        return triggers;
    }
    
    public void setTriggers(TriggersSchema triggers) {
        this.triggers = triggers;
    }
    
    public AutoGGConfig getAutoGGConfig() {
        return autoGGConfig;
    }
}
