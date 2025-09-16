package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class PanelCreator {
    
    private static final Dimension GRAPHIC_PANEL_SIZE = new Dimension(200, 200);
    private static final Dimension OPTIONS_PANEL_SIZE = new Dimension(150, 150);
    
    private final Color backgroundColor;
    
    public PanelCreator(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    // ========== PANEL CREATION METHODS ==========
    
    public JPanel createConditionsPanel(ConditionComponents components) {
        JPanel panel = createTitledPanel("Conditions", new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        
        // Switch condition row
        addConditionRow(panel, gbc, 0, 
            components.switchCondition, 
            components.switchField, 
            null, null, "is ON");
        
        // Variable condition row
        addConditionRow(panel, gbc, 1,
            components.variableCondition,
            components.variableField,
            components.variableComparison,
            components.variableValue, null);
        
        // Self switch condition row
        addConditionRow(panel, gbc, 2,
            components.selfSwitchCondition,
            components.selfSwitchValue,
            null, null, "is ON");
        
        return panel;
    }
    
    public JPanel createGraphicPanel(GraphicComponents components) {
        JPanel panel = createTitledPanel("Graphic", new GridBagLayout());
        panel.setPreferredSize(GRAPHIC_PANEL_SIZE);
        GridBagConstraints gbc = createDefaultConstraints();
        
        // Character preview
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(components.characterPreview, gbc);
        
        // Change graphic button
        gbc.gridy = 2;
        panel.add(components.characterButton, gbc);
        
        // Direction selection
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Direction:"), gbc);
        gbc.gridx = 1;
        panel.add(components.directionCombo, gbc);
        
        return panel;
    }
    
    public JPanel createMovementPanel(MovementComponents components) {
        JPanel panel = createTitledPanel("Autonomous Movement", new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        
        addLabeledComponent(panel, gbc, 0, "Type:", components.moveType);
        addLabeledComponent(panel, gbc, 1, "Speed:", components.moveSpeed);
        addLabeledComponent(panel, gbc, 2, "Freq:", components.moveFreq);
        
        // Move Route button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(components.moveRouteButton, gbc);
        
        return panel;
    }
    
    public JPanel createOptionsPanel(OptionComponents components) {
        JPanel panel = createTitledPanel("Options", new GridBagLayout());
        panel.setPreferredSize(OPTIONS_PANEL_SIZE);
        GridBagConstraints gbc = createDefaultConstraints();
        
        addCheckBoxColumn(panel, gbc, new JCheckBox[]{
            components.moveAnimation,
            components.stopAnimation,
            components.directionFix,
            components.through,
            components.alwaysOnTop
        });
        
        return panel;
    }
    
    public JPanel createTriggerPanel(TriggerComponents components) {
        JPanel panel = createTitledPanel("Trigger", new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        
        addCheckBoxColumn(panel, gbc, new JRadioButton[]{
            components.actionButton,
            components.playerTouch,
            components.eventTouch,
            components.autorun,
            components.parallelProcess
        });
        
        return panel;
    }
    
    // ========== UTILITY METHODS ==========
    
    private JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(backgroundColor);
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), title);
        panel.setBorder(border);
        return panel;
    }
    
    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }
    
    private void addConditionRow(JPanel panel, GridBagConstraints gbc, int row,
                                JComponent checkBox, JComponent field1, 
                                JComponent field2, JComponent field3, String label) {
        gbc.gridy = row;
        
        gbc.gridx = 0; gbc.gridwidth = 1;
        panel.add(checkBox, gbc);
        
        gbc.gridx = 1;
        if (field2 == null) { // Two-component row
            gbc.gridwidth = 2;
            panel.add(field1, gbc);
        } else { // Three-component row
            panel.add(field1, gbc);
            gbc.gridx = 2;
            panel.add(field2, gbc);
            if (field3 != null) {
                gbc.gridx = 3;
                panel.add(field3, gbc);
            }
        }
        
        if (label != null) {
            gbc.gridx = field3 != null ? 4 : 3;
            gbc.gridwidth = 1;
            panel.add(new JLabel(label), gbc);
        }
    }
    
    private void addLabeledComponent(JPanel panel, GridBagConstraints gbc, int row,
                                   String labelText, JComponent component) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }
    
    private void addCheckBoxColumn(JPanel panel, GridBagConstraints gbc, JComponent[] components) {
        gbc.gridx = 0;
        for (int i = 0; i < components.length; i++) {
            gbc.gridy = i;
            panel.add(components[i], gbc);
        }
    }
}