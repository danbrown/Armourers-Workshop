package moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.guide;

import moe.plushie.armourers_workshop.core.armature.Armatures;
import moe.plushie.armourers_workshop.core.client.bake.BakedArmature;
import moe.plushie.armourers_workshop.core.skin.paint.texture.TextureData;
import moe.plushie.armourers_workshop.init.ModTextures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AdvancedBoatGuideRenderer extends AdvancedEntityGuideRenderer {

    public AdvancedBoatGuideRenderer() {
    }

    @Override
    public BakedArmature getArmature() {
        return BakedArmature.defaultBy(Armatures.BOAT);
    }

    @Override
    public TextureData getTexture() {
        return new TextureData(ModTextures.BOAT_DEFAULT.toString(), 128, 64);
    }
}
