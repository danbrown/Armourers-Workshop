package moe.plushie.armourers_workshop.core.skin.document;

import moe.plushie.armourers_workshop.api.skin.part.ISkinPartType;
import moe.plushie.armourers_workshop.core.math.OpenMath;
import moe.plushie.armourers_workshop.core.math.OpenTransform3f;
import moe.plushie.armourers_workshop.core.math.Vector3f;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartTypes;
import moe.plushie.armourers_workshop.core.utils.OpenUUID;
import moe.plushie.armourers_workshop.utils.Constants;
import moe.plushie.armourers_workshop.utils.TranslateUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

import java.util.ArrayList;

public class SkinDocumentNode {

    private Vector3f location = Vector3f.ZERO;
    private Vector3f rotation = Vector3f.ZERO;
    private Vector3f scale = Vector3f.ONE;
    private Vector3f pivot = Vector3f.ZERO;
    private OpenTransform3f transform = null;

    private ISkinPartType type;
    private SkinDescriptor skin = SkinDescriptor.EMPTY;

    private String name;
    private SkinDocumentNode parent;
    private SkinDocumentListener listener;

    private boolean isEnabled = true;
    private boolean isMirror = false;

    private final String id;
    private final ArrayList<SkinDocumentNode> children = new ArrayList<>();

    private String cachedTypeName;

    public SkinDocumentNode(String name) {
        this(OpenUUID.randomUUIDString(), name);
    }

    public SkinDocumentNode(String id, String name) {
        this(id, name, SkinPartTypes.ADVANCED);
    }

