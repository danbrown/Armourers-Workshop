package moe.plushie.armourers_workshop.builder.blockentity;

import moe.plushie.armourers_workshop.api.data.IDataSerializer;
import moe.plushie.armourers_workshop.core.blockentity.UpdatableContainerBlockEntity;
import moe.plushie.armourers_workshop.utils.BlockUtils;
import moe.plushie.armourers_workshop.utils.DataSerializerKey;
import moe.plushie.armourers_workshop.utils.DataTypeCodecs;
import moe.plushie.armourers_workshop.utils.NonNullItemList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.util.Strings;

public class OutfitMakerBlockEntity extends UpdatableContainerBlockEntity {

    private static final DataSerializerKey<String> MAKER_NAME_KEY = DataSerializerKey.create("Name", DataTypeCodecs.STRING, "");
    private static final DataSerializerKey<String> MAKER_FLAVOUR_KEY = DataSerializerKey.create("Flavour", DataTypeCodecs.STRING, "");

    private String itemName = "";
    private String itemFlavour = "";

    private final NonNullItemList items = new NonNullItemList(getContainerSize());

    public OutfitMakerBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    public void readAdditionalData(IDataSerializer serializer) {
        items.deserialize(serializer);
        itemName = serializer.read(MAKER_NAME_KEY);
        itemFlavour = serializer.read(MAKER_FLAVOUR_KEY);
    }

    public void writeAdditionalData(IDataSerializer serializer) {
        items.serialize(serializer);
        if (Strings.isNotEmpty(itemName)) {
            serializer.write(MAKER_NAME_KEY, itemName);
        }
        if (Strings.isNotEmpty(itemFlavour)) {
            serializer.write(MAKER_FLAVOUR_KEY, itemFlavour);
        }
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String name) {
        this.itemName = name;
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    public String getItemFlavour() {
        return itemFlavour;
    }

    public void setItemFlavour(String flavour) {
        this.itemFlavour = flavour;
        BlockUtils.combine(this, this::sendBlockUpdates);
    }

    @Override
    protected NonNullItemList getItems() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return 21;
    }
}


