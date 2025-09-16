package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

import libs.json.JSONObject;
import psdk.EventEditor.model.EventPage;

public class PagePropertiesPanel extends JPanel {
    
    // Constants
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    private static final Dimension GRAPHIC_PANEL_SIZE = new Dimension(200, 200);
    private static final Dimension OPTIONS_PANEL_SIZE = new Dimension(150, 150);
    private static final Dimension CHARACTER_PREVIEW_SIZE = new Dimension(96, 96);
    private static final Dimension BUTTON_SIZE = new Dimension(30, 25);
    
    // Direction mappings
    private static final String[] DIRECTION_ARROWS = {"↑", "→", "↓", "←"};
    private static final int[] DIRECTION_VALUES = {8, 6, 2, 4}; // RPG Maker XP values
    
    // Model
    private EventPage currentEventPage;
    
    // UI Components - organized by section
    private ConditionComponents conditionComponents;
    private GraphicComponents graphicComponents;
    private MovementComponents movementComponents;
    private OptionComponents optionComponents;
    private TriggerComponents triggerComponents;

    public PagePropertiesPanel() {
        initializePanel();
        createComponents();
        layoutComponents();
        setupEventListeners();
    }

    // ========== INITIALIZATION ==========
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
    }
    
    private void createComponents() {
        conditionComponents = new ConditionComponents();
        graphicComponents = new GraphicComponents();
        movementComponents = new MovementComponents();
        optionComponents = new OptionComponents();
        triggerComponents = new TriggerComponents();
    }
    
    private void layoutComponents() {
        JPanel contentPanel = createMainContentPanel();
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }
    
    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add sections with spacing
        contentPanel.add(createConditionsPanel());
        contentPanel.add(Box.createVerticalStrut(5));
        
        contentPanel.add(createGraphicMovementPanel());
        contentPanel.add(Box.createVerticalStrut(5));
        
        contentPanel.add(createOptionsTriggersPanel());
        contentPanel.add(Box.createVerticalGlue());
        
        return contentPanel;
    }
    
    private JPanel createGraphicMovementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(createGraphicPanel(), BorderLayout.WEST);
        panel.add(createMovementPanel(), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createOptionsTriggersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(createOptionsPanel(), BorderLayout.WEST);
        panel.add(createTriggerPanel(), BorderLayout.CENTER);
        return panel;
    }

    // ========== COMPONENT CLASSES ==========
    
    private class ConditionComponents {
        JCheckBox switchCondition, variableCondition, selfSwitchCondition;
        JTextField switchField, variableField, variableValue;
        JComboBox<String> variableComparison, selfSwitchValue;
        
        ConditionComponents() {
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
    }
    
    private class GraphicComponents {
        JLabel characterPreview;
        JButton characterButton;
        JComboBox<String> directionCombo;
        
        GraphicComponents() {
            initializeGraphicComponents();
        }
        
        private void initializeGraphicComponents() {
            characterPreview = createCharacterPreview();
            characterButton = createStyledButton("Change graphic", BUTTON_SIZE);
            directionCombo = new JComboBox<>(DIRECTION_ARROWS);
        }
    }
    
    private class MovementComponents {
        JComboBox<String> moveType, moveSpeed, moveFreq;
        JButton moveRouteButton;
        
        MovementComponents() {
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
    }
    
    private class OptionComponents {
        JCheckBox moveAnimation, stopAnimation, directionFix, through, alwaysOnTop;
        
        OptionComponents() {
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
    }
    
    private class TriggerComponents {
        ButtonGroup triggerGroup;
        JRadioButton actionButton, playerTouch, eventTouch, autorun, parallelProcess;
        
        TriggerComponents() {
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
    }

    // ========== PANEL CREATION METHODS ==========
    
    private JPanel createConditionsPanel() {
        JPanel panel = createTitledPanel("Conditions", new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        
        // Switch condition row
        addConditionRow(panel, gbc, 0, 
            conditionComponents.switchCondition, 
            conditionComponents.switchField, 
            null, null, "is ON");
        
        // Variable condition row
        addConditionRow(panel, gbc, 1,
            conditionComponents.variableCondition,
            conditionComponents.variableField,
            conditionComponents.variableComparison,
            conditionComponents.variableValue, null);
        
        // Self switch condition row
        addConditionRow(panel, gbc, 2,
            conditionComponents.selfSwitchCondition,
            conditionComponents.selfSwitchValue,
            null, null, "is ON");
        
        return panel;
    }
    
    private JPanel createGraphicPanel() {
        JPanel panel = createTitledPanel("Graphic", new GridBagLayout());
        panel.setPreferredSize(GRAPHIC_PANEL_SIZE);
        GridBagConstraints gbc = createDefaultConstraints();
        
        // Character preview
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(graphicComponents.characterPreview, gbc);
        
        // Change graphic button
        gbc.gridy = 2;
        panel.add(graphicComponents.characterButton, gbc);
        
        // Direction selection
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Direction:"), gbc);
        gbc.gridx = 1;
        panel.add(graphicComponents.directionCombo, gbc);
        
        return panel;
    }
    
    private JPanel createMovementPanel() {
        JPanel panel = createTitledPanel("Autonomous Movement", new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        
        addLabeledComponent(panel, gbc, 0, "Type:", movementComponents.moveType);
        addLabeledComponent(panel, gbc, 1, "Speed:", movementComponents.moveSpeed);
        addLabeledComponent(panel, gbc, 2, "Freq:", movementComponents.moveFreq);
        
        // Move Route button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(movementComponents.moveRouteButton, gbc);
        
        return panel;
    }
    
    private JPanel createOptionsPanel() {
        JPanel panel = createTitledPanel("Options", new GridBagLayout());
        panel.setPreferredSize(OPTIONS_PANEL_SIZE);
        GridBagConstraints gbc = createDefaultConstraints();
        
        addCheckBoxColumn(panel, gbc, new JCheckBox[]{
            optionComponents.moveAnimation,
            optionComponents.stopAnimation,
            optionComponents.directionFix,
            optionComponents.through,
            optionComponents.alwaysOnTop
        });
        
        return panel;
    }
    
    private JPanel createTriggerPanel() {
        JPanel panel = createTitledPanel("Trigger", new GridBagLayout());
        GridBagConstraints gbc = createDefaultConstraints();
        
        addCheckBoxColumn(panel, gbc, new JRadioButton[]{
            triggerComponents.actionButton,
            triggerComponents.playerTouch,
            triggerComponents.eventTouch,
            triggerComponents.autorun,
            triggerComponents.parallelProcess
        });
        
        return panel;
    }

    // ========== UTILITY METHODS ==========
    
    private JPanel createTitledPanel(String title, LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BACKGROUND_COLOR);
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
    
    private JCheckBox createCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBackground(BACKGROUND_COLOR);
        return checkBox;
    }
    
    private JRadioButton createRadioButton(String text, boolean selected) {
        JRadioButton radioButton = new JRadioButton(text);
        radioButton.setBackground(BACKGROUND_COLOR);
        radioButton.setSelected(selected);
        return radioButton;
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
    
    private JLabel createCharacterPreview() {
        JLabel preview = new JLabel();
        preview.setPreferredSize(CHARACTER_PREVIEW_SIZE);
        preview.setBorder(BorderFactory.createLoweredBevelBorder());
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.setVerticalAlignment(SwingConstants.CENTER);
        preview.setText("<html><center>No Character<br>Selected</center></html>");
        preview.setBackground(Color.WHITE);
        preview.setOpaque(true);
        return preview;
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

    // ========== EVENT LISTENERS ==========
    
    private void setupEventListeners() {
        setupConditionListeners();
        setupGraphicListeners();
        setupMovementListeners();
    }
    
    private void setupConditionListeners() {
        conditionComponents.switchCondition.addActionListener(e -> {
            boolean enabled = conditionComponents.switchCondition.isSelected();
            conditionComponents.switchField.setEnabled(enabled);
            if (!enabled) conditionComponents.switchField.setText("");
        });
        
        conditionComponents.variableCondition.addActionListener(e -> {
            boolean enabled = conditionComponents.variableCondition.isSelected();
            conditionComponents.variableField.setEnabled(enabled);
            conditionComponents.variableComparison.setEnabled(enabled);
            conditionComponents.variableValue.setEnabled(enabled);
            if (!enabled) {
                conditionComponents.variableField.setText("");
                conditionComponents.variableValue.setText("");
            }
        });
        
        conditionComponents.selfSwitchCondition.addActionListener(e -> 
            conditionComponents.selfSwitchValue.setEnabled(
                conditionComponents.selfSwitchCondition.isSelected()));
    }
    
    private void setupGraphicListeners() {
        graphicComponents.characterButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Open Character Selection Dialog (Not yet implemented)");
            updateCharacterPreview(currentEventPage.getCharacterName() + ".png");
        });
        
        graphicComponents.directionCombo.addActionListener(e -> {
            String characterName = getCurrentCharacterName();
            if (characterName != null && !characterName.isEmpty()) {
                updateCharacterPreview(characterName);
            }
        });
    }
    
    private void setupMovementListeners() {
        movementComponents.moveType.addActionListener(e -> 
            movementComponents.moveRouteButton.setEnabled(
                movementComponents.moveType.getSelectedItem().equals("Custom")));
    }

    // ========== PUBLIC API METHODS ==========
    
    public void updatePageProperties(EventPage page) {
        if (page == null) {
            clearAllFields();
            return;
        }
        
        currentEventPage = page;
        updateConditions(page);
        updateGraphic(page);
        updateMovement(page);
        updateOptions(page);
        updateTrigger(page);
    }
    
    private void updateConditions(EventPage page) {
        JSONObject condition = page.getCondition();
        
        // Switch condition
        boolean hasSwitch = condition.optBoolean("switch1_valid", false);
        conditionComponents.switchCondition.setSelected(hasSwitch);
        conditionComponents.switchField.setEnabled(hasSwitch);
        conditionComponents.switchField.setText(hasSwitch ? 
            String.format("%04d", condition.optInt("switch1_id", 0)) : "");
        
        // Variable condition
        boolean hasVariable = condition.optBoolean("variable_valid", false);
        conditionComponents.variableCondition.setSelected(hasVariable);
        setComponentsEnabled(hasVariable, conditionComponents.variableField, 
            conditionComponents.variableComparison, conditionComponents.variableValue);
        
        if (hasVariable) {
            conditionComponents.variableField.setText(
                String.format("%04d", condition.optInt("variable_id", 0)));
            conditionComponents.variableValue.setText(
                String.valueOf(condition.optInt("variable_value", 0)));
            conditionComponents.variableComparison.setSelectedItem("=");
        } else {
            conditionComponents.variableField.setText("");
            conditionComponents.variableValue.setText("");
        }
        
        // Self switch condition
        boolean hasSelfSwitch = condition.optBoolean("self_switch_valid", false);
        conditionComponents.selfSwitchCondition.setSelected(hasSelfSwitch);
        conditionComponents.selfSwitchValue.setEnabled(hasSelfSwitch);
        conditionComponents.selfSwitchValue.setSelectedItem(
            hasSelfSwitch ? condition.optString("self_switch_ch", "A") : "A");
    }
    
    private void updateGraphic(EventPage page) {
        String characterName = page.getCharacterName();
        
        if (characterName == null || characterName.isEmpty()) {
            SpritePreviewManager.clearPreview(graphicComponents.characterPreview);
        } else {
            SpritePreviewManager.updateSpritePreview(
                graphicComponents.characterPreview, characterName, 
                page.getCharacterIndex(), page.getDirection(), page.getPattern());
        }
        
        // Set direction combo based on RPG Maker XP direction values
        setDirectionCombo(page.getDirection());
    }
    
    private void updateMovement(EventPage page) {
        setComboByIndex(movementComponents.moveType, page.getMove_type(), 
            new String[]{"Fixed", "Random", "Approach", "Custom"});
        
        int speed = page.getMoveSpeed();
        if (speed >= 1 && speed <= 6) {
            movementComponents.moveSpeed.setSelectedIndex(speed - 1);
        }
        
        int frequency = page.getMoveFrequency();
        if (frequency >= 1 && frequency <= 5) {
            movementComponents.moveFreq.setSelectedIndex(frequency - 1);
        }
        
        movementComponents.moveRouteButton.setEnabled(page.getMove_type() == 3);
    }
    
    private void updateOptions(EventPage page) {
        optionComponents.moveAnimation.setSelected(page.isWalkAnime());
        optionComponents.stopAnimation.setSelected(page.isStepAnime());
        optionComponents.directionFix.setSelected(page.isDirectionFix());
        optionComponents.through.setSelected(page.isThrough());
        optionComponents.alwaysOnTop.setSelected(page.isAlwaysOnTop());
    }
    
    private void updateTrigger(EventPage page) {
        JRadioButton[] buttons = {
            triggerComponents.actionButton, triggerComponents.playerTouch,
            triggerComponents.eventTouch, triggerComponents.autorun,
            triggerComponents.parallelProcess
        };
        
        int trigger = page.getTrigger();
        if (trigger >= 0 && trigger < buttons.length) {
            buttons[trigger].setSelected(true);
        } else {
            triggerComponents.actionButton.setSelected(true);
        }
    }
    
    private void clearAllFields() {
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
    
    private void setComponentsEnabled(boolean enabled, JComponent... components) {
        for (JComponent component : components) {
            component.setEnabled(enabled);
        }
    }
    
    private void setComboByIndex(JComboBox<String> combo, int value, String[] options) {
        if (value >= 0 && value < options.length) {
            combo.setSelectedItem(options[value]);
        } else if (options.length > 0) {
            combo.setSelectedItem(options[0]);
        }
    }
    
    private void setDirectionCombo(int direction) {
        for (int i = 0; i < DIRECTION_VALUES.length; i++) {
            if (DIRECTION_VALUES[i] == direction) {
                graphicComponents.directionCombo.setSelectedItem(DIRECTION_ARROWS[i]);
                return;
            }
        }
        graphicComponents.directionCombo.setSelectedItem("↓"); // Default
    }
    
    private int getDirectionFromCombo() {
        String selected = (String) graphicComponents.directionCombo.getSelectedItem();
        for (int i = 0; i < DIRECTION_ARROWS.length; i++) {
            if (DIRECTION_ARROWS[i].equals(selected)) {
                return DIRECTION_VALUES[i];
            }
        }
        return 2; // Default to down
    }
    
    private void updateCharacterPreview(String characterFileName) {
        int characterIndex = 0;
        int direction = getDirectionFromCombo();
        int pattern = 1;
        
        SpritePreviewManager.updateSpritePreview(
            graphicComponents.characterPreview, characterFileName, 
            characterIndex, direction, pattern);
    }
    
    private String getCurrentCharacterName() {
        return currentEventPage != null ? currentEventPage.getCharacterName() : null;
    }
}