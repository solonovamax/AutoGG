package club.sk1er.mods.autogg.config;


import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;

import java.io.File;


@SuppressWarnings("FieldMayBeFinal")
public class AutoGGConfig extends Vigilant {
    @Property(
            type = PropertyType.SWITCH, name = "AutoGG",
            description = "Entirely toggles AutoGG",
            category = "General", subcategory = "General"
    )
    private boolean autoGGEnabled = true;
    
    @Property(
            type = PropertyType.SWITCH, name = "Casual AutoGG",
            description = "Enable AutoGG for things that don't give Karma such as Skyblock Events.",
            category = "General", subcategory = "General"
    )
    private boolean casualAutoGGEnabled;
    
    @Property(
            type = PropertyType.SWITCH, name = "Anti GG",
            description = "Remove GG messages from chat.",
            category = "General", subcategory = "Miscellaneous"
    )
    private boolean antiGGEnabled;
    
    @Property(
            type = PropertyType.SWITCH, name = "Anti Karma",
            description = "Remove Karma messages from chat.",
            category = "General", subcategory = "Miscellaneous"
    )
    private boolean antiKarmaEnabled;
    
    @Property(
            type = PropertyType.SLIDER, name = "Delay",
            description = "Delay after the game ends to say the message.\n§eMeasured in seconds.",
            category = "General", subcategory = "General",
            max = 8
    )
    private int autoGGDelay = 1;
    
    @Property(
            type = PropertyType.SELECTOR, name = "Phrase",
            description = "Choose what message is said on game completion.",
            category = "General", subcategory = "General",
            options = { "gg", "GG", "gf", "Good Game", "Good Fight", "Good Round! :D" }
    )
    private int autoGGPhrase = 0;
    
    @Property(
            type = PropertyType.SWITCH, name = "Second Message",
            description = "Enable a secondary message to send after your first GG. The message is randomized.",
            category = "General", subcategory = "Secondary Message"
    )
    private boolean secondaryEnabled;
    
    @Property(
            type = PropertyType.SLIDER, name = "Second Message Delay",
            description = "Delay between the first & second end of game messages.\n§eMeasured in seconds.",
            category = "General", subcategory = "Secondary Message",
            max = 8
    )
    private int secondaryDelay = 1;
    
    public AutoGGConfig() {
        super(new File("./config/autogg.toml"));
        initialize();
    }
    
    public void setAutoGGDelay(int autoGGDelay) {
        this.autoGGDelay = autoGGDelay;
    }
    
    public boolean isCasualAutoGGEnabled() {
        return casualAutoGGEnabled;
    }
    
    public boolean isAntiGGEnabled() {
        return antiGGEnabled;
    }
    
    public boolean isAntiKarmaEnabled() {
        return antiKarmaEnabled;
    }
    
    public int getAutoGGDelay() {
        return autoGGDelay;
    }
    
    public boolean isAutoGGEnabled() {
        return autoGGEnabled;
    }
    
    public int getAutoGGPhrase() {
        return autoGGPhrase;
    }
    
    public boolean isSecondaryEnabled() {
        return secondaryEnabled;
    }
    
    public int getSecondaryDelay() {
        return secondaryDelay;
    }
    
    public void setSecondaryDelay(int secondaryDelay) {
        this.secondaryDelay = secondaryDelay;
    }
}
