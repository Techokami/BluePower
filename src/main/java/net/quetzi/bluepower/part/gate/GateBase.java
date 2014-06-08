package net.quetzi.bluepower.part.gate;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.quetzi.bluepower.api.part.BPPartFace;
import net.quetzi.bluepower.api.vec.Vector3;
import net.quetzi.bluepower.api.vec.Vector3Cube;
import net.quetzi.bluepower.client.renderers.RenderHelper;
import net.quetzi.bluepower.references.Refs;

import org.lwjgl.opengl.GL11;

public class GateBase extends BPPartFace {
    
    private static Vector3Cube hitbox    = new Vector3Cube(0, 0, 0, 1, 1D / 8D, 1);
    private static Vector3Cube occlusion = new Vector3Cube(1D / 8D, 0, 1D / 8D, 7D / 8D, 1D / 8D, 7D / 8D);
    
    @Override
    public String getType() {
    
        return "gatebase";
    }
    
    @Override
    public String getUnlocalizedName() {
    
        return "gate.gatebase";
    }
    
    @Override
    public List<AxisAlignedBB> getCollisionBoxes() {
    
        return getSelectionBoxes();
    }
    
    @Override
    public List<AxisAlignedBB> getSelectionBoxes() {
    
        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
        
        aabbs.add(hitbox.toAABB());
        
        return aabbs;
    }
    
    @Override
    public List<AxisAlignedBB> getOcclusionBoxes() {
    
        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
        
        aabbs.add(occlusion.toAABB());
        
        return aabbs;
    }
    
    @Override
    public void renderDynamic(Vector3 loc, int pass, float frame) {
    
        GL11.glPushMatrix();
        {
            GL11.glTranslated(loc.getX(), loc.getY(), loc.getZ());
            GL11.glDisable(GL11.GL_CULL_FACE);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Refs.MODID + ":textures/blocks/gates/bottom.png"));
            GL11.glBegin(GL11.GL_QUADS);
            /* Top */
            GL11.glNormal3d(0, 1, 0);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 0, 0, 0);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 0, 1, 0);
            /* Bottom */
            GL11.glNormal3d(0, -1, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 0.001, 1, 1, 1);
            RenderHelper.addVertexWithTexture(1, 0.001, 0, 1, 0);
            GL11.glEnd();
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Refs.MODID + ":textures/blocks/gates/side.png"));
            GL11.glBegin(GL11.GL_QUADS);
            /* East */
            GL11.glNormal3d(1, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 0, 1, 0);
            /* West */
            GL11.glNormal3d(-1, 0, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 0, 1, 0);
            /* North */
            GL11.glNormal3d(0, 0, -1);
            RenderHelper.addVertexWithTexture(0, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 0, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 0, 1, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 0, 1, 0);
            /* South */
            GL11.glNormal3d(0, 0, 1);
            RenderHelper.addVertexWithTexture(0, 0.001, 1, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 1, 1, 0);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_CULL_FACE);
        }
        GL11.glPopMatrix();
    }
    
    @Override
    public boolean renderStatic(Vector3 loc, int pass) {
    
        GL11.glPushMatrix();
        {
            
        }
        GL11.glPopMatrix();
        
        return true;
    }
    
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
    
        GL11.glPushMatrix();
        {
            GL11.glDisable(GL11.GL_CULL_FACE);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Refs.MODID + ":textures/blocks/gates/bottom.png"));
            GL11.glBegin(GL11.GL_QUADS);
            /* Top */
            GL11.glNormal3d(0, 1, 0);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 0, 0, 0);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 0, 1, 0);
            /* Bottom */
            GL11.glNormal3d(0, -1, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 0.001, 1, 1, 1);
            RenderHelper.addVertexWithTexture(1, 0.001, 0, 1, 0);
            GL11.glEnd();
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Refs.MODID + ":textures/blocks/gates/side.png"));
            GL11.glBegin(GL11.GL_QUADS);
            /* East */
            GL11.glNormal3d(1, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 0, 1, 0);
            /* West */
            GL11.glNormal3d(-1, 0, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(0, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 0, 1, 0);
            /* North */
            GL11.glNormal3d(0, 0, -1);
            RenderHelper.addVertexWithTexture(0, 0.001, 0, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 0, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 0, 1, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 0, 1, 0);
            /* South */
            GL11.glNormal3d(0, 0, 1);
            RenderHelper.addVertexWithTexture(0, 0.001, 1, 0, 0);
            RenderHelper.addVertexWithTexture(1, 0.001, 1, 0, 1);
            RenderHelper.addVertexWithTexture(1, 1D/8D, 1, 1, 1);
            RenderHelper.addVertexWithTexture(0, 1D/8D, 1, 1, 0);
            GL11.glEnd();
            GL11.glEnable(GL11.GL_CULL_FACE);
        }
        GL11.glPopMatrix();
    }
    
}