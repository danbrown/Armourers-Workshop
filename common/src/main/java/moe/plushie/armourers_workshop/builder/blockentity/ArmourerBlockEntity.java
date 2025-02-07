package moe.plushie.armourers_workshop.builder.blockentity;

import com.google.common.collect.ImmutableMap;
import moe.plushie.armourers_workshop.api.common.IBlockEntityHandler;
import moe.plushie.armourers_workshop.api.common.IWorldUpdateTask;
import moe.plushie.armourers_workshop.api.data.IDataSerializer;
import moe.plushie.armourers_workshop.api.skin.ISkinToolType;
import moe.plushie.armourers_workshop.api.skin.ISkinType;
import moe.plushie.armourers_workshop.api.skin.paint.ISkinPaintColor;
import moe.plushie.armourers_workshop.api.skin.part.ISkinPartType;
import moe.plushie.armourers_workshop.api.skin.property.ISkinProperty;
import moe.plushie.armourers_workshop.builder.block.ArmourerBlock;
import moe.plushie.armourers_workshop.builder.data.BoundingBox;
import moe.plushie.armourers_workshop.builder.data.PlayerTextureDescriptor;
import moe.plushie.armourers_workshop.builder.item.impl.IPaintToolSelector;
import moe.plushie.armourers_workshop.builder.other.CubeChangesCollector;
import moe.plushie.armourers_workshop.builder.other.CubeReplacingEvent;
import moe.plushie.armourers_workshop.builder.other.CubeSelector;
import moe.plushie.armourers_workshop.builder.other.CubeTransform;
import moe.plushie.armourers_workshop.builder.other.WorldBlockUpdateTask;
import moe.plushie.armourers_workshop.builder.other.WorldUpdater;
import moe.plushie.armourers_workshop.builder.other.WorldUtils;
import moe.plushie.armourers_workshop.core.blockentity.UpdatableBlockEntity;
import moe.plushie.armourers_workshop.core.math.Rectangle3i;
import moe.plushie.armourers_workshop.core.math.Vector2i;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintColor;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintData;
import moe.plushie.armourers_workshop.core.skin.part.SkinPartTypes;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.init.ModBlocks;
import moe.plushie.armourers_workshop.utils.BlockUtils;
import moe.plushie.armourers_workshop.utils.DataSerializerKey;
import moe.plushie.armourers_workshop.utils.DataTypeCodecs;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ArmourerBlockEntity extends UpdatableBlockEntity implements IBlockEntityHandler, IPaintToolSelector.Provider {

    private static final DataSerializerKey<ISkinType> SKIN_TYPE_KEY = DataSerializerKey.create("SkinType", DataTypeCodecs.SKIN_TYPE, SkinTypes.UNKNOWN);
    private static final DataSerializerKey<SkinProperties> SKIN_PROPERTIES_KEY = DataSerializerKey.create("SkinProperties", DataTypeCodecs.SKIN_PROPERTIES, SkinProperties.EMPTY, SkinProperties::new);
    private static final DataSerializerKey<PlayerTextureDescriptor> PLAYER_TEXTURE_KEY = DataSerializerKey.create("Texture", DataTypeCodecs.TEXTURE_DESCRIPTOR, PlayerTextureDescriptor.EMPTY);
    private static final DataSerializerKey<SkinPaintData> PAINT_DATA_KEY = DataSerializerKey.create("PaintData", DataTypeCodecs.SKIN_PAINT_DATA, null);
    private static final DataSerializerKey<Integer> FLAGS_KEY = DataSerializerKey.create("Flags", DataTypeCodecs.INT, 0);
    private static final DataSerializerKey<Integer> VERSION_KEY = DataSerializerKey.create("DataVersion", DataTypeCodecs.INT, 0);

    private static final ImmutableMap<ISkinPartType, ISkinProperty<Boolean>> PART_TO_MODEL = new ImmutableMap.Builder<ISkinPartType, ISkinProperty<Boolean>>()
            .put(SkinPartTypes.BIPPED_HEAD, SkinProperty.OVERRIDE_MODEL_HEAD)
            .put(SkinPartTypes.BIPPED_CHEST, SkinProperty.OVERRIDE_MODEL_CHEST)
            .put(SkinPartTypes.BIPPED_LEFT_ARM, SkinProperty.OVERRIDE_MODEL_LEFT_ARM)
            .put(SkinPartTypes.BIPPED_RIGHT_ARM, SkinProperty.OVERRIDE_MODEL_RIGHT_ARM)
            .put(SkinPartTypes.BIPPED_LEFT_THIGH, SkinProperty.OVERRIDE_MODEL_LEFT_LEG)
            .put(SkinPartTypes.BIPPED_RIGHT_THIGH, SkinProperty.OVERRIDE_MODEL_RIGHT_LEG)
            .put(SkinPartTypes.BIPPED_LEFT_FOOT, SkinProperty.OVERRIDE_MODEL_LEFT_LEG)
            .put(SkinPartTypes.BIPPED_RIGHT_FOOT, SkinProperty.OVERRIDE_MODEL_RIGHT_LEG)
            .build();

    protected int flags = 0;
    protected int version = 0;

    protected ISkinType skinType = SkinTypes.ARMOR_HEAD;
    protected SkinProperties skinProperties = new SkinProperties();
    protected PlayerTextureDescriptor textureDescriptor = PlayerTextureDescriptor.EMPTY;

    protected SkinPaintData paintData;

    protected AABB renderBoundingBox;

    public ArmourerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void readAdditionalData(IDataSerializer serializer) {
        this.skinType = serializer.read(SKIN_TYPE_KEY);
        this.skinProperties = serializer.read(SKIN_PROPERTIES_KEY);
        this.textureDescriptor = serializer.read(PLAYER_TEXTURE_KEY);
        this.flags = serializer.read(FLAGS_KEY);
        this.version = serializer.read(VERSION_KEY);
        this.paintData = serializer.read(PAINT_DATA_KEY);
        // when no skin type is provided, default select head.
        if (this.skinType == SkinTypes.UNKNOWN) {
            this.skinType = SkinTypes.ARMOR_HEAD;
        }
    }

    @Override
    public void writeAdditionalData(IDataSerializer serializer) {
        serializer.write(SKIN_TYPE_KEY, skinType);
        serializer.write(SKIN_PROPERTIES_KEY, skinProperties);
        serializer.write(PLAYER_TEXTURE_KEY, textureDescriptor);
        serializer.write(FLAGS_KEY, flags);
        serializer.write(VERSION_KEY, version);
        serializer.write(PAINT_DATA_KEY, paintData);
    }

    public void onPlace(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity) {
        remakeBoundingBoxes(null, getBoundingBoxes(), true);
        if (entity instanceof Player player) {
            setTextureDescriptor(PlayerTextureDescriptor.fromPlayer(player));
        }
    }

    public void onRemove(Level level, BlockPos pos, BlockState state) {
        // if has been deleted, don't need to remake bounding, because it has been carried out.
        if (!getBlockState().is(ModBlocks.ARMOURER.get())) {
            return;
        }
        remakeBoundingBoxes(getBoundingBoxes(), null, true);
    }

    public ISkinType getSkinType() {
        return skinType;
    }

    public void setSkinType(ISkinType skinType) {
        if (this.skinType == skinType) {
            return;
        }
        var boxes = getBoundingBoxes();
        this.skinType = skinType;
        this.setPaintData(null);
        this.remakeSkinProperties();
        this.remakeBoundingBoxes(boxes, getBoundingBoxes(), true);
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public SkinProperties getSkinProperties() {
        return skinProperties;
    }

    public void setSkinProperties(SkinProperties skinProperties) {
        var boxes = getBoundingBoxes();
        this.skinProperties = skinProperties;
        this.remakeBoundingBoxes(boxes, getBoundingBoxes(), false);
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        var boxes = getBoundingBoxes();
        this.flags = flags;
        this.remakeBoundingBoxes(boxes, getBoundingBoxes(), false);
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public PlayerTextureDescriptor getTextureDescriptor() {
        return textureDescriptor;
    }

    public void setTextureDescriptor(PlayerTextureDescriptor textureDescriptor) {
        this.textureDescriptor = textureDescriptor;
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public SkinPaintData getPaintData() {
        return paintData;
    }

    public void setPaintData(SkinPaintData paintData) {
        if (this.paintData == paintData) {
            return;
        }
        if (paintData != null) {
            this.paintData = SkinPaintData.v2();
            this.paintData.copyFrom(paintData);
        } else {
            this.paintData = null;
        }
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public ISkinPaintColor getPaintColor(Vector2i pos) {
        if (paintData != null) {
            return SkinPaintColor.of(paintData.getColor(pos));
        }
        return null;
    }

    public void setPaintColor(Vector2i pos, ISkinPaintColor paintColor) {
        if (this.paintData == null) {
            this.paintData = SkinPaintData.v2();
        }
        this.paintData.setColor(pos, paintColor.getRawValue());
        this.setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.version += 1;
    }

    public boolean isShowGuides() {
        return (flags & 0x01) == 0;
    }

    public void setShowGuides(boolean value) {
        if (value) {
            this.flags &= ~0x01; // -
        } else {
            this.flags |= 0x01; // +
        }
        this.setChanged();
    }

    public boolean isShowHelper() {
        return (flags & 0x02) == 0;
    }

    public void setShowHelper(boolean value) {
        if (value) {
            this.flags &= ~0x02; // -
        } else {
            this.flags |= 0x02; // +
        }
        this.setChanged();
    }

    public boolean isShowModelGuides() {
        return (flags & 0x04) == 0;
    }

    public void setShowModelGuides(boolean value) {
        if (value) {
            this.flags &= ~0x04; // -
        } else {
            this.flags |= 0x04; // +
        }
        this.setChanged();
    }

    public boolean isUseHelper() {
        if (skinType == SkinTypes.ARMOR_WINGS) {
            return true;
        }
        return skinType instanceof ISkinToolType;
    }

    public IPaintToolSelector createPaintToolSelector(UseOnContext context) {
        var player = context.getPlayer();
        if (player == null || !player.isSecondaryUseActive()) {
            return null;
        }
        var rects = new ArrayList<Rectangle3i>();
        var transform = getTransform();
        for (var partType : getSkinType().getParts()) {
            var box = WorldUtils.getResolvedBuildingSpace(partType);
            var p1 = transform.mul(box.getMinX(), box.getMinY(), box.getMinZ());
            var p2 = transform.mul(box.getMaxX(), box.getMaxY(), box.getMaxZ());
            var minX = Math.min(p1.getX(), p2.getX());
            var minY = Math.min(p1.getY(), p2.getY());
            var minZ = Math.min(p1.getZ(), p2.getZ());
            var maxX = Math.max(p1.getX(), p2.getX());
            var maxY = Math.max(p1.getY(), p2.getY());
            var maxZ = Math.max(p1.getZ(), p2.getZ());
            rects.add(new Rectangle3i(minX, minY, minZ, maxX - minX, maxY - minY, maxZ - minZ));
        }
        return CubeSelector.all(rects);
    }

    public void copyPaintData(CubeChangesCollector collector, ISkinPartType srcPart, ISkinPartType destPart, boolean mirror) {
        if (paintData == null) {
            return;
        }
        var textureModel = BoundingBox.MODEL;
        var srcBox = textureModel.get(srcPart);
        var destBox = textureModel.get(destPart);
        if (srcBox != null && destBox != null) {
            WorldUtils.copyPaintData(paintData, srcBox, destBox, mirror);
            BlockUtils.combine(this, this::sendBlockUpdates);
        }
    }

    public void clearPaintData(CubeChangesCollector collector, ISkinPartType partType) {
        if (paintData == null) {
            return;
        }
        // we think the unknown part type is the signal for the clear all.
        if (partType == SkinPartTypes.UNKNOWN) {
            setPaintData(null);
            return;
        }
        // we just need to clear the paint data for the current part type.
        var textureModel = BoundingBox.MODEL;
        var srcBox = textureModel.get(partType);
        if (srcBox != null) {
            WorldUtils.clearPaintData(paintData, srcBox);
            BlockUtils.combine(this, this::sendBlockUpdates);
        }
    }

    public void clearCubes(CubeChangesCollector collector, ISkinPartType partType) {
        // remove all part
        WorldUtils.clearCubes(collector, getTransform(), getSkinType(), getSkinProperties(), partType);
        // when just clear a part, we don't reset skin properties.
        if (partType != SkinPartTypes.UNKNOWN) {
            return;
        }
        // remake all properties.
        var isMultiBlock = skinProperties.get(SkinProperty.BLOCK_MULTIBLOCK);
        skinProperties = new SkinProperties();
        skinProperties.put(SkinProperty.BLOCK_MULTIBLOCK, isMultiBlock);
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public void replaceCubes(CubeChangesCollector collector, ISkinPartType partType, CubeReplacingEvent event) throws Exception {
        WorldUtils.replaceCubes(collector, getTransform(), getSkinType(), getSkinProperties(), event);
    }

    public void copyCubes(CubeChangesCollector collector, ISkinPartType srcPart, ISkinPartType destPart, boolean mirror) throws Exception {
        WorldUtils.copyCubes(collector, getTransform(), getSkinType(), getSkinProperties(), srcPart, destPart, mirror);
    }

    public void clearMarkers(CubeChangesCollector collector, ISkinPartType partType) {
        WorldUtils.clearMarkers(collector, getTransform(), getSkinType(), getSkinProperties(), partType);
        setChanged();
    }


    public boolean isModelOverridden(ISkinPartType partType) {
        var property = PART_TO_MODEL.get(partType);
        if (property != null) {
            return getSkinProperties().get(property);
        }
        return false;
    }

    public int getVersion() {
        return version;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public AABB getRenderBoundingBox(BlockState blockState) {
        if (renderBoundingBox == null) {
            renderBoundingBox = new AABB(-32, -32, -44, 64, 64, 64);
            renderBoundingBox = renderBoundingBox.move(getBlockPos());
        }
        return renderBoundingBox;
    }

    private void remakeSkinProperties() {
        String name = skinProperties.get(SkinProperty.ALL_CUSTOM_NAME);
        String flavour = skinProperties.get(SkinProperty.ALL_FLAVOUR_TEXT);
        this.skinProperties = new SkinProperties();
        this.skinProperties.put(SkinProperty.ALL_CUSTOM_NAME, name);
        this.skinProperties.put(SkinProperty.ALL_FLAVOUR_TEXT, flavour);
    }

    private boolean shouldAddBoundingBoxes(ISkinPartType partType) {
        if (isUseHelper()) {
            return isShowHelper();
        }
        return !isModelOverridden(partType);
    }

    private void remakeBoundingBoxes(Collection<BoundingBox> oldBoxes, Collection<BoundingBox> newBoxes, boolean forced) {
        // we only remake bounding box on the server side.
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        // we only remake bounding box when data is changed.
        if (!forced && Objects.equals(oldBoxes, newBoxes)) {
            return;
        }
        // we need to remove the old bounding box before add.
        applyBoundingBoxes(oldBoxes, (partType, pos, offset) -> {
            WorldBlockUpdateTask task = new WorldBlockUpdateTask(level, pos, Blocks.AIR.defaultBlockState());
            task.setValidator(state -> state.is(ModBlocks.BOUNDING_BOX.get()));
            return task;
        });
        applyBoundingBoxes(newBoxes, (partType, pos, offset) -> {
            WorldBlockUpdateTask task = new WorldBlockUpdateTask(level, pos, ModBlocks.BOUNDING_BOX.get().defaultBlockState());
            task.setValidator(state -> state.isReplaceable() || state.is(ModBlocks.BOUNDING_BOX.get()));
            task.setModifier(state -> setupBoundingBox(level, pos, offset, partType));
            return task;
        });
    }

    private void setupBoundingBox(Level level, BlockPos pos, Vector3i offset, ISkinPartType partType) {
        if (level.getBlockEntity(pos) instanceof BoundingBoxBlockEntity blockEntity) {
            blockEntity.setPartType(partType);
            blockEntity.setGuide(offset);
            blockEntity.setParent(pos.subtract(getBlockPos()));
            BlockUtils.combine(blockEntity, blockEntity::sendBlockUpdates);
        }
    }

    private void applyBoundingBoxes(@Nullable Collection<BoundingBox> boxes, IUpdateTaskBuilder builder) {
        if (boxes == null || boxes.isEmpty()) {
            return;
        }
        var transform = getTransform();
        boxes.forEach(box -> box.forEach((ix, iy, iz) -> {
            BlockPos target = transform.mul(ix + box.getX(), iy + box.getY(), iz + box.getZ());
            ix = box.getWidth() - ix - 1;
            iy = box.getHeight() - iy - 1;
            ISkinPartType partType = box.getPartType();
            IWorldUpdateTask task = builder.build(partType, target, new Vector3i(ix, iy, iz));
            if (task != null) {
                WorldUpdater.getInstance().submit(task);
            }
        }));
    }

    private Collection<BoundingBox> getBoundingBoxes() {
        var boxes = new ArrayList<BoundingBox>();
        for (var partType : skinType.getParts()) {
            if (shouldAddBoundingBoxes(partType)) {
                var offset = partType.getOffset();
                var bounds = partType.getBuildingSpace();
                var rect = new Rectangle3i(partType.getGuideSpace());
                rect = rect.offset(-offset.getX(), -offset.getY() - bounds.getMinY(), offset.getZ());
                boxes.add(new BoundingBox(partType, rect));
            }
        }
        return boxes;
    }

    private Collection<BoundingBox> getFullBoundingBoxes() {
        var boxes = new ArrayList<BoundingBox>();
        for (var partType : skinType.getParts()) {
            if (shouldAddBoundingBoxes(partType)) {
                var origin = partType.getOffset();
                var buildSpace = partType.getBuildingSpace();
                var dx = -origin.getX() + buildSpace.getX();
                var dy = -origin.getY();
                var dz = origin.getZ() + buildSpace.getZ();
                var rect = new Rectangle3i(dx, dy, dz, buildSpace.getWidth(), buildSpace.getHeight(), buildSpace.getDepth());
                boxes.add(new BoundingBox(partType, rect));
            }
        }
        return boxes;
    }

    public Direction getFacing() {
        return getBlockState().getOptionalValue(ArmourerBlock.FACING).orElse(Direction.NORTH);
    }

    public CubeTransform getTransform() {
        var pos = getBlockPos().offset(0, 1, 0);
        return new CubeTransform(getLevel(), pos, getFacing());
    }

    public interface IUpdateTaskBuilder {
        IWorldUpdateTask build(ISkinPartType partType, BlockPos pos, Vector3i offset);
    }
}
