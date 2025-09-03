package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import libs.json.JSONObject;
import psdk.EventEditor.ConfigManager;
import psdk.EventEditor.model.EventPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.awt.Dimension;
import java.awt.Image;

public class PagePropertiesPanel extends JPanel {
    private EventPage currentEventPage;


    // Page properties components
    private JCheckBox switchCondition;
    private JTextField switchField;
    private JCheckBox variableCondition;
    private JTextField variableField;
    private JComboBox<String> variableComparison;
    private JTextField variableValue;
    private JCheckBox selfSwitchCondition;
    private JComboBox<String> selfSwitchValue;

    // Graphic components
    private JLabel characterPreview;
    private JButton characterButton;
    private JComboBox<String> directionCombo;

    // Movement components
    private JComboBox<String> moveType;
    private JComboBox<String> moveSpeed;
    private JComboBox<String> moveFreq;
    private JButton moveRouteButton;
    
    // Options components
    private JCheckBox moveAnimation;
    private JCheckBox stopAnimation;
    private JCheckBox directionFix;
    private JCheckBox through;
    private JCheckBox alwaysOnTop;
    
    // Trigger components
    private ButtonGroup triggerGroup;
    private JRadioButton actionButton;
    private JRadioButton playerTouch;
    private JRadioButton eventTouch;
    private JRadioButton autorun;
    private JRadioButton parallelProcess;

public PagePropertiesPanel() {
        // 1. Définir la disposition (layout) du PagePropertiesPanel lui-même
        this.setLayout(new BorderLayout()); // Utilise BorderLayout pour la disposition principale du panneau
        this.setBackground(new Color(236, 233, 216)); // Définir la couleur d'arrière-plan pour le panneau entier

        // 2. Créer un nouveau JPanel qui contiendra toutes les sections (conditions, graphisme, etc.)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS)); // Disposition verticale pour les sections
        contentPanel.setBackground(new Color(236, 233, 216));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Conditions panel
        contentPanel.add(createConditionsPanel());
        contentPanel.add(Box.createVerticalStrut(5));

        // Create a horizontal panel for Graphic and Autonomous Movement
        JPanel horizontalPanel = new JPanel(new BorderLayout());
        horizontalPanel.setBackground(new Color(236, 233, 216));
        
        // Graphic panel (left side)
        JPanel graphicPanel = createGraphicPanel();
        horizontalPanel.add(graphicPanel, BorderLayout.WEST);
        
        // Autonomous Movement panel (right side)
        JPanel movementPanel = createMovementPanel();
        horizontalPanel.add(movementPanel, BorderLayout.CENTER);
        
        contentPanel.add(horizontalPanel);
        contentPanel.add(Box.createVerticalStrut(5));

        // Options and Trigger in horizontal layout
        JPanel optionsTriggersPanel = new JPanel(new BorderLayout());
        optionsTriggersPanel.setBackground(new Color(236, 233, 216));
        
        // Options panel (left)
        JPanel optionsPanel = createOptionsPanel();
        optionsTriggersPanel.add(optionsPanel, BorderLayout.WEST);
        
        // Trigger panel (right)
        JPanel triggerPanel = createTriggerPanel();
        optionsTriggersPanel.add(triggerPanel, BorderLayout.CENTER);
        
        contentPanel.add(optionsTriggersPanel);
        contentPanel.add(Box.createVerticalGlue());

        // 3. Ajouter le 'contentPanel' (qui contient tout) au PagePropertiesPanel lui-même (this)
        this.add(new JScrollPane(contentPanel), BorderLayout.CENTER); // Ajoute le panneau de contenu dans un JScrollPane
        // --- FIN DES MODIFICATIONS ---
    }

