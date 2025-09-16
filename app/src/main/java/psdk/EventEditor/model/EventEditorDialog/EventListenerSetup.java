package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;

public class EventListenerSetup {
    
    public static void setupConditionListeners(ConditionComponents components) {
        components.switchCondition.addActionListener(e -> {
            boolean enabled = components.switchCondition.isSelected();
            components.switchField.setEnabled(enabled);
            if (!enabled) components.switchField.setText("");
        });
        
        components.variableCondition.addActionListener(e -> {
            boolean enabled = components.variableCondition.isSelected();
            components.variableField.setEnabled(enabled);
            components.variableComparison.setEnabled(enabled);
            components.variableValue.setEnabled(enabled);
            if (!enabled) {
                components.variableField.setText("");
                components.variableValue.setText("");
            }
        });
        
        components.selfSwitchCondition.addActionListener(e -> 
            components.selfSwitchValue.setEnabled(
                components.selfSwitchCondition.isSelected()));
    }
    
    public static void setupGraphicListeners(GraphicComponents components, JPanel parent) {
        components.characterButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(parent, "Open Character Selection Dialog (Not yet implemented)");
            // Logic will be moved to a separate handler class
        });
        
        components.directionCombo.addActionListener(e -> {
            // Direction change logic will be moved to a separate handler class
        });
    }
    
    public static void setupMovementListeners(MovementComponents components) {
        components.moveType.addActionListener(e -> 
            components.moveRouteButton.setEnabled(
                components.moveType.getSelectedItem().equals("Custom")));
    }
    
    public static void setupOptionListeners(OptionComponents components, PagePropertiesPanel parent) {
        components.moveAnimation.addActionListener(e -> 
            parent.saveOptionChange("walkAnime", components.moveAnimation.isSelected()));
        
        components.stopAnimation.addActionListener(e -> 
            parent.saveOptionChange("stepAnime", components.stopAnimation.isSelected()));
        
        components.directionFix.addActionListener(e -> 
            parent.saveOptionChange("directionFix", components.directionFix.isSelected()));
        
        components.through.addActionListener(e -> 
            parent.saveOptionChange("through", components.through.isSelected()));
        
        components.alwaysOnTop.addActionListener(e -> 
            parent.saveOptionChange("alwaysOnTop", components.alwaysOnTop.isSelected()));
    }
}