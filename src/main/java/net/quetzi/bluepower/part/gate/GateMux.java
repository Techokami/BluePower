package net.quetzi.bluepower.part.gate;

import java.util.List;

import net.minecraft.util.AxisAlignedBB;
import net.quetzi.bluepower.api.part.FaceDirection;
import net.quetzi.bluepower.api.part.RedstoneConnection;
import net.quetzi.bluepower.client.renderers.RenderHelper;
import net.quetzi.bluepower.references.Refs;

public class GateMux extends GateBase {
    
    @Override
    public void initializeConnections(RedstoneConnection front, RedstoneConnection left, RedstoneConnection back, RedstoneConnection right) {
    
        // Init front
        front.enable();
        front.setOutput();
        
        // Init left
        left.enable();
        left.setInput();
        
        // Init back
        back.enable();
        back.setInput();
        
        // Init right
        right.enable();
        right.setInput();
    }
    
    @Override
    public String getGateID() {
    
        return "multiplexer";
    }
    
    @Override
    public void renderTop(RedstoneConnection front, RedstoneConnection left, RedstoneConnection back, RedstoneConnection right, float frame) {
    
        // renderTopTexture(FaceDirection.FRONT, false);
        renderTopTexture(FaceDirection.LEFT, left.getPower() > 0 || back.getPower() == 0);
        renderTopTexture(FaceDirection.RIGHT, right.getPower() > 0 || back.getPower() > 0);
        renderTopTexture(FaceDirection.BACK, back.getPower() > 0);
        RenderHelper.renderRedstoneTorch(0, 1D / 8D, 2 / 16D, 9D / 16D, back.getPower() == 0);
        boolean frontLeft = !(left.getPower() > 0 || back.getPower() == 0);
        boolean frontRight = !(right.getPower() > 0 || back.getPower() > 0);
        RenderHelper.renderRedstoneTorch(4/16D, 1D / 8D, -1 / 16D, 9D / 16D, frontRight);
        RenderHelper.renderRedstoneTorch(-4/16D, 1D / 8D, -1 / 16D, 9D / 16D, frontLeft);
        

        renderTopTexture(Refs.MODID + ":textures/blocks/gates/" + getType() + "/frontleft_" + (frontLeft ? "on" : "off") + ".png");
        renderTopTexture(Refs.MODID + ":textures/blocks/gates/" + getType() + "/frontright_" + (frontRight ? "on" : "off") + ".png");
        RenderHelper.renderRedstoneTorch(0, 1D / 8D, -4 / 16D, 9D / 16D, !frontLeft && !frontRight);
    }
    
    @Override
    public void addOcclusionBoxes(List<AxisAlignedBB> boxes) {
    
        super.addOcclusionBoxes(boxes);
        
        boxes.add(AxisAlignedBB.getBoundingBox(7D / 16D, 2D / 16D, 7D / 16D, 9D / 16D, 8D / 16D, 9D / 16D));
    }
    
    @Override
    public void doLogic(RedstoneConnection front, RedstoneConnection left, RedstoneConnection back, RedstoneConnection right) {
    
        boolean selected = back.getPower() > 0;
        int out = 0;
        
        if (selected) {
            out = left.getPower();
        } else {
            out = right.getPower();
        }
        
        front.setPower(out);
    }
    
}
