package club.sk1er.mods.autogg.handlers.patterns;


import java.util.HashMap;
import java.util.Map;


public class PlaceholderAPI {
    public static final PlaceholderAPI INSTANCE = new PlaceholderAPI();
    
    private static final String PLACEHOLDER = "${%s}";
    
    private final Map<String, String> placeholders = new HashMap<>();
    
    public void registerPlaceHolder(String key, String value) {
        placeholders.put(key, value);
    }
    
    public String process(String string) {
        String finalPlaceholder = string;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String placeholder = String.format(PLACEHOLDER, entry.getKey());
            if (finalPlaceholder.contains(placeholder)) {
                finalPlaceholder = finalPlaceholder.replace(placeholder, entry.getValue());
            }
        }
        
        return finalPlaceholder;
    }
}
