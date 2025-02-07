package moe.plushie.armourers_workshop.compatibility.forge;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.common.IMenuProvider;
import moe.plushie.armourers_workshop.api.common.IMenuSerializer;
import moe.plushie.armourers_workshop.compatibility.core.AbstractMenuType;
import moe.plushie.armourers_workshop.compatibility.core.data.AbstractFriendlyByteBuf;
import moe.plushie.armourers_workshop.core.utils.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import org.jetbrains.annotations.Nullable;

@Available("[1.21, )")
public class AbstractForgeMenuType<C extends AbstractContainerMenu> extends AbstractMenuType<C> {

    private final MenuType<C> type;
    private final IMenuProvider<C, Object> factory;
    private final IMenuSerializer<Object> serializer;

    public <T> AbstractForgeMenuType(IMenuProvider<C, T> factory, IMenuSerializer<T> serializer) {
        this.type = IMenuTypeExtension.create(this::createMenu);
        this.factory = Objects.unsafeCast(factory);
        this.serializer = Objects.unsafeCast(serializer);
    }

    public static <C extends AbstractContainerMenu, T> AbstractForgeMenuType<C> create(IMenuProvider<C, T> factory, IMenuSerializer<T> serializer) {
        // .
        return new AbstractForgeMenuType<>(factory, serializer);
    }

    protected C createMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        var value = serializer.read(AbstractFriendlyByteBuf.wrap(buf), inventory.player);
        return factory.createMenu(type, containerId, inventory, value);
    }

    @Override
    protected <T> InteractionResult openMenu(ServerPlayer player, Component title, T value) {
        player.openMenu(new MenuProvider() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
                return factory.createMenu(type, containerId, inventory, value);
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

        }, buf -> {
            // object to buffer.
            serializer.write(AbstractFriendlyByteBuf.wrap(buf), player, value);
        });
        return InteractionResult.SUCCESS;
    }

    public MenuType<C> getType() {
        return type;
    }
}
