package club.sk1er.mods.autogg.tasks.data;


public class TriggersSchema {
    private Server[] servers;
    
    // GSON doesn't like when you don't have a no-arg constructor
    public TriggersSchema() {
    }
    
    public TriggersSchema(Server[] servers) {
        this.servers = servers;
    }
    
    public Server[] getServers() {
        return servers;
    }
}
