package moe.plushie.armourers_workshop.init.platform.forge.proxy;

import moe.plushie.armourers_workshop.api.math.IPoseStack;
import moe.plushie.armourers_workshop.compatibility.AbstractPoseStack;
import moe.plushie.armourers_workshop.core.client.other.SkinRenderData;
import moe.plushie.armourers_workshop.core.client.render.HighlightPlacementRenderer;
import moe.plushie.armourers_workshop.core.client.skinrender.SkinRendererManager;
import moe.plushie.armourers_workshop.init.ModConfig;
import moe.plushie.armourers_workshop.init.ModItems;
import moe.plushie.armourers_workshop.init.client.ClientWardrobeHandler;
import moe.plushie.armourers_workshop.init.environment.EnvironmentExecutor;
import moe.plushie.armourers_workshop.init.environment.EnvironmentType;
import moe.plushie.armourers_workshop.init.platform.forge.ClientNativeManagerImpl;
import moe.plushie.armourers_workshop.init.platform.forge.NotificationCenterImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxyImpl {

    public static void init() {
        EnvironmentExecutor.willInit(EnvironmentType.CLIENT);

        // listen the fml events.
        NotificationCenterImpl.observer(FMLClientSetupEvent.class, event -> EnvironmentExecutor.didInit(EnvironmentType.CLIENT));
        NotificationCenterImpl.observer(FMLLoadCompleteEvent.class, event -> event.enqueueWork(() -> EnvironmentExecutor.didSetup(EnvironmentType.CLIENT)));

        // listen the block highlight events.
        ClientNativeManagerImpl.INSTANCE.willRenderBlockHighlight((traceResult, camera, poseStackIn, buffers) -> {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            // hidden hit box at inside
            // if (event.getTarget().isInside()) {
            //     BlockState state = player.level.getBlockState(event.getTarget().getBlockPos());
            //     if (state.is(ModBlocks.BOUNDING_BOX)) {
            //         event.setCanceled(true);
            //         return;
            //     }
            // }
            IPoseStack poseStack = AbstractPoseStack.wrap(poseStackIn);
            ItemStack itemStack = player.getMainHandItem();
            Item item = itemStack.getItem();
            if (ModConfig.Client.enableEntityPlacementHighlight && item == ModItems.MANNEQUIN.get()) {
                HighlightPlacementRenderer.renderEntity(player, traceResult, camera, poseStack, buffers);
            }
            if (ModConfig.Client.enableBlockPlacementHighlight && item == ModItems.SKIN.get()) {
                HighlightPlacementRenderer.renderBlock(itemStack, player, traceResult, camera, poseStack, buffers);
            }
            if (ModConfig.Client.enablePaintToolPlacementHighlight && item == ModItems.BLENDING_TOOL.get()) {
                HighlightPlacementRenderer.renderPaintTool(itemStack, player, traceResult, camera, poseStack, buffers);
            }
        });

        ClientNativeManagerImpl.INSTANCE.willRenderLivingEntity(((entity, renderer, context) -> {
            SkinRenderData renderData = SkinRenderData.of(entity);
            if (renderData != null) {
                SkinRendererManager.getInstance().willRender(entity, renderer.getModel(), renderer, renderData, context);
            }
        }));
        ClientNativeManagerImpl.INSTANCE.didRenderLivingEntity(((entity, renderer, context) -> {
            SkinRenderData renderData = SkinRenderData.of(entity);
            if (renderData != null) {
                SkinRendererManager.getInstance().didRender(entity, renderer.getModel(), renderer, renderData, context);
            }
        }));

        NotificationCenterImpl.observer(RenderArmEvent.class, event -> {
            if (!ModConfig.enableFirstPersonSkinRenderer()) {
                return;
            }
            int light = event.getPackedLight();
            Player player = Minecraft.getInstance().player;
            IPoseStack poseStack = AbstractPoseStack.wrap(event.getPoseStack());
            MultiBufferSource buffers = event.getMultiBufferSource();
            ItemTransforms.TransformType transformType = ItemTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            if (event.getArm() == HumanoidArm.RIGHT) {
                transformType = ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND;
            }
            ClientWardrobeHandler.onRenderSpecificHand(player, 0, light, 0, transformType, poseStack, buffers, () -> {
                event.setCanceled(true);
            });
        });
    }
}
