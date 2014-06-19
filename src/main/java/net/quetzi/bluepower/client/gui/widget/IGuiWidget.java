package net.quetzi.bluepower.client.gui.widget;

import java.awt.Rectangle;
import java.util.List;

public interface IGuiWidget {
    
    public void setListener(IWidgetListener gui);
    
    public int getID();
    
    public void render(int mouseX, int mouseY);
    
    public void onMouseClicked(int mouseX, int mouseY, int button);
    
    public void addTooltip(List<String> curTip);
    
    public Rectangle getBounds();
}
