package moe.plushie.armourers_workshop.core.client.render;

import com.apple.library.uikit.UIColor;
import moe.plushie.armourers_workshop.api.client.IBufferSource;
import moe.plushie.armourers_workshop.api.math.IPoseStack;
import moe.plushie.armourers_workshop.compatibility.client.renderer.AbstractBlockEntityRenderer;
import moe.plushie.armourers_workshop.core.blockentity.HologramProjectorBlockEntity;
import moe.plushie.armourers_workshop.core.client.other.SkinItemSource;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderTesselator;
import moe.plushie.armourers_workshop.core.data.ticket.Tickets;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.init.ModDebugger;
import moe.plushie.armourers_workshop.utils.ShapeTesselator;
import moe.plushie.armourers_workshop.utils.TickUtils;
import moe.plushie.armourers_workshop.utils.math.OpenQuaternionf;
import moe.plushie.armourers_workshop.utils.math.Rectangle3f;
import moe.plushie.armourers_workshop.utils.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HologramProjectorBlockRenderer<T extends HologramProjectorBlockEntity> extends AbstractBlockEntityRenderer<T> {

    public HologramProjectorBlockRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(T entity, float partialTicks, IPoseStack poseStack, IBufferSource bufferSource, int light, int overlay) {
        if (!entity.isPowered()) {
            return;
        }
        var blockState = entity.getBlockState();
        var itemStack = entity.getItem(0);
        var descriptor = SkinDescriptor.of(itemStack);
        var context = SkinRenderTesselator.create(descriptor, Tickets.RENDERER);
        if (context == null) {
            return;
        }
        var f = 1 / 16f;
        var animationTicks = TickUtils.animationTicks();
        var overLight = light;
        if (entity.isOverrideLight()) {
            overLight = 0xf000f0;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.rotate(entity.getRenderRotations(blockState));
        poseStack.translate(0.0f, 0.5f, 0.0f);

        poseStack.scale(f, f, f);
        poseStack.scale(-1, -1, 1);

        context.setLightmap(overLight);
        context.setAnimationTicks(animationTicks);
        context.setColorScheme(descriptor.getColorScheme());
        context.setReferenced(SkinItemSource.create(itemStack));

        Rectangle3f rect = context.getBakedRenderBounds();
        apply(entity, rect, animationTicks, poseStack, bufferSource);

        context.draw(poseStack, bufferSource);

        poseStack.popPose();

        if (ModDebugger.hologramProjector) {
            var pos = entity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            ShapeTesselator.stroke(entity.getRenderShape(blockState), UIColor.ORANGE, poseStack, bufferSource);
            poseStack.popPose();
        }
    }

    private void apply(T entity, Rectangle3f rect, float animationTicks, IPoseStack poseStack, IBufferSource bufferSource) {
        var angle = entity.getModelAngle();
        var offset = entity.getModelOffset();
        var rotationOffset = entity.getRotationOffset();
        var rotationSpeed = entity.getRotationSpeed();

        var rotX = angle.getX();
        var speedX = rotationSpeed.getX() / 1000f;
        if (speedX != 0) {
            rotX += ((animationTicks % speedX) / speedX) * 360.0f;
        }

        var rotY = angle.getY();
        var speedY = rotationSpeed.getY() / 1000f;
        if (speedY != 0) {
            rotY += ((animationTicks % speedY) / speedY) * 360.0f;
        }

        var rotZ = angle.getZ();
        var speedZ = rotationSpeed.getZ() / 1000f;
        if (speedZ != 0) {
            rotZ += ((animationTicks % speedZ) / speedZ) * 360.0f;
        }

        var scale = entity.getModelScale();
        poseStack.scale(scale, scale, scale);
        if (entity.isOverrideOrigin()) {
            poseStack.translate(0, -rect.getMaxY(), 0); // to model center
        }
        poseStack.translate(-offset.getX(), -offset.getY(), offset.getZ());

        if (entity.shouldShowRotationPoint()) {
            ShapeTesselator.stroke(-1, -1, -1, 1, 1, 1, UIColor.MAGENTA, poseStack, bufferSource);
        }

        if (ModDebugger.hologramProjector) {
            ShapeTesselator.vector(Vector3f.ZERO, 128, poseStack, bufferSource);
        }

        poseStack.rotate(new OpenQuaternionf(rotX, -rotY, rotZ, true));
        poseStack.translate(rotationOffset.getX(), -rotationOffset.getY(), rotationOffset.getZ());

        if (ModDebugger.hologramProjector) {
            ShapeTesselator.vector(Vector3f.ZERO, 128, poseStack, bufferSource);
        }
    }

    @Override
    public int getViewDistance() {
        return 272;
    }
}