    private JPanel createConditionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 233, 216));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Conditions");
        panel.setBorder(border);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        // Switch condition
        gbc.gridx = 0; gbc.gridy = 0;
        switchCondition = new JCheckBox("Switch");
        switchCondition.setBackground(new Color(236, 233, 216));
        panel.add(switchCondition, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        switchField = new JTextField(15); // Set preferred column width
        switchField.setEnabled(false);
        panel.add(switchField, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("is ON"), gbc);

        // Variable condition
        gbc.gridx = 0; gbc.gridy = 1;
        variableCondition = new JCheckBox("Variable");
        variableCondition.setBackground(new Color(236, 233, 216));
        panel.add(variableCondition, gbc);

        gbc.gridx = 1;
        variableField = new JTextField(10); // Set preferred column width
        variableField.setEnabled(false);
        panel.add(variableField, gbc);

        gbc.gridx = 2;
        variableComparison = new JComboBox<>(new String[]{"=", "≥", "≤", ">", "<", "≠"}); // Updated comparison options
        variableComparison.setEnabled(false);
        panel.add(variableComparison, gbc);

        gbc.gridx = 3;
        variableValue = new JTextField(5); // Set preferred column width
        variableValue.setEnabled(false);
        panel.add(variableValue, gbc);

        // Self Switch condition
        gbc.gridx = 0; gbc.gridy = 2;
        selfSwitchCondition = new JCheckBox("Self Switch");
        selfSwitchCondition.setBackground(new Color(236, 233, 216));
        panel.add(selfSwitchCondition, gbc);

        gbc.gridx = 1;
        selfSwitchValue = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        selfSwitchValue.setEnabled(false);
        panel.add(selfSwitchValue, gbc);

        gbc.gridx = 2;
        panel.add(new JLabel("is ON"), gbc);

        // Enable/disable fields based on checkbox state
        switchCondition.addActionListener(e -> {
            boolean enabled = switchCondition.isSelected();
            switchField.setEnabled(enabled);
            // If switch is unchecked, clear the text
            if (!enabled) {
                switchField.setText("");
            }
        });
        variableCondition.addActionListener(e -> {
            boolean enabled = variableCondition.isSelected();
            variableField.setEnabled(enabled);
            variableComparison.setEnabled(enabled);
            variableValue.setEnabled(enabled);
            // If variable is unchecked, clear the texts
            if (!enabled) {
                variableField.setText("");
                variableValue.setText("");
            }
        });
        selfSwitchCondition.addActionListener(e -> selfSwitchValue.setEnabled(selfSwitchCondition.isSelected()));

        return panel;
    }





       private JPanel createGraphicPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 233, 216));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Graphic");
        panel.setBorder(border);
        panel.setPreferredSize(new Dimension(200, 200));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        // Character preview and button
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        characterPreview = new JLabel();
        characterPreview.setPreferredSize(new Dimension(96, 96));
        characterPreview.setBorder(BorderFactory.createLoweredBevelBorder());
        characterPreview.setHorizontalAlignment(SwingConstants.CENTER);
        characterPreview.setVerticalAlignment(SwingConstants.CENTER); // Center text vertically
        characterPreview.setText("<html><center>No Character<br>Selected</center></html>"); // Placeholder text
        characterPreview.setBackground(Color.WHITE);
        characterPreview.setOpaque(true);
        panel.add(characterPreview, gbc);

        gbc.gridy = 2;
        characterButton = new JButton("Change graphic");
        characterButton.setPreferredSize(new Dimension(30, 25));
        styleButton(characterButton);
        // Add action listener to characterButton
        characterButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Open Character Selection Dialog (Not yet implemented)");
            updateCharacterPreview(currentEventPage.getCharacterName() + ".png"); 
        });
        panel.add(characterButton, gbc);

        // Direction
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Direction:"), gbc);

        gbc.gridx = 1;
        directionCombo = new JComboBox<>(new String[]{"↑", "→", "↓", "←"});
        // Ajouter un listener pour mettre à jour le preview quand la direction change
        directionCombo.addActionListener(e -> {
            String characterName = getCurrentCharacterName();
            if (characterName != null && !characterName.isEmpty()) {
                updateCharacterPreview(characterName);
            }
        });
        panel.add(directionCombo, gbc);

        return panel;
    }



    private JPanel createMovementPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 233, 216));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Autonomous Movement");
        panel.setBorder(border);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        // Type
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Type:"), gbc);

        gbc.gridx = 1;
        moveType = new JComboBox<>(new String[]{"Fixed", "Random", "Approach", "Custom"});
        panel.add(moveType, gbc);

        // Speed
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Speed:"), gbc);

        gbc.gridx = 1;
        moveSpeed = new JComboBox<>(new String[]{"1: Slowest", "2: Slower", "3: Slow", "4: Normal", "5: Fast", "6: Faster"});
        moveSpeed.setSelectedIndex(2);
        panel.add(moveSpeed, gbc);

        // Frequency
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Freq:"), gbc);

        gbc.gridx = 1;
        moveFreq = new JComboBox<>(new String[]{"1: Lowest", "2: Lower", "3: Low", "4: Normal", "5: High"});
        moveFreq.setSelectedIndex(2);
        panel.add(moveFreq, gbc);

        // Move Route button
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        moveRouteButton = new JButton("Move Route...");
        moveRouteButton.setEnabled(false);
        styleButton(moveRouteButton);
        panel.add(moveRouteButton, gbc);

        // Enable Move Route button only for Custom type
        moveType.addActionListener(e -> {
            moveRouteButton.setEnabled(moveType.getSelectedItem().equals("Custom"));
        });

        return panel;
    }

    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 233, 216));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Options");
        panel.setBorder(border);
        panel.setPreferredSize(new Dimension(150, 150));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        moveAnimation = new JCheckBox("Move Animation");
        moveAnimation.setSelected(true);
        moveAnimation.setBackground(new Color(236, 233, 216));
        panel.add(moveAnimation, gbc);

        gbc.gridy = 1;
        stopAnimation = new JCheckBox("Stop Animation");
        stopAnimation.setBackground(new Color(236, 233, 216));
        panel.add(stopAnimation, gbc);

        gbc.gridy = 2;
        directionFix = new JCheckBox("Direction Fix");
        directionFix.setBackground(new Color(236, 233, 216));
        panel.add(directionFix, gbc);

        gbc.gridy = 3;
        through = new JCheckBox("Through");
        through.setBackground(new Color(236, 233, 216));
        panel.add(through, gbc);

        gbc.gridy = 4;
        alwaysOnTop = new JCheckBox("Always on Top");
        alwaysOnTop.setBackground(new Color(236, 233, 216));
        panel.add(alwaysOnTop, gbc);

        return panel;
    }

    private JPanel createTriggerPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(236, 233, 216));
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Trigger");
        panel.setBorder(border);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        triggerGroup = new ButtonGroup();

        gbc.gridx = 0; gbc.gridy = 0;
        actionButton = new JRadioButton("Action Button");
        actionButton.setSelected(true);
        actionButton.setBackground(new Color(236, 233, 216));
        triggerGroup.add(actionButton);
        panel.add(actionButton, gbc);

        gbc.gridy = 1;
        playerTouch = new JRadioButton("Player Touch");
        playerTouch.setBackground(new Color(236, 233, 216));
        triggerGroup.add(playerTouch);
        panel.add(playerTouch, gbc);

        gbc.gridy = 2;
        eventTouch = new JRadioButton("Event Touch");
        eventTouch.setBackground(new Color(236, 233, 216));
        triggerGroup.add(eventTouch);
        panel.add(eventTouch, gbc);

        gbc.gridy = 3;
        autorun = new JRadioButton("Autorun");
        autorun.setBackground(new Color(236, 233, 216));
        triggerGroup.add(autorun);
        panel.add(autorun, gbc);

        gbc.gridy = 4;
        parallelProcess = new JRadioButton("Parallel Process");
        parallelProcess.setBackground(new Color(236, 233, 216));
        triggerGroup.add(parallelProcess);
        panel.add(parallelProcess, gbc);

        return panel;
    }



    private void styleButton(JButton button) {
        button.setBackground(new Color(236, 233, 216));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }









