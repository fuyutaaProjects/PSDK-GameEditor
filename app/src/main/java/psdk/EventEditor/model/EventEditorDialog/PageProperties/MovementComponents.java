package psdk.EventEditor.model.EventEditorDialog.PageProperties;

import javax.swing.*;
import java.awt.*;

public class MovementComponents {
    
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    public JComboBox<String> moveType, moveSpeed, moveFreq;
    public JButton moveRouteButton;
    
    public MovementComponents() {
        initializeMovementComponents();
    }
    
    private void initializeMovementComponents() {
        moveType = new JComboBox<>(new String[]{"Fixed", "Random", "Approach", "Custom"});
        
        moveSpeed = new JComboBox<>(new String[]{
            "1: Slowest", "2: Slower", "3: Slow", "4: Normal", "5: Fast", "6: Faster"});
        moveSpeed.setSelectedIndex(2);
        
        moveFreq = new JComboBox<>(new String[]{
            "1: Lowest", "2: Lower", "3: Low", "4: Normal", "5: High"});
        moveFreq.setSelectedIndex(2);
        
        moveRouteButton = createStyledButton("Move Route...", null);
        moveRouteButton.setEnabled(false);
    }
    
    private JButton createStyledButton(String text, Dimension size) {
        JButton button = new JButton(text);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        if (size != null) {
            button.setPreferredSize(size);
        }
        return button;
    }
}