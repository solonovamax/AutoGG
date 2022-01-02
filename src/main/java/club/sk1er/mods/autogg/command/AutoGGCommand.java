package club.sk1er.mods.autogg.command;


import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.tasks.RetrieveTriggersTask;
import gg.essential.api.EssentialAPI;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.api.commands.SubCommand;
import gg.essential.api.utils.GuiUtil;
import gg.essential.api.utils.Multithreading;
import gg.essential.universal.ChatColor;
import gg.essential.universal.wrappers.message.UTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


public class AutoGGCommand extends Command {
    private static final Logger logger = LogManager.getLogger(AutoGGCommand.class);
    
    private final AutoGG autoGG;
    
    public AutoGGCommand(AutoGG autoGG) {
        super("autogg");
        this.autoGG = autoGG;
        logger.info("AutoGG Command initialized");
    }
    
    @DefaultHandler
    public void handle() {
        GuiUtil.open(Objects.requireNonNull(autoGG.getAutoGGConfig().gui()));
    }
    
    @SubCommand(value = "refresh", description = "Refreshes your loaded triggers.")
    public void refresh() {
        Multithreading.runAsync(new RetrieveTriggersTask(autoGG));
        EssentialAPI.getMinecraftUtil().sendMessage(new UTextComponent(String.format("%sRefreshed triggers!", ChatColor.GREEN)));
    }
}
