package psdk.EventEditor.model.EventEditorDialog.PageProperties;

import javax.swing.*;
import java.awt.*;

public class ConditionComponents {
    
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    public JCheckBox switchCondition, variableCondition, selfSwitchCondition;
    public JTextField switchField, variableField, variableValue;
    public JComboBox<String> variableComparison, selfSwitchValue;
    
    public ConditionComponents() {
        initializeConditionComponents();
    }
    
    private void initializeConditionComponents() {
        // Switch components
        switchCondition = createCheckBox("Switch");
        switchField = new JTextField(15);
        switchField.setEnabled(false);
        
        // Variable components
        variableCondition = createCheckBox("Variable");
        variableField = new JTextField(10);
        variableField.setEnabled(false);
        variableComparison = new JComboBox<>(new String[]{"=", "≥", "≤", ">", "<", "≠"});
        variableComparison.setEnabled(false);
        variableValue = new JTextField(5);
        variableValue.setEnabled(false);
        
        // Self switch components
        selfSwitchCondition = createCheckBox("Self Switch");
        selfSwitchValue = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        selfSwitchValue.setEnabled(false);
    }
    
    private JCheckBox createCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBackground(BACKGROUND_COLOR);
        return checkBox;
    }
}