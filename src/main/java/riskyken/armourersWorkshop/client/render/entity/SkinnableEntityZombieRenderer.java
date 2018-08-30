package riskyken.armourersWorkshop.client.render.entity;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderZombie;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import riskyken.armourersWorkshop.api.client.render.entity.ISkinnableEntityRenderer;
import riskyken.armourersWorkshop.api.common.skin.IEntityEquipment;
import riskyken.armourersWorkshop.api.common.skin.data.ISkinPointer;
import riskyken.armourersWorkshop.api.common.skin.type.ISkinType;
import riskyken.armourersWorkshop.client.handler.ModClientFMLEventHandler;
import riskyken.armourersWorkshop.client.model.skin.AbstractModelSkin;
import riskyken.armourersWorkshop.client.render.SkinModelRenderer;
import riskyken.armourersWorkshop.client.skin.cache.ClientSkinCache;
import riskyken.armourersWorkshop.common.skin.data.Skin;
import riskyken.armourersWorkshop.common.skin.type.SkinTypeRegistry;

@SideOnly(Side.CLIENT)
public class SkinnableEntityZombieRenderer implements ISkinnableEntityRenderer<EntityZombie> {
    
    //@Override
    public void render(EntityZombie entity, RenderBiped renderer, double x, double y, double z, IEntityEquipment entityEquipment) {
        GL11.glPushMatrix();
        float scale = 0.0625F;
        
        GL11.glTranslated(x, y, z);
        GL11.glScalef(1, -1, -1);
        
        double rot = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * ModClientFMLEventHandler.renderTickTime;
        GL11.glRotated(rot, 0, 1, 0);
        
        if (entity.deathTime > 0) {
            float angle = ((float)entity.deathTime + ModClientFMLEventHandler.renderTickTime - 1.0F) / 20.0F * 1.6F;
            angle = MathHelper.sqrt(angle);
            if (angle > 1.0F) {
                angle = 1.0F;
            }
            GL11.glRotatef(angle * 90F, 0.0F, 0.0F, 1.0F);
        }
        
        GL11.glTranslated(0, -entity.height + 4.67F * scale, 0);
        
        float headScale = 1.002F;
        GL11.glScalef(headScale, headScale, headScale);
        renderEquipmentType(entity, renderer, SkinTypeRegistry.skinHead, entityEquipment);
        renderEquipmentType(entity, renderer, SkinTypeRegistry.skinChest, entityEquipment);
        renderEquipmentType(entity, renderer, SkinTypeRegistry.skinLegs, entityEquipment);
        renderEquipmentType(entity, renderer, SkinTypeRegistry.skinSkirt, entityEquipment);
        renderEquipmentType(entity, renderer, SkinTypeRegistry.skinFeet, entityEquipment);
        renderEquipmentType(entity, renderer, SkinTypeRegistry.skinWings, entityEquipment);
        
        GL11.glPopMatrix();
    }
    
    private void renderEquipmentType(EntityLivingBase entity, RenderBiped renderer, ISkinType skinType, IEntityEquipment equipmentData) {
        float scale = 0.0625F;
        if (renderer instanceof RenderZombie) {
            RenderZombie rz = (RenderZombie) renderer;
            boolean isZombieVillager = false;
            isZombieVillager = rz.getMainModel() instanceof ModelZombieVillager;
            if (!equipmentData.haveEquipment(skinType, 0)) {
                return;
            }
            ISkinPointer skinPointer = equipmentData.getSkinPointer(skinType, 0);
            Skin skin = ClientSkinCache.INSTANCE.getSkin(skinPointer);
            if (skin == null) {
                return;
            }
            
            AbstractModelSkin model = SkinModelRenderer.INSTANCE.getModelForEquipmentType(skinType);
            
            GL11.glPushMatrix();
            if (isZombieVillager & skinType == SkinTypeRegistry.skinHead) {
                GL11.glTranslated(0, -2.0F * scale, 0);
            }
            if (skinType == SkinTypeRegistry.skinLegs | skinType == SkinTypeRegistry.skinFeet) {
                GL11.glTranslated(0, 0, 0.1F * scale);
            }
            //GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            //GL11.glPolygonOffset(-1F, -1F);
            //model.render(entity, rz.getMainModel(), skin, false, skinPointer.getSkinDye(), null, false, 0, false);
            //GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPopMatrix();
        }
    }
}
