package net.quetzi.bluepower.part.tube;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.common.util.ForgeDirection;
import net.quetzi.bluepower.api.part.BPPart;
import net.quetzi.bluepower.api.tube.IPneumaticTube.TubeColor;
import net.quetzi.bluepower.api.vec.Vector3;
import net.quetzi.bluepower.api.vec.Vector3Cube;
import net.quetzi.bluepower.client.renderers.IconSupplier;
import net.quetzi.bluepower.helper.IOHelper;
import net.quetzi.bluepower.helper.TileEntityCache;
import net.quetzi.bluepower.init.CustomTabs;

/**
 * 
 * @author MineMaarten
 */

public class PneumaticTube extends BPPart {
    
    public final boolean[]    connections = new boolean[6];
    /**
     * true when != 2 connections, when this is true the logic doesn't have to 'think' which way an item should go.
     */
    public boolean            isCrossOver;
    private final Vector3Cube sideBB      = new Vector3Cube(AxisAlignedBB.getBoundingBox(0.25, 0, 0.25, 0.75, 0.25, 0.75));
    private TileEntityCache[] tileCache;
    private TubeColor         color       = TubeColor.NONE;
    private final TubeLogic   logic       = new TubeLogic(this);
    
    // private final ResourceLocation tubeSideTexture = new ResourceLocation(Refs.MODID + ":textures/blocks/Tubes/pneumatic_tube_side.png");
    // private final ResourceLocation tubeNodeTexture = new ResourceLocation(Refs.MODID + ":textures/blocks/Tubes/tube_end.png");
    
    @Override
    public String getType() {
    
        return "pneumaticTube";
    }
    
    @Override
    public String getUnlocalizedName() {
    
        return "pneumaticTube";
    }
    
    /**
     * Gets all the collision boxes for this block
     * 
     * @return A list with the collision boxes
     */
    @Override
    public List<AxisAlignedBB> getCollisionBoxes() {
    
        return getSelectionBoxes();
    }
    
    /**
     * Gets all the selection boxes for this block
     * 
     * @return A list with the selection boxes
     */
    @Override
    public List<AxisAlignedBB> getSelectionBoxes() {
    
        List<AxisAlignedBB> aabbs = getOcclusionBoxes();
        for (int i = 0; i < 6; i++) {
            if (connections[i]) {
                ForgeDirection d = ForgeDirection.getOrientation(i);
                if (d == ForgeDirection.UP || d == ForgeDirection.DOWN) d = d.getOpposite();
                Vector3Cube c = sideBB.clone().rotate90Degrees(d);
                aabbs.add(c.toAABB());
            }
        }
        return aabbs;
    }
    
    /**
     * Gets all the occlusion boxes for this block
     * 
     * @return A list with the occlusion boxes
     */
    @Override
    public List<AxisAlignedBB> getOcclusionBoxes() {
    
        List<AxisAlignedBB> aabbs = new ArrayList<AxisAlignedBB>();
        aabbs.add(AxisAlignedBB.getBoundingBox(0.25, 0.25, 0.25, 0.75, 0.75, 0.75));
        return aabbs;
    }
    
    @Override
    public void update() {
    
        if (tick == 0) {
            logic.world = world;
            logic.x = x;
            logic.y = y;
            logic.z = z;
        }
        logic.update();
        super.update();
        if (tick == 3) updateConnections();
    }
    
    /**
     * Event called whenever a nearby block updates
     */
    @Override
    public void onNeighborUpdate() {
    
        updateConnections();
    }
    
    @Override
    public void onPartChanged() {
    
        updateConnections();
    }
    
    public TileEntityCache[] getTileCache() {
    
        if (tileCache == null) {
            tileCache = TileEntityCache.getDefaultCache(world, x, y, z);
        }
        return tileCache;
    }
    
    public TubeLogic getLogic() {
    
        return logic;
    }
    
