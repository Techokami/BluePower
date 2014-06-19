/*
 * This file is part of Blue Power.
 *
 *     Blue Power is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Blue Power is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Blue Power.  If not, see <http://www.gnu.org/licenses/>
 */
package net.quetzi.bluepower.client.gui.widget;

import java.awt.Rectangle;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

/**
 * @author MineMaarten
 */
public class BaseWidget implements IGuiWidget {
    
    private final int                id;
    public int                       value;            //just a generic value
    protected final int              x, y;
    private final int                width;
    private final int                height;
    private final int                textureU;
    private final int                textureV;
    private final ResourceLocation[] textures;
    protected int                    textureIndex = 0;
    protected IWidgetListener        gui;
    
    public BaseWidget(int id, int x, int y, int width, int height, String... textureLocs) {
    
        this(id, x, y, width, height, 0, 0, textureLocs);
    }
    
    public BaseWidget(int id, int x, int y, int width, int height, int textureU, int textureV, String... textureLocs) {
    
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textureU = textureU;
        this.textureV = textureV;
        textures = new ResourceLocation[textureLocs.length];
        for (int i = 0; i < textures.length; i++) {
            textures[i] = new ResourceLocation(textureLocs[i]);
        }
    }
    
    @Override
    public int getID() {
    
        return id;
    }
    
    @Override
    public void setListener(IWidgetListener gui) {
    
        this.gui = gui;
    }
    
    @Override
    public void render(int mouseX, int mouseY) {
    
        if (textures.length > 0) Minecraft.getMinecraft().getTextureManager().bindTexture(textures[textureIndex]);
        Gui.func_146110_a(x, y, getTextureU(), getTextureV(), width, height, getTextureWidth(), getTextureHeight());
    }
    
    protected int getTextureU() {
    
        return textureU;
    }
    
    protected int getTextureV() {
    
        return textureV;
    }
    
    protected int getTextureWidth() {
    
        return width;
    }
    
    protected int getTextureHeight() {
    
        return height;
    }
    
    @Override
    public void onMouseClicked(int mouseX, int mouseY, int button) {
    
        gui.actionPerformed(this);
    }
    
    @Override
    public Rectangle getBounds() {
    
        return new Rectangle(x, y, width, height);
    }
    
    @Override
    public void addTooltip(List<String> curTip) {
    
    }
    
}
