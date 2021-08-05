package club.sk1er.mods.autogg.tasks.data;


import club.sk1er.mods.autogg.AutoGG;
import com.google.gson.InstanceCreator;

import java.lang.reflect.Type;


/**
 * @author solonovamax
 */
public class ServerInstanceCreator implements InstanceCreator<Server> {
    private final AutoGG autogg;
    
    public ServerInstanceCreator(AutoGG autogg) {
        this.autogg = autogg;
    }
    
    @Override
    public Server createInstance(Type type) {
        return new Server(autogg);
    }
}
