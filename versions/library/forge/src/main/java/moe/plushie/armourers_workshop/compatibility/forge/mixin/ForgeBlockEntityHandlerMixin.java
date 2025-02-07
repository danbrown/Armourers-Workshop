package moe.plushie.armourers_workshop.compatibility.forge.mixin;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.common.IBlockEntityHandler;
import moe.plushie.armourers_workshop.compatibility.core.data.AbstractDataSerializer;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeBlockEntity;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

@Available("[1.21, )")
@Mixin(IBlockEntityHandler.class)
public interface ForgeBlockEntityHandlerMixin extends AbstractForgeBlockEntity {

    @Override
    default AABB getRenderBoundingBox() {
        IBlockEntityHandler handler = Objects.unsafeCast(this);
        BlockEntity blockEntity = Objects.unsafeCast(this);
        AABB result = handler.getRenderBoundingBox(blockEntity.getBlockState());
        if (result != null) {
            return result;
        }
        return AbstractForgeBlockEntity.super.getRenderBoundingBox();
    }

    @Override
    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        BlockEntity blockEntity = Objects.unsafeCast(this);
        IBlockEntityHandler handler = Objects.unsafeCast(this);
        AbstractDataSerializer serializer = AbstractDataSerializer.wrap(pkt.getTag(), provider);
        handler.handleUpdatePacket(blockEntity.getBlockState(), serializer);
    }
}
