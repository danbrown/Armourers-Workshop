package moe.plushie.armourers_workshop.compatibility.forge.event.client;

import moe.plushie.armourers_workshop.api.annotation.Available;
import moe.plushie.armourers_workshop.api.registry.IEventHandler;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeClientEventsImpl;
import moe.plushie.armourers_workshop.init.platform.event.client.RegisterModelEvent;
import net.minecraft.resources.ResourceLocation;

@Available("[1.21, )")
public class AbstractForgeRegisterModelEvent {

    public static IEventHandler<RegisterModelEvent> registryFactory() {
        return AbstractForgeClientEventsImpl.MODEL_REGISTRY.map(event -> registryName -> event.register(ResourceLocation.create(registryName, "inventory")));
    }
}
