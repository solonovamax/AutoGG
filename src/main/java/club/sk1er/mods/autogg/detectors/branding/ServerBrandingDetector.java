package club.sk1er.mods.autogg.detectors.branding;


import club.sk1er.mods.autogg.AutoGG;
import club.sk1er.mods.autogg.detectors.IDetector;
import net.minecraft.client.Minecraft;


public class ServerBrandingDetector implements IDetector {
    private final AutoGG autoGG;
    
    public ServerBrandingDetector(AutoGG autoGG) {
        this.autoGG = autoGG;
    }
    
    @Override
    public boolean detect(String data) {
        return Minecraft.getMinecraft().thePlayer != null &&
               autoGG.getPatternHandler()
                     .getOrRegisterPattern(data)
                     .matcher(Minecraft.getMinecraft().thePlayer.getClientBrand())
                     .matches();
    }
}
