package moe.plushie.armourers_workshop.core.data.transform;

import moe.plushie.armourers_workshop.api.core.math.IPoseStack;
import moe.plushie.armourers_workshop.api.core.math.ITransform;
import moe.plushie.armourers_workshop.api.core.math.ITransform3f;
import moe.plushie.armourers_workshop.api.skin.part.features.ICanRotation;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;

import java.util.ArrayList;
import java.util.List;

public class SkinPartTransform implements ITransform {

    public static final SkinPartTransform IDENTITY = new SkinPartTransform();
    private final ArrayList<ITransform> children = new ArrayList<>();

    public SkinPartTransform() {
    }

    public SkinPartTransform(SkinPart part, ITransform quadsTransform) {
        if (quadsTransform != null) {
            children.add(quadsTransform);
        }
        var wingsTransform = getWingsTransform(part);
        if (wingsTransform != null) {
            children.add(wingsTransform);
        }
        var partTransform = part.getTransform();
        if (partTransform != null) {
            children.add(partTransform);
        }
    }

    private ITransform getWingsTransform(SkinPart part) {
        var partType = part.getType();
        if (!(partType instanceof ICanRotation)) {
            return null;
        }
        var markers = part.getMarkers();
        if (markers == null || markers.isEmpty()) {
            return null;
        }
        return new SkinWingsTransform(partType, part.getProperties(), markers.iterator().next());
    }

    @Override
    public void apply(IPoseStack poseStack) {
        for (var transform : children) {
            transform.apply(poseStack);
        }
    }

    public void addChild(ITransform transform) {
        children.add(transform);
    }

    public void insertChild(ITransform transform, int index) {
        children.add(index, transform);
    }

    public void replaceChild(ITransform oldTransform, ITransform newTransform) {
        int index = children.indexOf(oldTransform);
        if (index != -1) {
            children.set(index, newTransform);
        }
    }

    public void removeChild(ITransform transform) {
        children.remove(transform);
    }

    public List<ITransform> getChildren() {
        return children;
    }

    public boolean isIdentity() {
        for (var transform : children) {
            if (transform instanceof ITransform3f transform1 && transform1.isIdentity()) {
                continue;
            }
            return false;
        }
        return true;
    }
}
