package moe.plushie.armourers_workshop.compatibility.mixin;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.builder.entity.CameraEntity;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Available("[1.21, )")
@Mixin(Camera.class)
public class CameraMixin {

    @ModifyVariable(method = "getMaxZoom", at = @At("HEAD"), argsOnly = true)
    private float aw2$getMaxZoom(float zoom) {
        var camera = Camera.class.cast(this);
        if (camera.getEntity() instanceof CameraEntity cameraEntity) {
            return cameraEntity.getMaxZoom(zoom);
        }
        return zoom;
    }
}
