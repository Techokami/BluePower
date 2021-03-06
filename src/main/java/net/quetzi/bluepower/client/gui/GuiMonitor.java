package net.quetzi.bluepower.client.gui;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.quetzi.bluepower.containers.ContainerMonitor;
import net.quetzi.bluepower.references.Refs;
import net.quetzi.bluepower.tileentities.tier3.TileMonitor;

import org.lwjgl.opengl.GL11;

public class GuiMonitor extends GuiBase {
    
    private static final ResourceLocation resLoc            = new ResourceLocation(Refs.MODID + ":textures/gui/monitorgui.png");
    private static final ResourceLocation chracterSetResLoc = new ResourceLocation(Refs.MODID + ":textures/gui/65el02_chars.png");
    private final TileMonitor             monitor;
    
    public GuiMonitor(InventoryPlayer invPlayer, TileMonitor monitor) {
    
        super(new ContainerMonitor(invPlayer, monitor), resLoc);
        this.monitor = monitor;
        xSize = 350;
        ySize = 230;
        width = 350 / 2;
        //TODO: fix height and width fields as well
    }
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
    
        //super.drawGuiContainerBackgroundLayer(f, i, j);
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(resLoc);
        int k = (width - xSize) / 2;
        int l = (height - ySize) / 2;
        drawTexturedModalRect2(k, l, 0, 0, xSize, ySize);
        
        // screen color
        mc.getTextureManager().bindTexture(chracterSetResLoc);
        GL11.glColor4f(monitor.screenColor[0], monitor.screenColor[1], monitor.screenColor[2], 1.0F);
        
        for (int row = 0; row < 50; row++) {
            for (int col = 0; col < 80; col++) {
                byte character = monitor.screenMemory[row * 80 + col];
                //TODO: overlay cursor character
                if (character != 32) {
                    drawCharacter(row, col, character);
                }
            }
        }
    }
    
    private void drawCharacter(int row, int col, byte character) {
    
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        int tempOffset = 0; //350;
        if (monitor.mode80x40) {
            //Not implemented yet
            drawTexturedModalRect3(x + 15 + col * 4, y + 15 + row * 4, tempOffset + (character & 0xF) * 8, (character >> 4) * 8, 8, 8);
        } else {
            //TODO: fix texture mapping issues
            drawTexturedModalRect3(x + 15 + col * 4, y + 15 + row * 4, tempOffset + (character & 0xF) * 8, (character >> 4) * 8, 8, 8);
        }
    }
    
    /**
        * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
    */
    public void drawTexturedModalRect2(int x, int z, int u, int v, int w, int h) {
    
        float f = 0.00195313F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, z + h, zLevel, (u + 0) * f, (v + h) * f1);
        tessellator.addVertexWithUV(x + w, z + h, zLevel, (u + w) * f, (v + h) * f1);
        tessellator.addVertexWithUV(x + w, z + 0, zLevel, (u + w) * f, (v + 0) * f1);
        tessellator.addVertexWithUV(x + 0, z + 0, zLevel, (u + 0) * f, (v + 0) * f1);
        tessellator.draw();
    }
    
    public void drawTexturedModalRect3(int x, int z, int u, int v, int w, int h) {
    
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, z + h, zLevel, (u + 0) * f, (v + h) * f1);
        tessellator.addVertexWithUV(x + w, z + h, zLevel, (u + w) * f, (v + h) * f1);
        tessellator.addVertexWithUV(x + w, z + 0, zLevel, (u + w) * f, (v + 0) * f1);
        tessellator.addVertexWithUV(x + 0, z + 0, zLevel, (u + 0) * f, (v + 0) * f1);
        tessellator.draw();
    }
}