    public SkinDocumentNode(String id, String name, ISkinPartType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public SkinDocumentNode(CompoundTag tag) {
        this.id = tag.getString(Keys.UID);
        this.name = tag.getOptionalString(Keys.NAME, null);
        this.type = tag.getOptionalType(Keys.TYPE, SkinPartTypes.ADVANCED, SkinPartTypes::byName);
        this.skin = tag.getOptionalSkinDescriptor(Keys.SKIN);
        this.location = tag.getOptionalVector3f(Keys.LOCATION, Vector3f.ZERO);
        this.rotation = tag.getOptionalVector3f(Keys.ROTATION, Vector3f.ZERO);
        this.scale = tag.getOptionalVector3f(Keys.SCALE, Vector3f.ONE);
        this.pivot = tag.getOptionalVector3f(Keys.PIVOT, Vector3f.ZERO);
        if (tag.contains(Keys.CHILDREN)) {
            var listTag = tag.getList(Keys.CHILDREN, Constants.TagFlags.COMPOUND);
            var count = listTag.size();
            for (var i = 0; i < count; ++i) {
                var node = new SkinDocumentNode(listTag.getCompound(i));
                node.parent = this;
                this.children.add(node);
            }
        }
        this.isEnabled = tag.getOptionalBoolean(Keys.ENABLED, true);
        this.isMirror = tag.getOptionalBoolean(Keys.MIRROR, false);
    }

    public void add(SkinDocumentNode node) {
        if (node.parent != null) {
            node.removeFromParent();
        }
        children.add(node);
        node.parent = this;
        node.setListener(listener);
        if (listener != null) {
            listener.documentDidInsertNode(this, node, -1);
        }
    }

    public void insertAtIndex(SkinDocumentNode node, int index) {
        if (node.parent != null) {
            node.removeFromParent();
        }
        children.add(index, node);
        node.parent = this;
        node.setListener(listener);
        if (listener != null) {
            listener.documentDidInsertNode(this, node, index);
        }
    }

    public void moveTo(SkinDocumentNode node, int toIndex) {
        var index = children.indexOf(node);
        if (index < 0 || index == toIndex) {
            return;
        }
        children.remove(index);
        children.add(OpenMath.clamp(toIndex, 0, children.size()), node);
        if (listener != null) {
            listener.documentDidMoveNode(this, node, toIndex);
        }
    }

    public void removeFromParent() {
        if (parent == null) {
            return;
        }
        if (listener != null) {
            listener.documentDidRemoveNode(this);
        }
        parent.children.remove(this);
        parent = null;
        setListener(null);
    }

    public void setName(String value) {
        name = value;
        cachedTypeName = null;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putOptionalString(Keys.NAME, value, null);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public String getName() {
        if (name != null) {
            return name;
        }
        if (cachedTypeName != null) {
            return cachedTypeName;
        }
        var lhs = TranslateUtils.title("documentType.armourers_workshop.node.root");
        var rhs = TranslateUtils.title("documentType.armourers_workshop.node." + id);
        if (type != SkinPartTypes.ADVANCED) {
            rhs = TranslateUtils.Name.of("documentType.armourers_workshop.node", type);
        }
        cachedTypeName = TranslateUtils.title("documentType.armourers_workshop.node", lhs, rhs).getString();
        return cachedTypeName;
    }

    public void setType(ISkinPartType type) {
        this.type = type;
        this.cachedTypeName = null;
    }

    public ISkinPartType getType() {
        return type;
    }

    public void setSkin(SkinDescriptor value) {
        skin = value;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putOptionalSkinDescriptor(Keys.SKIN, value, null);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public SkinDescriptor getSkin() {
        return skin;
    }

    public void setLocation(Vector3f value) {
        location = value;
        transform = null;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putOptionalVector3f(Keys.LOCATION, value, null);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public Vector3f getLocation() {
        return location;
    }

    public void setRotation(Vector3f value) {
        rotation = value;
        transform = null;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putOptionalVector3f(Keys.ROTATION, value, null);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setScale(float value) {
        scale = new Vector3f(value, value, value);
        transform = null;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putOptionalVector3f(Keys.SCALE, scale, null);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public float getScale() {
        return scale.getX();
    }

    public void setPivot(Vector3f value) {
        pivot = value;
        transform = null;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putOptionalVector3f(Keys.PIVOT, value, null);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public Vector3f getPivot() {
        return pivot;
    }


    public OpenTransform3f getTransform() {
        if (transform != null) {
            return transform;
        }
        var translate = this.location;
        var pivot = this.pivot;
        if (!translate.equals(Vector3f.ZERO)) {
            translate = new Vector3f(-translate.getX(), -translate.getY(), translate.getZ());
        }
        if (!pivot.equals(Vector3f.ZERO)) {
            pivot = new Vector3f(-pivot.getX(), -pivot.getY(), pivot.getZ());
        }
        var scale = this.scale;
        if (isMirror) {
            scale = scale.scaling(-1, 1, 1);
        }
        transform = OpenTransform3f.create(translate, rotation, scale, pivot, Vector3f.ZERO);
        return transform;
    }

    public String getId() {
        return id;
    }

    public void setEnabled(boolean value) {
        isEnabled = value;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putBoolean(Keys.ENABLED, value);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setMirror(boolean value) {
        isMirror = value;
        transform = null;
        if (listener != null) {
            var tag = new CompoundTag();
            tag.putBoolean(Keys.MIRROR, value);
            listener.documentDidUpdateNode(this, tag);
        }
    }

    public boolean isMirror() {
        return isMirror;
    }

    public boolean isLocked() {
        return name == null;
    }

    public boolean isStatic() {
        return type == SkinPartTypes.ADVANCED_STATIC;
    }

    public boolean isFloat() {
        return type == SkinPartTypes.ADVANCED_FLOAT;
    }

    public boolean isLocator() {
        return type == SkinPartTypes.ADVANCED_LOCATOR;
    }


    public boolean isBasic() {
        return type != SkinPartTypes.ADVANCED && type != SkinPartTypes.ADVANCED_STATIC && type != SkinPartTypes.ADVANCED_FLOAT && type != SkinPartTypes.ADVANCED_LOCATOR;
    }

    public SkinDocumentNode parent() {
        return parent;
    }

    public ArrayList<SkinDocumentNode> children() {
        return children;
    }

    protected void setListener(SkinDocumentListener listener) {
        this.listener = listener;
        this.children.forEach(it -> it.setListener(listener));
    }

    protected SkinDocumentListener getListener() {
        return listener;
    }

//    protected boolean equalsStruct(SkinDocumentNode node) {
//        int childSize = children.size();
//        if (!this.id.equals(node.id) || childSize != node.children.size()) {
//            return false;
//        }
//        for (int i = 0; i < childSize; ++i) {
//            SkinDocumentNode leftChild = children.get(i);
//            SkinDocumentNode rightChild = node.children.get(i);
//            if (!leftChild.equalsStruct(rightChild)) {
//                return false;
//            }
//        }
//        return true;
//    }

    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putString(Keys.UID, id);
        tag.putOptionalString(Keys.NAME, name, null);
        tag.putOptionalType(Keys.TYPE, type, SkinPartTypes.ADVANCED);
        tag.putOptionalSkinDescriptor(Keys.SKIN, skin);
        tag.putOptionalVector3f(Keys.LOCATION, location, Vector3f.ZERO);
        tag.putOptionalVector3f(Keys.ROTATION, rotation, Vector3f.ZERO);
        tag.putOptionalVector3f(Keys.SCALE, scale, Vector3f.ONE);
        tag.putOptionalVector3f(Keys.PIVOT, pivot, Vector3f.ZERO);
        if (!children.isEmpty()) {
            var listTag = new ListTag();
            children.forEach(it -> listTag.add(it.serializeNBT()));
            tag.put(Keys.CHILDREN, listTag);
        }
        tag.putOptionalBoolean(Keys.ENABLED, isEnabled, true);
        tag.putOptionalBoolean(Keys.MIRROR, isMirror, false);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        var newSkin = tag.getOptionalSkinDescriptor(Keys.SKIN, null);
        if (newSkin != null) {
            skin = newSkin;
        }
        var newLocation = tag.getOptionalVector3f(Keys.LOCATION, null);
        if (newLocation != null) {
            location = newLocation;
            transform = null;
        }
        var newRotation = tag.getOptionalVector3f(Keys.ROTATION, null);
        if (newRotation != null) {
            rotation = newRotation;
            transform = null;
        }
        var newScale = tag.getOptionalVector3f(Keys.SCALE, null);
        if (newScale != null) {
            scale = newScale;
            transform = null;
        }
        var newPivot = tag.getOptionalVector3f(Keys.PIVOT, null);
        if (newPivot != null) {
            pivot = newPivot;
            transform = null;
        }
        var newName = tag.getOptionalString(Keys.NAME, null);
        if (newName != null) {
            name = newName;
        }
        if (tag.contains(Keys.ENABLED)) {
            isEnabled = tag.getBoolean(Keys.ENABLED);
        }
        if (tag.contains(Keys.MIRROR)) {
            isMirror = tag.getBoolean(Keys.MIRROR);
        }
        listener.documentDidUpdateNode(this, tag);
    }

    public static class Keys {
        public static final String UID = "UID";
        public static final String NAME = "Name";
        public static final String TYPE = "Type";
        public static final String SKIN = "Skin";
        public static final String LOCATION = "Location";
        public static final String ROTATION = "Rotation";
        public static final String SCALE = "Scale";
        public static final String PIVOT = "Pivot";
        public static final String CHILDREN = "Children";
        public static final String ENABLED = "Enabled";
        public static final String MIRROR = "Mirror";
    }
}
