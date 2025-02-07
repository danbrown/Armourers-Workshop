package moe.plushie.armourers_workshop.core.client.gui.wardrobe;

import com.apple.library.coregraphics.CGRect;
import com.apple.library.uikit.UICheckBox;
import com.apple.library.uikit.UIControl;
import moe.plushie.armourers_workshop.core.capability.SkinWardrobe;
import moe.plushie.armourers_workshop.core.network.UpdateWardrobePacket;
import moe.plushie.armourers_workshop.core.utils.Objects;
import moe.plushie.armourers_workshop.init.platform.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SkinWardrobeDisplaySetting extends SkinWardrobeBaseSetting {

    private final SkinWardrobe wardrobe;

    public SkinWardrobeDisplaySetting(SkinWardrobe wardrobe) {
        super("wardrobe.display_settings");
        this.wardrobe = wardrobe;
        this.setup();
    }

    private void setup() {
        setupOptionView(83, 27, UpdateWardrobePacket.Field.WARDROBE_ARMOUR_HEAD, "renderHeadArmour");
        setupOptionView(83, 47, UpdateWardrobePacket.Field.WARDROBE_ARMOUR_CHEST, "renderChestArmour");
        setupOptionView(83, 67, UpdateWardrobePacket.Field.WARDROBE_ARMOUR_LEGS, "renderLegArmour");
        setupOptionView(83, 87, UpdateWardrobePacket.Field.WARDROBE_ARMOUR_FEET, "renderFootArmour");
    }

    private void setupOptionView(int x, int y, UpdateWardrobePacket.Field<Boolean> property, String key) {
        var checkBox = new UICheckBox(new CGRect(x, y, 185, 10));
        checkBox.setTitle(getDisplayText(key));
        checkBox.setSelected(property.getOrDefault(wardrobe, true));
        checkBox.addTarget(this, UIControl.Event.VALUE_CHANGED, (self, c) -> {
            UICheckBox checkBox1 = Objects.unsafeCast(c);
            NetworkManager.sendToServer(property.buildPacket(self.wardrobe, checkBox1.isSelected()));
        });
        addSubview(checkBox);
    }
}
