package club.sk1er.mods.autogg.tasks;


import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.tasks.data.Server;
import club.sk1er.mods.autogg.tasks.data.ServerInstanceCreator;
import club.sk1er.mods.autogg.tasks.data.Trigger;
import club.sk1er.mods.autogg.tasks.data.TriggersSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import gg.essential.api.utils.WebUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Runnable class to fetch the AutoGG triggers on startup.
 *
 * @author ChachyDev
 */
public class RetrieveTriggersTask implements Runnable {
    private static final Logger logger = LogManager.getLogger(RetrieveTriggersTask.class);
    
    private static final String TRIGGERS_URL = "https://static.sk1er.club/autogg/regex_triggers_3.json";
    
    private final AutoGG autoGG;
    
    private final Gson gson;
    
    public RetrieveTriggersTask(AutoGG autoGG) {
        this.autoGG = autoGG;
        gson = new GsonBuilder().registerTypeAdapter(Server.class, new ServerInstanceCreator(autoGG))
                                .create();
    }
    
    /**
     * Runs a task which fetches the triggers JSON from the internet.
     *
     * @author ChachyDev
     */
    @Override
    public void run() {
        try {
            autoGG.setTriggers(gson.fromJson(WebUtil.fetchString(TRIGGERS_URL), TriggersSchema.class));
        } catch (JsonSyntaxException e) {
            // Prevent the game from melting when the triggers are not available
            logger.error("Failed to fetch the AutoGG triggers! This isn't good...", e);
            autoGG.setTriggers(new TriggersSchema(new Server[0]));
        } catch (RuntimeException e) {
            // Prevent the game from melting when the triggers are not available
            logger.error("Runtime exception occurred while fetching AutoGG triggers.", e);
            autoGG.setTriggers(new TriggersSchema(new Server[0]));
        }
        
        logger.info("Registering patterns...");
        for (Server server : autoGG.getTriggers().getServers()) {
            for (Trigger trigger : server.getTriggers()) {
                String pattern = trigger.getPattern();
                autoGG.getPatternHandler().getOrRegisterPattern(pattern);
            }
        }
    }
}
