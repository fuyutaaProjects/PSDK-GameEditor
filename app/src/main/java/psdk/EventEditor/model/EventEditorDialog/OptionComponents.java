package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import java.awt.*;

public class OptionComponents {
    
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    public JCheckBox moveAnimation, stopAnimation, directionFix, through, alwaysOnTop;
    
    public OptionComponents() {
        initializeOptionComponents();
    }
    
    private void initializeOptionComponents() {
        moveAnimation = createCheckBox("Move Animation");
        moveAnimation.setSelected(true);
        
        stopAnimation = createCheckBox("Stop Animation");
        directionFix = createCheckBox("Direction Fix");
        through = createCheckBox("Through");
        alwaysOnTop = createCheckBox("Always on Top");
    }
    
    private JCheckBox createCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBackground(BACKGROUND_COLOR);
        return checkBox;
    }
}