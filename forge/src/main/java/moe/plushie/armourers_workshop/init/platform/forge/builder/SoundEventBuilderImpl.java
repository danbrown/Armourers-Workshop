package moe.plushie.armourers_workshop.init.platform.forge.builder;

import moe.plushie.armourers_workshop.api.core.IRegistryHolder;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.registry.ISoundEventBuilder;
import moe.plushie.armourers_workshop.compatibility.forge.AbstractForgeRegistries;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.utils.TypedRegistry;
import net.minecraft.sounds.SoundEvent;

public class SoundEventBuilderImpl<T extends SoundEvent> implements ISoundEventBuilder<T> {

    @Override
    public IRegistryHolder<T> build(String name) {
        IResourceLocation registryName = ModConstants.key(name);
        SoundEvent event = SoundEvent.createVariableRangeEvent(registryName.toLocation());
        AbstractForgeRegistries.SOUND_EVENTS.register(name, () -> event);
        return TypedRegistry.Entry.cast(registryName, () -> event);
    }
}
