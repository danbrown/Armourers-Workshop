package moe.plushie.armourers_workshop.core.client.other;

import com.google.common.collect.Iterators;
import moe.plushie.armourers_workshop.api.client.IBufferSource;
import moe.plushie.armourers_workshop.api.core.math.IPoseStack;
import moe.plushie.armourers_workshop.compatibility.api.AbstractItemTransformType;
import moe.plushie.armourers_workshop.compatibility.client.AbstractBufferSource;
import moe.plushie.armourers_workshop.compatibility.client.AbstractPoseStack;
import moe.plushie.armourers_workshop.core.client.animation.AnimationManager;
import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintScheme;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.utils.TickUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class SkinRenderContext implements ConcurrentRenderingContext {

    public static final SkinRenderContext EMPTY = new SkinRenderContext();
    private static final Iterator<SkinRenderContext> POOL = Iterators.cycle(Collections.newList(100, i -> new SkinRenderContext()));

    protected int lightmap = 0xf000f0;
    protected int overlay = OverlayTexture.NO_OVERLAY;
    protected int outlineColor = 0;
    protected float partialTicks = 0;
    protected float animationTicks = 0;

    protected IBufferSource bufferSource;

    protected EntityRenderData renderData;
    protected Function<BakedSkin, ConcurrentBufferBuilder> bufferProvider;

    protected SkinItemSource itemSource;

    protected SkinPaintScheme colorScheme = SkinPaintScheme.EMPTY;
    protected AnimationManager animationManager;
    protected AbstractItemTransformType transformType = AbstractItemTransformType.NONE;

    protected final IPoseStack defaultPoseStack;
    protected IPoseStack poseStack;
    protected IPoseStack modelViewStack;

    public SkinRenderContext() {
        this(new AbstractPoseStack());
    }

    public SkinRenderContext(IPoseStack poseStack) {
        this.defaultPoseStack = poseStack;
        this.poseStack = defaultPoseStack;
    }

    public static SkinRenderContext alloc(EntityRenderData renderData, int light, float partialTick, AbstractItemTransformType transformType) {
        SkinRenderContext context = POOL.next();
        context.setRenderData(renderData);
        context.setLightmap(light);
        context.setPartialTicks(partialTick);
        context.setAnimationTicks(TickUtils.animationTicks());
        context.setTransformType(transformType);
        return context;
    }

    public static SkinRenderContext alloc(EntityRenderData renderData, int light, float partialTick) {
        return alloc(renderData, light, partialTick, AbstractItemTransformType.NONE);
    }

    public void release() {
        this.overlay = OverlayTexture.NO_OVERLAY;
        this.lightmap = 0xf000f0;
        this.outlineColor = 0;
        this.partialTicks = 0;

        this.colorScheme = SkinPaintScheme.EMPTY;
        this.transformType = AbstractItemTransformType.NONE;
        this.itemSource = SkinItemSource.EMPTY;

        this.poseStack = defaultPoseStack;

        this.bufferProvider = null;
        this.renderData = null;
        this.bufferSource = null;
        this.animationManager = null;
    }

    public void pushPose() {
        poseStack.pushPose();
    }

    public void popPose() {
        poseStack.popPose();
    }

    public IPoseStack pose() {
        return poseStack;
    }

    public void setLightmap(int lightmap) {
        this.lightmap = lightmap;
    }

    public int getLightmap() {
        return lightmap;
    }

    public void setOverlay(int overlay) {
        this.overlay = overlay;
    }

    public int getOverlay() {
        return overlay;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setAnimationTicks(float animationTicks) {
        this.animationTicks = animationTicks;
    }

    public float getAnimationTicks() {
        return animationTicks;
    }

    public void setColorScheme(SkinPaintScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public SkinPaintScheme getColorScheme() {
        return colorScheme;
    }

    public void setTransformType(AbstractItemTransformType transformType) {
        this.transformType = transformType;
    }

    public AbstractItemTransformType getTransformType() {
        return transformType;
    }

    public void setRenderData(EntityRenderData renderData) {
        this.renderData = renderData;
    }

    public EntityRenderData getRenderData() {
        return renderData;
    }

    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = animationManager;
    }

    public AnimationManager getAnimationManager() {
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        if (animationManager != null) {
            return animationManager;
        }
        return AnimationManager.NONE;
    }

    @Override
    public ConcurrentBufferBuilder getBuffer(@NotNull BakedSkin skin) {
        if (bufferProvider != null) {
            return bufferProvider.apply(skin);
        }
        var usedBufferSource = bufferSource;
        if (outlineColor != 0) {
            usedBufferSource = AbstractBufferSource.outline();
        }
        var bufferBuilder = SkinVertexBufferBuilder.getBuffer(usedBufferSource);
        return bufferBuilder.getBuffer(skin);
    }

    public void setBufferProvider(Function<BakedSkin, ConcurrentBufferBuilder> bufferProvider) {
        this.bufferProvider = bufferProvider;
    }

    public void setOutlineColor(int outlineColor) {
        this.outlineColor = outlineColor;
    }

    public int getOutlineColor() {
        return outlineColor;
    }

    @Override
    public boolean shouldRenderOutline() {
        return outlineColor != 0;
    }

    @Override
    public float getRenderPriority() {
        if (itemSource != null) {
            return itemSource.getRenderPriority();
        }
        return 0;
    }

    public void setItemSource(SkinItemSource itemSource) {
        this.itemSource = itemSource;
    }

    @Override
    public SkinItemSource getItemSource() {
        if (this.itemSource != null) {
            return this.itemSource;
        }
        return SkinItemSource.EMPTY;
    }

    public void setPoseStack(IPoseStack pose) {
        this.poseStack = pose;
    }

    @Override
    public IPoseStack getPoseStack() {
        return poseStack;
    }

    public void setBufferSource(IBufferSource bufferSource) {
        this.bufferSource = bufferSource;
    }

    @Override
    public IBufferSource getBufferSource() {
        return bufferSource;
    }

    public void setModelViewStack(IPoseStack modelViewStack) {
        this.modelViewStack = modelViewStack;
    }

    @Override
    public IPoseStack getModelViewStack() {
        return modelViewStack;
    }
}
