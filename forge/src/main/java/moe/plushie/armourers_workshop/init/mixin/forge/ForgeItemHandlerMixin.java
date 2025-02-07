package moe.plushie.armourers_workshop.init.mixin.forge;

import moe.plushie.armourers_workshop.api.common.IItemHandler;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeItem;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(IItemHandler.class)
public interface ForgeItemHandlerMixin extends AbstractForgeItem {

    @Override
    default boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
        IItemHandler handler = Objects.unsafeCast(this);
        return handler.attackLivingEntity(itemStack, player, entity) != InteractionResult.PASS;
    }

    @Override
    default InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        IItemHandler handler = Objects.unsafeCast(this);
        return handler.useOnFirst(stack, context);
    }
}