    private void updateConnections() {
    
        if (world != null && !world.isRemote) {
            int connectionCount = 0;
            boolean clearedCache = false;
            for (int i = 0; i < 6; i++) {
                boolean oldState = connections[i];
                getTileCache()[i].update();
                ForgeDirection d = ForgeDirection.getOrientation(i);
                TileEntity neighbor = getTileCache()[i].getTileEntity();
                connections[i] = IOHelper.canInterfaceWith(neighbor, d.getOpposite(), this);
                
                if (!connections[i]) connections[i] = neighbor instanceof ITubeConnection && ((ITubeConnection) neighbor).isConnectedTo(d.getOpposite());
                if (connections[i]) {
                    connections[i] = isConnected(d, null);
                }
                if (connections[i]) connectionCount++;
                if (!clearedCache && oldState != connections[i]) {
                    // getLogic().clearNodeCaches();
                    clearedCache = true;
                }
            }
            isCrossOver = connectionCount != 2;
            sendUpdatePacket();
        }
    }
    
    public boolean isConnected(ForgeDirection dir, PneumaticTube otherTube) {
    
        if (otherTube != null && otherTube.color != TubeColor.NONE && color != TubeColor.NONE && color != otherTube.color) return false;
        if (dir == ForgeDirection.UP || dir == ForgeDirection.DOWN) dir = dir.getOpposite();
        return world == null || !checkOcclusion(sideBB.clone().rotate90Degrees(dir).toAABB());
    }
    
    @Override
    public void save(NBTTagCompound tag) {
    
        super.save(tag);
        for (int i = 0; i < 6; i++) {
            tag.setBoolean("connections" + i, connections[i]);
        }
        tag.setByte("tubeColor", (byte) color.ordinal());
        
        NBTTagCompound logicTag = new NBTTagCompound();
        logic.writeToNBT(logicTag);
        tag.setTag("logic", logicTag);
    }
    
    @Override
    public void load(NBTTagCompound tag) {
    
        super.load(tag);
        int connectionCount = 0;
        for (int i = 0; i < 6; i++) {
            connections[i] = tag.getBoolean("connections" + i);
            if (connections[i]) connectionCount++;
        }
        isCrossOver = connectionCount != 2;
        color = TubeColor.values()[tag.getByte("tubeColor")];
        
        NBTTagCompound logicTag = tag.getCompoundTag("logic");
        logic.readFromNBT(logicTag);
    }
    
    /**
     * Event called when the part is activated (right clicked)
     * 
     * @param player
     *            Player that right clicked the part
     * @param item
     *            Item that was used to click it
     * @return Whether or not an action occurred
     */
    @Override
    public boolean onActivated(EntityPlayer player, ItemStack item) {
    
        if (world == null) return false;
        
        if (!world.isRemote) {
            
            if (item != null && item.getItem() == Items.dye) {
                
                if (item.getItemDamage() < 16) {
                    color = TubeColor.values()[item.getItemDamage()];
                    updateConnections();
                    notifyUpdate();
                    return true;
                }
            } else if (item != null) {
                
                logic.injectStack(item, ForgeDirection.DOWN, TubeColor.values()[new Random().nextInt(16)], false);
            }
        }
        return false;
    }
    
    @Override
    public List<ItemStack> getDrops() {
    
        List<ItemStack> drops = super.getDrops();
        for (TubeStack stack : logic.tubeStacks) {
            drops.add(stack.stack);
        }
        return drops;
    }
    
    /**
     * How 'dense' the tube is to the pathfinding algorithm. Is altered in the RestrictionTube
     * @return
     */
    public int getWeigth() {
    
        return 1;
    }
    
    public TubeColor getColor() {
    
        return color;
    }
    
    /**
     * This render method gets called every tick. You should use this if you're doing animations
     * 
     * @param loc
     *            Distance from the player's position
     * @param pass
     *            Render pass (0 or 1)
     * @param frame
     *            Partial tick for smoother animations
     */
    @Override
    public void renderDynamic(Vector3 loc, int pass, float frame) {
    
        logic.renderDynamic(loc, frame);
    }
    
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
    
        Tessellator t = Tessellator.instance;
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        t.startDrawingQuads();
        
        connections[0] = true;
        connections[1] = true;
        
        renderStatic(new Vector3(0, 0, 0), 0);
        
