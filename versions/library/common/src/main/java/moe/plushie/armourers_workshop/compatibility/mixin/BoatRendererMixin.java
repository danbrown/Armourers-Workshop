package moe.plushie.armourers_workshop.compatibility.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.client.model.IEntityModelProvider;
import moe.plushie.armourers_workshop.api.math.IPoseStack;
import moe.plushie.armourers_workshop.compatibility.client.AbstractPoseStack;
import moe.plushie.armourers_workshop.init.client.ClientWardrobeHandler;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

import manifold.ext.rt.api.auto;

@Available("[1.20, )")
@Mixin(BoatRenderer.class)
public class BoatRendererMixin<T extends Boat> implements IEntityModelProvider<Boat, ListModel<Boat>> {

    @Shadow
    @Final
    private Map<Boat.Type, Pair<ResourceLocation, ListModel<Boat>>> boatResources;

    @Inject(method = "render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;isUnderWater()Z"))
    public void aw2$render(T entity, float p_225623_2_, float partialTicks, PoseStack poseStack, MultiBufferSource renderType, int light, CallbackInfo ci) {
        auto originStack = ClientWardrobeHandler.RENDERING_POSE_STACK;
        if (originStack != null) {
            IPoseStack resultStack = AbstractPoseStack.wrap(poseStack);
            resultStack.last().set(originStack.last());
        }
    }

    @Unique
    @Override
    public ListModel<Boat> getModel(Boat entity) {
        return boatResources.get(entity.getVariant()).getSecond();
    }
}
