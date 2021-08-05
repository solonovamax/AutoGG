package club.sk1er.mods.autogg.detectors.ip;


import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.detectors.IDetector;
import net.minecraft.client.Minecraft;


public class ServerIPDetector implements IDetector {
    private final AutoGG autogg;
    
    public ServerIPDetector(AutoGG autogg) {
        this.autogg = autogg;
    }
    
    @Override
    public boolean detect(String data) {
        return Minecraft.getMinecraft().thePlayer != null &&
               autogg.getPatternHandler()
                     .getOrRegisterPattern(data)
                     .matcher(Minecraft.getMinecraft().getCurrentServerData().serverIP)
                     .matches();
    }
}