        t.draw();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationItemsTexture);
    }
    
    private void renderMiddle(AxisAlignedBB aabb, IIcon icon) {
    
        Tessellator t = Tessellator.instance;
        
        if (!connections[2]) {
            double minX = icon.getInterpolatedU(aabb.minX * 16);
            double maxX = icon.getInterpolatedU(aabb.maxX * 16);
            double minY = icon.getInterpolatedV(aabb.minY * 16);
            double maxY = icon.getInterpolatedV(aabb.maxY * 16);
            
            t.setNormal(0, 0, -1);
            if (connections[4]) {// or 5
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxY);// minZ
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxX, maxY);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxY);// minZ
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minX, minY);
            } else {
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxY);// minZ
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minY);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxY);// minZ
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxY);
            }
        }
        
        if (!connections[3]) {
            double minX = icon.getInterpolatedU(aabb.minX * 16);
            double maxX = icon.getInterpolatedU(aabb.maxX * 16);
            double minY = icon.getInterpolatedV(aabb.minY * 16);
            double maxY = icon.getInterpolatedV(aabb.maxY * 16);
            t.setNormal(0, 0, 1);
            if (connections[4]) {// or 5
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxY);// maxZ
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minY);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxY);// maxZ
            } else {
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxY);// maxZ
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minY);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxY);// maxZ
            }
        }
        
        if (!connections[0]) {
            double minX = icon.getInterpolatedU(aabb.minX * 16);
            double maxX = icon.getInterpolatedU(aabb.maxX * 16);
            double minZ = icon.getInterpolatedV(aabb.minZ * 16);
            double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
            t.setNormal(0, -1, 0);
            if (connections[4]) {// or 5
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxZ);// bottom
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, maxX, minZ);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxZ);// bottom
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, maxX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxZ);
            } else {
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxZ);// bottom
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minZ);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxZ);// bottom
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxZ);
            }
        }
        
        if (!connections[1]) {
            double minX = icon.getInterpolatedU(aabb.minX * 16);
            double maxX = icon.getInterpolatedU(aabb.maxX * 16);
            double minZ = icon.getInterpolatedV(aabb.minZ * 16);
            double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
            t.setNormal(0, 1, 0);
            if (connections[4]) {// or 5
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, maxZ);// top
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, maxZ);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, maxZ);// top
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxX, minZ);
            } else {
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minZ);// top
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxZ);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minZ);// top
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minZ);
            }
        }
        
        if (!connections[4]) {
            double minY = icon.getInterpolatedU(aabb.minY * 16);
            double maxY = icon.getInterpolatedU(aabb.maxY * 16);
            double minZ = icon.getInterpolatedV(aabb.minZ * 16);
            double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
            t.setNormal(-1, 0, 0);
            if (connections[0]) {
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxY, maxZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxX, minY, minZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxY, minZ);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxY, maxZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxY, minZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxX, minY, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minY, maxZ);
            } else {
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minY, minZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxY, minZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxX, maxY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minY, maxZ);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minY, minZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxX, maxY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxY, minZ);
            }
        }
        
        if (!connections[5]) {
            double minY = icon.getInterpolatedU(aabb.minY * 16);
            double maxY = icon.getInterpolatedU(aabb.maxY * 16);
            double minZ = icon.getInterpolatedV(aabb.minZ * 16);
            double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
            t.setNormal(1, 0, 0);
            if (connections[0]) {
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxY, maxZ);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minY, minZ);
            } else {
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxY, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minY, minZ);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxY, maxZ);
            }
        }
    }
    
    public void renderTexturedCuboid(AxisAlignedBB aabb, IIcon icon) {
    
        Tessellator t = Tessellator.instance;
        
        if (aabb.minZ != 0 && (!connections[3] || aabb.minZ != 0.75)) {
            if (aabb.maxY == 1 || aabb.minY == 0) {
                double minX = icon.getInterpolatedU(aabb.minX * 16);
                double maxX = icon.getInterpolatedU(aabb.maxX * 16);
                double minY = icon.getInterpolatedV(aabb.minY * 16);
                double maxY = icon.getInterpolatedV(aabb.maxY * 16);
                t.setNormal(0, 0, -1);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxY);// minZ
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minY);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, maxY);// minZ
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, maxX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxY);
                
            } else {
                double minX = icon.getInterpolatedU(aabb.minY * 16);
                double maxX = icon.getInterpolatedU(aabb.maxY * 16);
                double minY = icon.getInterpolatedV(aabb.minX * 16);
                double maxY = icon.getInterpolatedV(aabb.maxX * 16);
                t.setNormal(0, 0, -1);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, maxX, maxY);// minZ
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, minY);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, maxX, maxY);// minZ
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxX, minY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, maxY);
            }
        }
        
        if (aabb.maxZ != 1 && (!connections[2] || aabb.maxZ != 0.25)) {
            if (aabb.minY == 0 || aabb.maxY == 1) {
                double minX = icon.getInterpolatedU(aabb.minX * 16);
                double maxX = icon.getInterpolatedU(aabb.maxX * 16);
                double minY = icon.getInterpolatedV(aabb.minY * 16);
                double maxY = icon.getInterpolatedV(aabb.maxY * 16);
                t.setNormal(0, 0, 1);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, maxY);// maxZ
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minY);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minY);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, maxY);// maxZ
                
            } else {
                double minX = icon.getInterpolatedU(aabb.minY * 16);
                double maxX = icon.getInterpolatedU(aabb.maxY * 16);
                double minY = icon.getInterpolatedV(aabb.minX * 16);
                double maxY = icon.getInterpolatedV(aabb.maxX * 16);
                t.setNormal(0, 0, 1);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, maxY);// maxZ
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minY);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, minY);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minY);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxY);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, minX, maxY);// maxZ
            }
        }
        
        if (aabb.minY != 0 && (!connections[1] || aabb.minY != 0.75)) {
            if (aabb.minX == 0 || aabb.maxX == 1) {
                double minX = icon.getInterpolatedU(aabb.minZ * 16);
                double maxX = icon.getInterpolatedU(aabb.maxZ * 16);
                double minZ = icon.getInterpolatedV(aabb.minX * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxX * 16);
                t.setNormal(0, -1, 0);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxZ);// bottom
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minX, minZ);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minX, maxZ);// bottom
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxZ);
            } else {
                double minX = icon.getInterpolatedU(aabb.minX * 16);
                double maxX = icon.getInterpolatedU(aabb.maxX * 16);
                double minZ = icon.getInterpolatedV(aabb.minZ * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
                t.setNormal(0, -1, 0);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxZ);// bottom
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, maxX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minX, minZ);
                
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, minX, maxZ);// bottom
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, maxX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxX, maxZ);
                
            }
        }
        
        if (aabb.maxY != 1 && (!connections[0] || aabb.maxY != 0.25)) {
            if (aabb.minX == 0 || aabb.maxX == 1) {
                double minX = icon.getInterpolatedU(aabb.minZ * 16);
                double maxX = icon.getInterpolatedU(aabb.maxZ * 16);
                double minZ = icon.getInterpolatedV(aabb.minX * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxX * 16);
                t.setNormal(0, 1, 0);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minZ);// top
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minX, maxZ);
                
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minX, minZ);// top
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minX, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minZ);
            } else {
                double minX = icon.getInterpolatedU(aabb.minX * 16);
                double maxX = icon.getInterpolatedU(aabb.maxX * 16);
                double minZ = icon.getInterpolatedV(aabb.minZ * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
                t.setNormal(0, 1, 0);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, minZ);// top
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxX, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minX, maxZ);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minX, minZ);// top
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxX, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxX, minZ);
                
            }
            
        }
        
        if (aabb.minX != 0 && (!connections[5] || aabb.minX != 0.75)) {
            if (aabb.minY == 0 || aabb.maxY == 1) {
                double minY = icon.getInterpolatedU(aabb.minZ * 16);
                double maxY = icon.getInterpolatedU(aabb.maxZ * 16);
                double minZ = icon.getInterpolatedV(aabb.minY * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxY * 16);
                
                t.setNormal(-1, 0, 0);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minY, minZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxY, minZ);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minY, minZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, minY, maxZ);
            } else {
                double minY = icon.getInterpolatedU(aabb.minY * 16);
                double maxY = icon.getInterpolatedU(aabb.maxY * 16);
                double minZ = icon.getInterpolatedV(aabb.minZ * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
                
                t.setNormal(-1, 0, 0);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minY, minZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxY, minZ);
                
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.minZ, minY, minZ);// minX
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.minZ, maxY, minZ);
                t.addVertexWithUV(aabb.minX, aabb.minY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.minX, aabb.maxY, aabb.maxZ, minY, maxZ);
            }
        }
        
        if (aabb.maxX != 1 && (!connections[4] || aabb.maxX != 0.25)) {
            if (aabb.minY == 0 || aabb.maxY == 1) {
                
                double minY = icon.getInterpolatedU(aabb.minZ * 16);
                double maxY = icon.getInterpolatedU(aabb.maxZ * 16);
                double minZ = icon.getInterpolatedV(aabb.minY * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxY * 16);
                t.setNormal(1, 0, 0);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, minZ);
                
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, maxZ);
            } else {
                double minY = icon.getInterpolatedU(aabb.minY * 16);
                double maxY = icon.getInterpolatedU(aabb.maxY * 16);
                double minZ = icon.getInterpolatedV(aabb.minZ * 16);
                double maxZ = icon.getInterpolatedV(aabb.maxZ * 16);
                t.setNormal(1, 0, 0);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, maxZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, minZ);
                
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.maxZ, minY, maxZ);// maxX
                t.addVertexWithUV(aabb.maxX, aabb.minY, aabb.minZ, minY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.minZ, maxY, minZ);
                t.addVertexWithUV(aabb.maxX, aabb.maxY, aabb.maxZ, maxY, maxZ);
                
            }
        }
    }
    
    /**
     * This render method gets called whenever there's a block update in the chunk. You should use this to remove load from the renderer if a part of
     * the rendering code doesn't need to get called too often or just doesn't change at all. To call a render update to re-render this just call
     * {@link BPPart#markPartForRenderUpdate()}
     * 
     * @param loc
     *            Distance from the player's position
     * @param pass
     *            Render pass (0 or 1)
     * @return Whether or not it rendered something
     */
    @Override
    public boolean renderStatic(Vector3 loc, int pass) {
    
        Tessellator t = Tessellator.instance;
        t.setColorOpaque_F(1, 1, 1);
        t.addTranslation((float) loc.getX(), (float) loc.getY(), (float) loc.getZ());
        
        List<AxisAlignedBB> aabbs = getSelectionBoxes();
        
        boolean shouldRenderNode = false;
        int connectionCount = 0;
        for (int i = 0; i < 6; i += 2) {
            if (connections[i] != connections[i + 1]) {
                shouldRenderNode = true;
                break;
            }
            if (connections[i]) connectionCount++;
            if (connections[i + 1]) connectionCount++;
        }
        if (shouldRenderNode || connectionCount == 0 || connectionCount > 2) {
            renderMiddle(aabbs.get(0), IconSupplier.pneumaticTubeNode);
            if (color != TubeColor.NONE) {
                t.setColorOpaque_I(ItemDye.field_150922_c[color.ordinal()]);
                renderMiddle(aabbs.get(0), IconSupplier.pneumaticTubeColorNode);
                t.setColorOpaque_F(1, 1, 1);
            }
        } else {
            renderMiddle(aabbs.get(0), IconSupplier.pneumaticTubeSide);
            if (color != TubeColor.NONE) {
                t.setColorOpaque_I(ItemDye.field_150922_c[color.ordinal()]);
                renderMiddle(aabbs.get(0), IconSupplier.pneumaticTubeColorSide);
                t.setColorOpaque_F(1, 1, 1);
            }
        }
        for (int i = 1; i < aabbs.size(); i++) {
            renderTexturedCuboid(aabbs.get(i), IconSupplier.pneumaticTubeSide);
            if (color != TubeColor.NONE) {
                t.setColorOpaque_I(ItemDye.field_150922_c[color.ordinal()]);
                renderTexturedCuboid(aabbs.get(i), IconSupplier.pneumaticTubeColorSide);
                t.setColorOpaque_F(1, 1, 1);
            }
        }
        t.addTranslation((float) -loc.getX(), (float) -loc.getY(), (float) -loc.getZ());
        return true;
    }
    
    @Override
    public CreativeTabs getCreativeTab() {
    
        return CustomTabs.tabBluePowerMachines;
    }
}
