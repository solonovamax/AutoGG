package club.sk1er.mods.autogg.tasks.data;


import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.detectors.IDetector;
import club.sk1er.mods.autogg.detectors.branding.ServerBrandingDetector;
import club.sk1er.mods.autogg.detectors.ip.ServerIPDetector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class Server {
    /**
     * This is initialized by {@link ServerInstanceCreator}
     */
    @SuppressWarnings("TransientFieldInNonSerializableClass")
    private final transient AutoGG autogg;
    
    private String name;
    
    private String kind;
    
    private String data;
    
    private String messagePrefix;
    
    private Trigger[] triggers;
    
    private IDetector detector;
    
    public Server(AutoGG autogg) {
        this.autogg = autogg;
    }
    
    public Server(@NotNull String name, @NotNull String kind, @NotNull String data, @NotNull String messagePrefix,
                  @NotNull Trigger[] triggers, @NotNull String[] casualTriggers, @Nullable String antiGGTrigger,
                  @Nullable String antiKarmaTrigger, AutoGG autogg) {
        this(autogg);
        this.name = name;
        this.kind = kind;
        this.data = data;
        this.messagePrefix = messagePrefix;
        this.triggers = triggers;
    }
    
    @NotNull
    public String getName() {
        return name;
    }
    
    @NotNull
    public String getData() {
        return data;
    }
    
    @NotNull
    public Trigger[] getTriggers() {
        return triggers;
    }
    
    public String getMessagePrefix() {
        return messagePrefix;
    }
    
    @NotNull
    public IDetector getDetector() {
        if (detector == null) {
            switch (kind) {
                case "SERVER_BRANDING":
                    detector = new ServerBrandingDetector(autogg);
                    break;
                case "SERVER_IP":
                    detector = new ServerIPDetector(autogg);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Kind '%s' is not a valid type. " +
                                                                     "Must be one of 'SERVER_BRANDING' or 'SERVER_IP'.", kind));
            }
        }
        return detector;
    }
}