public void updatePageProperties(EventPage page) {
    if (page == null) {
        // Clear all fields if no page is provided
        clearAllFields();
        return;
    }

    currentEventPage = page;

    // --- CONDITIONS ---
    JSONObject condition = page.getCondition();
    
    // Switch Condition (switch1_valid)
    boolean hasSwitch = condition.optBoolean("switch1_valid", false);
    switchCondition.setSelected(hasSwitch);
    switchField.setEnabled(hasSwitch);
    if (hasSwitch) {
        int switchId = condition.optInt("switch1_id", 0);
        switchField.setText(String.format("%04d", switchId)); // Format as 4-digit ID
    } else {
        switchField.setText("");
    }

    // Variable Condition
    boolean hasVariable = condition.optBoolean("variable_valid", false);
    variableCondition.setSelected(hasVariable);
    variableField.setEnabled(hasVariable);
    variableComparison.setEnabled(hasVariable);
    variableValue.setEnabled(hasVariable);
    if (hasVariable) {
        int variableId = condition.optInt("variable_id", 0);
        int variableVal = condition.optInt("variable_value", 0);
        variableField.setText(String.format("%04d", variableId));
        variableValue.setText(String.valueOf(variableVal));
        // Note: RPG Maker XP uses different comparison types, you might need to map them
        // For now, defaulting to "=" - you may need to add comparison type to EventPage
        variableComparison.setSelectedItem("=");
    } else {
        variableField.setText("");
        variableValue.setText("");
    }

    // Self Switch Condition
    boolean hasSelfSwitch = condition.optBoolean("self_switch_valid", false);
    selfSwitchCondition.setSelected(hasSelfSwitch);
    selfSwitchValue.setEnabled(hasSelfSwitch);
    if (hasSelfSwitch) {
        String selfSwitchCh = condition.optString("self_switch_ch", "A");
        selfSwitchValue.setSelectedItem(selfSwitchCh);
    } else {
        selfSwitchValue.setSelectedItem("A"); // Default
    }

    // --- GRAPHIC ---
    JSONObject graphic = page.getGraphic();
    String characterName = page.getCharacterName();
    int characterIndex = page.getCharacterIndex();
    int direction = page.getDirection();
    int pattern = page.getPattern();
    
    // Determine graphic type based on character name
    if (characterName == null || characterName.isEmpty()) {
        SpritePreviewManager.clearPreview(characterPreview);
    } else {
        // Utiliser les vraies valeurs de l'EventPage
        SpritePreviewManager.updateSpritePreview(characterPreview, characterName, characterIndex, direction, pattern);
    }

    // Direction mapping: RPG Maker XP uses 2=Down, 4=Left, 6=Right, 8=Up
    switch (direction) {
        case 8: directionCombo.setSelectedItem("↑"); break;
        case 6: directionCombo.setSelectedItem("→"); break;
        case 2: directionCombo.setSelectedItem("↓"); break;
        case 4: directionCombo.setSelectedItem("←"); break;
        default: directionCombo.setSelectedItem("↓"); break; // Default
    }

    // --- MOVEMENT ---
    int moveType = page.getMove_type();
    switch (moveType) {
        case 0: this.moveType.setSelectedItem("Fixed"); break;
        case 1: this.moveType.setSelectedItem("Random"); break;
        case 2: this.moveType.setSelectedItem("Approach"); break;
        case 3: this.moveType.setSelectedItem("Custom"); break;
        default: this.moveType.setSelectedItem("Fixed"); break;
    }

    // Move Speed (RPG Maker XP uses 1-6)
    int speed = page.getMoveSpeed();
    if (speed >= 1 && speed <= 6) {
        moveSpeed.setSelectedIndex(speed - 1);
    } else {
        moveSpeed.setSelectedIndex(2); // Default to "3: Slow"
    }

    // Move Frequency (RPG Maker XP uses 1-5)
    int frequency = page.getMoveFrequency();
    if (frequency >= 1 && frequency <= 5) {
        moveFreq.setSelectedIndex(frequency - 1);
    } else {
        moveFreq.setSelectedIndex(2); // Default to "3: Low"
    }

    // Enable Move Route button only for Custom type
    moveRouteButton.setEnabled(moveType == 3);

    // --- OPTIONS ---
    moveAnimation.setSelected(page.isWalkAnime());
    stopAnimation.setSelected(page.isStepAnime());
    directionFix.setSelected(page.isDirectionFix());
    through.setSelected(page.isThrough());
    alwaysOnTop.setSelected(page.isAlwaysOnTop());

    // --- TRIGGER ---
    int trigger = page.getTrigger();
    switch (trigger) {
        case 0: actionButton.setSelected(true); break;
        case 1: playerTouch.setSelected(true); break;
        case 2: eventTouch.setSelected(true); break;
        case 3: autorun.setSelected(true); break;
        case 4: parallelProcess.setSelected(true); break;
        default: actionButton.setSelected(true); break;
    }
}

