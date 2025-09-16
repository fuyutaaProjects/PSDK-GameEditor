package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventPage;

public class PagePropertiesUpdater {
    
    // Direction mappings
    private static final String[] DIRECTION_ARROWS = {"←", "→", "↓", "↑"};
    private static final int[] DIRECTION_VALUES = {8, 6, 2, 4}; // RPG Maker XP values
    
    public static void updateAllSections(EventPage page, ConditionComponents conditionComponents,
                                       GraphicComponents graphicComponents, MovementComponents movementComponents,
                                       OptionComponents optionComponents, TriggerComponents triggerComponents) {
        updateConditions(page, conditionComponents);
        updateGraphic(page, graphicComponents);
        updateMovement(page, movementComponents);
        updateOptions(page, optionComponents);
        updateTrigger(page, triggerComponents);
    }
    
    public static void updateConditions(EventPage page, ConditionComponents components) {
        JSONObject condition = page.getCondition();
        
        // Switch condition
        boolean hasSwitch = condition.optBoolean("switch1_valid", false);
        components.switchCondition.setSelected(hasSwitch);
        components.switchField.setEnabled(hasSwitch);
        components.switchField.setText(hasSwitch ? 
            String.format("%04d", condition.optInt("switch1_id", 0)) : "");
        
        // Variable condition
        boolean hasVariable = condition.optBoolean("variable_valid", false);
        components.variableCondition.setSelected(hasVariable);
        setComponentsEnabled(hasVariable, components.variableField, 
            components.variableComparison, components.variableValue);
        
        if (hasVariable) {
            components.variableField.setText(
                String.format("%04d", condition.optInt("variable_id", 0)));
            components.variableValue.setText(
                String.valueOf(condition.optInt("variable_value", 0)));
            components.variableComparison.setSelectedItem("=");
        } else {
            components.variableField.setText("");
            components.variableValue.setText("");
        }
        
        // Self switch condition
        boolean hasSelfSwitch = condition.optBoolean("self_switch_valid", false);
        components.selfSwitchCondition.setSelected(hasSelfSwitch);
        components.selfSwitchValue.setEnabled(hasSelfSwitch);
        components.selfSwitchValue.setSelectedItem(
            hasSelfSwitch ? condition.optString("self_switch_ch", "A") : "A");
    }
    
    public static void updateGraphic(EventPage page, GraphicComponents components) {
        String characterName = page.getCharacterName();
        
        if (characterName == null || characterName.isEmpty()) {
            SpritePreviewManager.clearPreview(components.characterPreview);
        } else {
            SpritePreviewManager.updateSpritePreview(
                components.characterPreview, characterName, 
                page.getCharacterIndex(), page.getDirection(), page.getPattern());
        }
        
        // Set direction combo based on RPG Maker XP direction values
        setDirectionCombo(components.directionCombo, page.getDirection());
    }
    
    public static void updateMovement(EventPage page, MovementComponents components) {
        setComboByIndex(components.moveType, page.getMove_type(), 
            new String[]{"Fixed", "Random", "Approach", "Custom"});
        
        int speed = page.getMoveSpeed();
        if (speed >= 1 && speed <= 6) {
            components.moveSpeed.setSelectedIndex(speed - 1);
        }
        
        int frequency = page.getMoveFrequency();
        if (frequency >= 1 && frequency <= 5) {
            components.moveFreq.setSelectedIndex(frequency - 1);
        }
        
        components.moveRouteButton.setEnabled(page.getMove_type() == 3);
    }
    
    public static void updateOptions(EventPage page, OptionComponents components) {
        components.moveAnimation.setSelected(page.isWalkAnime());
        components.stopAnimation.setSelected(page.isStepAnime());
        components.directionFix.setSelected(page.isDirectionFix());
        components.through.setSelected(page.isThrough());
        components.alwaysOnTop.setSelected(page.isAlwaysOnTop());
    }
    
    public static void updateTrigger(EventPage page, TriggerComponents components) {
        JRadioButton[] buttons = {
            components.actionButton, components.playerTouch,
            components.eventTouch, components.autorun,
            components.parallelProcess
        };
        
        int trigger = page.getTrigger();
        if (trigger >= 0 && trigger < buttons.length) {
            buttons[trigger].setSelected(true);
        } else {
            components.actionButton.setSelected(true);
        }
    }
    
    public static void clearAllFields(ConditionComponents conditionComponents, 
                                    GraphicComponents graphicComponents,
                                    MovementComponents movementComponents, 
                                    OptionComponents optionComponents,
                                    TriggerComponents triggerComponents) {
        // Clear conditions
        conditionComponents.switchCondition.setSelected(false);
        conditionComponents.switchField.setText("");
        conditionComponents.switchField.setEnabled(false);
        
        conditionComponents.variableCondition.setSelected(false);
        conditionComponents.variableField.setText("");
        conditionComponents.variableComparison.setSelectedItem("=");
        conditionComponents.variableValue.setText("");
        setComponentsEnabled(false, conditionComponents.variableField,
            conditionComponents.variableComparison, conditionComponents.variableValue);
        
        conditionComponents.selfSwitchCondition.setSelected(false);
        conditionComponents.selfSwitchValue.setSelectedItem("A");
        conditionComponents.selfSwitchValue.setEnabled(false);
        
        // Clear graphic
        SpritePreviewManager.clearPreview(graphicComponents.characterPreview);
        graphicComponents.directionCombo.setSelectedItem("↓");
        
        // Clear movement
        movementComponents.moveType.setSelectedItem("Fixed");
        movementComponents.moveSpeed.setSelectedIndex(2);
        movementComponents.moveFreq.setSelectedIndex(2);
        movementComponents.moveRouteButton.setEnabled(false);
        
        // Clear options
        optionComponents.moveAnimation.setSelected(true);
        optionComponents.stopAnimation.setSelected(false);
        optionComponents.directionFix.setSelected(false);
        optionComponents.through.setSelected(false);
        optionComponents.alwaysOnTop.setSelected(false);
        
        // Clear trigger
        triggerComponents.actionButton.setSelected(true);
    }
    
    // ========== HELPER METHODS ==========
    
    private static void setComponentsEnabled(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }
    
    private static void setComboByIndex(JComboBox<String> combo, int value, String[] options) {
        if (value >= 0 && value < options.length) {
            combo.setSelectedItem(options[value]);
        } else if (options.length > 0) {
            combo.setSelectedItem(options[0]);
        }
    }
    
    private static void setDirectionCombo(JComboBox<String> directionCombo, int direction) {
        for (int i = 0; i < DIRECTION_VALUES.length; i++) {
            if (DIRECTION_VALUES[i] == direction) {
                directionCombo.setSelectedItem(DIRECTION_ARROWS[i]);
                return;
            }
        }
        directionCombo.setSelectedItem("↓"); // Default
    }
    
    public static int getDirectionFromCombo(JComboBox<String> directionCombo) {
        String selected = (String) directionCombo.getSelectedItem();
        for (int i = 0; i < DIRECTION_ARROWS.length; i++) {
            if (DIRECTION_ARROWS[i].equals(selected)) {
                return DIRECTION_VALUES[i];
            }
        }
        return 2; // Default to down
    }
}