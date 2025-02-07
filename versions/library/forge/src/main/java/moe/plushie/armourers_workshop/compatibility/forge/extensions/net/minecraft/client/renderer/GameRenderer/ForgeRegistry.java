package moe.plushie.armourers_workshop.compatibility.forge.extensions.net.minecraft.client.renderer.GameRenderer;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.core.IRegistryHolder;
import moe.plushie.armourers_workshop.compatibility.client.AbstractBlockEntityRendererProvider;
import moe.plushie.armourers_workshop.compatibility.client.AbstractEntityRendererProvider;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeClientEventsImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.ThisClass;

@Available("[1.18, )")
@Extension
public class ForgeRegistry {

    public static <T extends Entity> void registerEntityRendererFO(@ThisClass Class<?> clazz, IRegistryHolder<EntityType<T>> entityType, AbstractEntityRendererProvider<T> provider) {
        AbstractForgeClientEventsImpl.ENTITY_RENDERER_REGISTRY.listen(event -> event.registerEntityRenderer(entityType.get(), provider::create));
    }

    public static <T extends BlockEntity> void registerBlockEntityRendererFO(@ThisClass Class<?> clazz, IRegistryHolder<BlockEntityType<T>> entityType, AbstractBlockEntityRendererProvider<T> provider) {
        AbstractForgeClientEventsImpl.ENTITY_RENDERER_REGISTRY.listen(event -> event.registerBlockEntityRenderer(entityType.get(), provider::create));
    }
}
