package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import java.awt.*;

public class TriggerComponents {
    
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    public ButtonGroup triggerGroup;
    public JRadioButton actionButton, playerTouch, eventTouch, autorun, parallelProcess;
    
    public TriggerComponents() {
        initializeTriggerComponents();
    }
    
    private void initializeTriggerComponents() {
        triggerGroup = new ButtonGroup();
        
        actionButton = createRadioButton("Action Button", true);
        playerTouch = createRadioButton("Player Touch", false);
        eventTouch = createRadioButton("Event Touch", false);
        autorun = createRadioButton("Autorun", false);
        parallelProcess = createRadioButton("Parallel Process", false);
        
        triggerGroup.add(actionButton);
        triggerGroup.add(playerTouch);
        triggerGroup.add(eventTouch);
        triggerGroup.add(autorun);
        triggerGroup.add(parallelProcess);
    }
    
    private JRadioButton createRadioButton(String text, boolean selected) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setBackground(BACKGROUND_COLOR);
        radioButton.setSelected(selected);
        return radioButton;
    }
}