/**
 * Helper method to clear all fields when no page is provided
 */
private void clearAllFields() {
    // Clear conditions
    switchCondition.setSelected(false);
    switchField.setText("");
    switchField.setEnabled(false);
    
    variableCondition.setSelected(false);
    variableField.setText("");
    variableComparison.setSelectedItem("=");
    variableValue.setText("");
    variableField.setEnabled(false);
    variableComparison.setEnabled(false);
    variableValue.setEnabled(false);
    
    selfSwitchCondition.setSelected(false);
    selfSwitchValue.setSelectedItem("A");
    selfSwitchValue.setEnabled(false);
    
    SpritePreviewManager.clearPreview(characterPreview);
    directionCombo.setSelectedItem("↓");
    
    // Clear movement
    moveType.setSelectedItem("Fixed");
    moveSpeed.setSelectedIndex(2);
    moveFreq.setSelectedIndex(2);
    moveRouteButton.setEnabled(false);
    
    // Clear options
    moveAnimation.setSelected(true);
    stopAnimation.setSelected(false);
    directionFix.setSelected(false);
    through.setSelected(false);
    alwaysOnTop.setSelected(false);
    
    // Clear trigger
    actionButton.setSelected(true);
}

/**
     * Updates the character preview JLabel with the specified character sprite.
     * @param characterFileName The filename of the character sprite (e.g., "Actor1.png").
     */
    private void updateCharacterPreview(String characterFileName) {
        // Utiliser les valeurs par défaut pour character index et pattern
        int characterIndex = 0;  // Premier personnage de la spritesheet
        int direction = getDirectionFromCombo();  // Direction actuelle sélectionnée
        int pattern = 1;  // Pattern central (frame du milieu)
        
        SpritePreviewManager.updateSpritePreview(characterPreview, characterFileName, characterIndex, direction, pattern);
    }

    /**
     * Convertit la sélection du combo de direction en valeur RPG Maker XP
     */
    private int getDirectionFromCombo() {
        String selectedDirection = (String) directionCombo.getSelectedItem();
        switch (selectedDirection) {
            case "↑": return 8;
            case "→": return 6;
            case "↓": return 2;
            case "←": return 4;
            default: return 2; // Default to down
        }
    }

    /**
     * Obtient le nom du personnage actuellement affiché
     * Dans une vraie implémentation, cela devrait venir de l'EventPage courante
     */
    private String getCurrentCharacterName() {
    String name = currentEventPage.getCharacterName();
    //System.out.println("DEBUG: getCurrentCharacterName() returns: " + name);
    return name;
    }
}