package moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.panel;

import com.apple.library.coregraphics.CGRect;
import com.apple.library.coregraphics.CGSize;
import com.apple.library.uikit.UICheckBox;
import com.apple.library.uikit.UIImage;
import com.apple.library.uikit.UIScrollView;
import com.google.common.collect.ImmutableMap;
import moe.plushie.armourers_workshop.api.skin.ISkinType;
import moe.plushie.armourers_workshop.api.skin.property.ISkinProperty;
import moe.plushie.armourers_workshop.builder.client.gui.advancedbuilder.document.DocumentEditor;
import moe.plushie.armourers_workshop.builder.client.gui.widget.PropertySettingView;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.core.skin.document.SkinDocumentNode;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.core.utils.Collections;
import moe.plushie.armourers_workshop.init.ModTextures;

import java.util.ArrayList;
import java.util.Collection;

public class AdvancedSettingPanel extends AdvancedPanel {

    private static final ImmutableMap<ISkinType, Collection<ISkinProperty<?>>> VALUES = new ImmutableMap.Builder<ISkinType, Collection<ISkinProperty<?>>>()
            .put(SkinTypes.OUTFIT, Collections.newList(
                    SkinProperty.OVERRIDE_MODEL_HEAD,
                    SkinProperty.OVERRIDE_MODEL_CHEST,
                    SkinProperty.OVERRIDE_MODEL_LEFT_ARM,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_ARM,
                    SkinProperty.OVERRIDE_MODEL_LEFT_LEG,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_LEG,
                    SkinProperty.OVERRIDE_OVERLAY_HAT,
                    SkinProperty.OVERRIDE_OVERLAY_CLOAK,
                    SkinProperty.OVERRIDE_OVERLAY_JACKET,
                    SkinProperty.OVERRIDE_OVERLAY_LEFT_SLEEVE,
                    SkinProperty.OVERRIDE_OVERLAY_RIGHT_SLEEVE,
                    SkinProperty.OVERRIDE_OVERLAY_LEFT_PANTS,
                    SkinProperty.OVERRIDE_OVERLAY_RIGHT_PANTS,
                    SkinProperty.OVERRIDE_EQUIPMENT_BOOTS,
                    SkinProperty.OVERRIDE_EQUIPMENT_CHESTPLATE,
                    SkinProperty.OVERRIDE_EQUIPMENT_HELMET,
                    SkinProperty.OVERRIDE_EQUIPMENT_LEGGINGS,
                    SkinProperty.LIMIT_LEGS_LIMBS,
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .put(SkinTypes.ARMOR_HEAD, Collections.newList(
                    SkinProperty.OVERRIDE_MODEL_HEAD,
                    SkinProperty.OVERRIDE_OVERLAY_HAT,
                    SkinProperty.OVERRIDE_EQUIPMENT_HELMET,
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .put(SkinTypes.ARMOR_CHEST, Collections.newList(
                    SkinProperty.OVERRIDE_MODEL_CHEST,
                    SkinProperty.OVERRIDE_MODEL_LEFT_ARM,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_ARM,
                    SkinProperty.OVERRIDE_OVERLAY_CLOAK,
                    SkinProperty.OVERRIDE_OVERLAY_JACKET,
                    SkinProperty.OVERRIDE_OVERLAY_LEFT_SLEEVE,
                    SkinProperty.OVERRIDE_OVERLAY_RIGHT_SLEEVE,
                    SkinProperty.OVERRIDE_EQUIPMENT_CHESTPLATE,
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .put(SkinTypes.ARMOR_FEET, Collections.newList(
                    SkinProperty.OVERRIDE_MODEL_LEFT_LEG,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_LEG,
                    SkinProperty.OVERRIDE_OVERLAY_LEFT_PANTS,
                    SkinProperty.OVERRIDE_OVERLAY_RIGHT_PANTS,
                    SkinProperty.OVERRIDE_EQUIPMENT_LEGGINGS,
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .put(SkinTypes.ARMOR_LEGS, Collections.newList(
                    SkinProperty.OVERRIDE_MODEL_LEFT_LEG,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_LEG,
                    SkinProperty.OVERRIDE_OVERLAY_LEFT_PANTS,
                    SkinProperty.OVERRIDE_OVERLAY_RIGHT_PANTS,
                    SkinProperty.OVERRIDE_EQUIPMENT_BOOTS,
                    SkinProperty.LIMIT_LEGS_LIMBS,
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .put(SkinTypes.ARMOR_WINGS, Collections.newList(
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .put(SkinTypes.ITEM_SWORD, Collections.newList())
            .put(SkinTypes.ITEM_SHIELD, Collections.newList())
            .put(SkinTypes.ITEM_BOW, Collections.newList())
            .put(SkinTypes.ITEM_TRIDENT, Collections.newList())
            .put(SkinTypes.ITEM_PICKAXE, Collections.newList())
            .put(SkinTypes.ITEM_AXE, Collections.newList())
            .put(SkinTypes.ITEM_SHOVEL, Collections.newList())
            .put(SkinTypes.ITEM_HOE, Collections.newList())
            .put(SkinTypes.BLOCK, Collections.newList(
                    SkinProperty.BLOCK_GLOWING,
                    SkinProperty.BLOCK_LADDER,
                    SkinProperty.BLOCK_NO_COLLISION,
                    SkinProperty.BLOCK_SEAT,
                    SkinProperty.BLOCK_MULTIBLOCK,
                    SkinProperty.BLOCK_BED,
                    SkinProperty.BLOCK_ENDER_INVENTORY,
                    SkinProperty.BLOCK_INVENTORY,
                    SkinProperty.BLOCK_INVENTORY_WIDTH,
                    SkinProperty.BLOCK_INVENTORY_HEIGHT
            ))

            .put(SkinTypes.HORSE, Collections.newList(
                    SkinProperty.OVERRIDE_MODEL_HEAD,
                    SkinProperty.OVERRIDE_MODEL_CHEST,
                    SkinProperty.OVERRIDE_MODEL_LEFT_FRONT_LEG,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_FRONT_LEG,
                    SkinProperty.OVERRIDE_MODEL_LEFT_HIND_LEG,
                    SkinProperty.OVERRIDE_MODEL_RIGHT_HIND_LEG,
                    SkinProperty.OVERRIDE_MODEL_TAIL,
                    SkinProperty.OVERRIDE_EQUIPMENT_CHESTPLATE,
                    SkinProperty.KEEP_OVERLAY_COLOR
            ))
            .build();


    private SkinProperties properties;
    private final ArrayList<UICheckBox> boxes = new ArrayList<>();
    private PropertySettingView settingView;
    private final UIScrollView scrollView = new UIScrollView(CGRect.ZERO);

    public AdvancedSettingPanel(DocumentEditor editor) {
        super(editor);
        this.barItem.setImage(UIImage.of(ModTextures.TAB_ICONS).uv(208, 0).fixed(16, 16).build());
        this.setup();
    }

    private void setup() {
        scrollView.setFrame(bounds());
        scrollView.setAutoresizingMask(AutoresizingMask.flexibleWidth | AutoresizingMask.flexibleHeight);
        insertViewAtIndex(scrollView, 0);
        editor.getConnector().addListener(this::update);
    }

    private void update(SkinDocumentNode node) {
        if (properties == document.getProperties()) {
            return;
        }
        properties = document.getProperties();
        addProperties(document.getType().getSkinType());
    }

    private void addProperties(ISkinType skinType) {
        if (settingView != null) {
            settingView.removeFromSuperview();
        }
        CGRect rect = scrollView.frame();
        Collection<ISkinProperty<?>> properties = VALUES.get(skinType);
        if (properties == null || properties.isEmpty()) {
            return;
        }
        settingView = new PropertySettingView(new CGRect(10, 10, rect.width - 20, 0), properties) {
            @Override
            public void beginEditing() {
                editor.beginEditing();
            }

            @Override
            public <T> void putValue(ISkinProperty<T> property, T value) {
                document.put(property, value);
            }

            @Override
            public <T> T getValue(ISkinProperty<T> property) {
                return document.get(property);
            }

            @Override
            public void endEditing() {
                editor.endEditing();
            }

        };
        scrollView.addSubview(settingView);
        scrollView.setContentSize(new CGSize(0, settingView.frame().getMaxY() + 10));
    }
}
