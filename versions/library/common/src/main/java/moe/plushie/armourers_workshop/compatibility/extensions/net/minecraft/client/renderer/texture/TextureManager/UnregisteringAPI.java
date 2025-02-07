package moe.plushie.armourers_workshop.compatibility.extensions.net.minecraft.client.renderer.texture.TextureManager;

import moe.plushie.armourers_workshop.api.annotation.Available;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Available("[1.20, )")
@Extension
public class UnregisteringAPI {

    public static void unregister(@This TextureManager textureManager, ResourceLocation resourceLocation) {
        textureManager.release(resourceLocation);
    }
}
