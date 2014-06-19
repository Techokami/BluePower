package net.quetzi.bluepower.client.renderers;

import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.quetzi.bluepower.references.Refs;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class IconSupplier {
    
    public static IIcon pneumaticTubeSide;
    public static IIcon pneumaticTubeNode;
    public static IIcon pneumaticTubeColorNode;
    public static IIcon pneumaticTubeColorSide;
    public static IIcon cagedLampFootSide;
    public static IIcon cagedLampFootTop;
    public static IIcon cagedLampCageSide;
    public static IIcon cagedLampCageTop;
    public static IIcon cagedLampLampActive;
    public static IIcon cagedLampLampInactive;
    public static IIcon cagedLampLampActiveTop;
    public static IIcon cagedLampLampInactiveTop;
    public static IIcon fixtureFootSide;
    public static IIcon fixtureFootTop;
    public static IIcon fixtureLampSideOn;
    public static IIcon fixtureLampTopOn;
    public static IIcon fixtureLampSideOff;
    public static IIcon fixtureLampTopOff;
    public static IIcon lampOn;
    public static IIcon lampOff;
    
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
    
        if (event.map.getTextureType() == 0) {
            pneumaticTubeSide = event.map.registerIcon(Refs.MODID + ":tubes/pneumatic_tube_side");
            pneumaticTubeNode = event.map.registerIcon(Refs.MODID + ":tubes/tube_end");
            pneumaticTubeColorSide = event.map.registerIcon(Refs.MODID + ":tubes/tube_color_side");
            pneumaticTubeColorNode = event.map.registerIcon(Refs.MODID + ":/tubes/tube_color_end");
            
            cagedLampFootSide = event.map.registerIcon(Refs.MODID + ":lamps/cage_foot_side");
            cagedLampFootTop = event.map.registerIcon(Refs.MODID + ":lamps/cage_foot_top");
            cagedLampCageSide = event.map.registerIcon(Refs.MODID + ":lamps/cage");
            cagedLampCageTop = event.map.registerIcon(Refs.MODID + ":lamps/cage_top");
            
            cagedLampLampActive = event.map.registerIcon(Refs.MODID + ":lamps/cage_lamp_on");
            cagedLampLampInactive = event.map.registerIcon(Refs.MODID + ":lamps/cage_lamp_off");
            
            cagedLampLampActiveTop = event.map.registerIcon(Refs.MODID + ":lamps/cage_lamp_on_top");
            cagedLampLampInactiveTop = event.map.registerIcon(Refs.MODID + ":lamps/cage_lamp_off_top");
            
            fixtureFootSide = event.map.registerIcon(Refs.MODID + ":lamps/fixture_foot_side");
            fixtureFootTop = event.map.registerIcon(Refs.MODID + ":lamps/fixture_foot_top");
            fixtureLampSideOn = event.map.registerIcon(Refs.MODID + ":lamps/fixture_lamp_on");
            fixtureLampTopOn = event.map.registerIcon(Refs.MODID + ":lamps/fixture_lamp_on_top");
            
            fixtureLampSideOff = event.map.registerIcon(Refs.MODID + ":lamps/fixture_lamp_off");
            fixtureLampTopOff = event.map.registerIcon(Refs.MODID + ":lamps/fixture_lamp_off_top");
            
            lampOn = event.map.registerIcon(Refs.MODID + ":lamps/lamp_off");
            lampOff = event.map.registerIcon(Refs.MODID + ":lamps/lamp_on");
        }
    }
}
