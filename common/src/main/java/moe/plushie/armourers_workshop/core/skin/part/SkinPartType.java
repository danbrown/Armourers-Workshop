package moe.plushie.armourers_workshop.core.skin.part;

import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.skin.part.ISkinPartType;
import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.utils.Objects;


public abstract class SkinPartType implements ISkinPartType {

    protected IResourceLocation registryName;

    protected Rectangle3i bounds;
    protected Rectangle3i buildingSpace;
    protected Rectangle3i guideSpace;
    protected Vector3i offset;

    protected Vector3i renderOffset = Vector3i.ZERO;
    protected float renderPolygonOffset = 0;

    public SkinPartType() {
    }

    @Override
    public String getName() {
        return registryName.toString();
    }

    @Override
    public IResourceLocation getRegistryName() {
        return registryName;
    }

    public SkinPartType setRegistryName(IResourceLocation registryName) {
        this.registryName = registryName;
        return this;
    }

    @Override
    public Rectangle3i getBuildingSpace() {
        return this.buildingSpace;
    }

    @Override
    public Rectangle3i getGuideSpace() {
        return this.guideSpace;
    }

    @Override
    public Vector3i getOffset() {
        return this.offset;
    }

    @Override
    public Rectangle3i getBounds() {
        return bounds;
    }

    @Override
    public int getMinimumMarkersNeeded() {
        return 0;
    }

    @Override
    public int getMaximumMarkersNeeded() {
        return 0;
    }

    @Override
    public boolean isPartRequired() {
        return false;
    }

    @Override
    public Vector3i getRenderOffset() {
        return renderOffset;
    }

    @Override
    public float getRenderPolygonOffset() {
        return renderPolygonOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkinPartType that)) return false;
        return Objects.equals(registryName, that.registryName);
    }

    @Override
    public int hashCode() {
        return registryName.hashCode();
    }

    @Override
    public String toString() {
        return registryName.toString();
    }
}
