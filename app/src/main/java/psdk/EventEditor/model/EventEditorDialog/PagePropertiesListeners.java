package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import java.awt.*;

import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.ChangeGraphicDialog;
import psdk.EventEditor.model.EventEditorDialog.PageProperties.ConditionComponents;
import psdk.EventEditor.model.EventEditorDialog.PageProperties.GraphicComponents;
import psdk.EventEditor.model.EventEditorDialog.PageProperties.MovementComponents;
import psdk.EventEditor.model.EventEditorDialog.PageProperties.OptionComponents;

public class PagePropertiesListeners {
    
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
    
    public static void setupGraphicListeners(GraphicComponents components, PagePropertiesPanel parentPanel) {
        components.characterButton.addActionListener(e -> {
            EventPage currentPage = parentPanel.getCurrentEventPage();
            if (currentPage == null) {
                JOptionPane.showMessageDialog(parentPanel, "No event page selected", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get current values from the EventPage
            String currentGraphicName = currentPage.getCharacterName() != null ? currentPage.getCharacterName() : "";
            int currentEncodedX = currentPage.getCharacterIndex(); // This should be the RPG Maker encoded X value
            int currentY = getRowFromDirection(currentPage.getDirection()); // Convert direction to row (0-3)
            
            System.out.println("DEBUG: Opening ChangeGraphicDialog with current values:");
            System.out.println("  Graphic Name: " + currentGraphicName);
            System.out.println("  Character Index (Encoded X): " + currentEncodedX);
            System.out.println("  Direction -> Row (Y): " + currentPage.getDirection() + " -> " + currentY);
            
            // Find parent window for modal dialog
            Window parentWindow = SwingUtilities.getWindowAncestor(parentPanel);
            Dialog parentDialog = (parentWindow instanceof Dialog) ? (Dialog) parentWindow : null;
            
            // Open the ChangeGraphicDialog
            ChangeGraphicDialog dialog = new ChangeGraphicDialog(
                parentDialog, 
                currentGraphicName, 
                currentEncodedX, 
                currentY, 
                0
            );
            
            dialog.setVisible(true);
            
            // If user clicked OK, update the EventPage and refresh display
            if (dialog.isOkPressed()) {
                String newGraphicName = dialog.getSelectedGraphicName();
                int newCharacterIndex = dialog.getCharacterIndex();
                int newDirection = dialog.getSelectedDirection();
                int newPattern = dialog.getSelectedPattern();

                System.out.println("DEBUG: ChangeGraphicDialog returned values:");
                System.out.println("  New Graphic Name: " + newGraphicName);
                System.out.println("  New Character Index (Encoded X): " + newCharacterIndex);
                System.out.println("  New Direction: " + newDirection);
                System.out.println("  New Pattern: " + newPattern);

                // Update the EventPage with new values
                currentPage.setCharacterName(newGraphicName);
                currentPage.setCharacterIndex(newCharacterIndex);
                currentPage.setDirection(newDirection);
                currentPage.setPattern(newPattern);
                
                // Let PagePropertiesPanel handle the update and save
                parentPanel.updatePageProperties(currentPage);
                
                System.out.println("DEBUG: EventPage updated, display refresh handled by PagePropertiesPanel.");
            } else {
                System.out.println("DEBUG: Dialog closed with Cancel, no changes made.");
            }
        });
        
        components.directionCombo.addActionListener(e -> {
            // Direction change logic - could update the EventPage direction here
            // This would need to be implemented based on your requirements
        });
    }
    
    /**
     * Convert RPG Maker XP direction value to spritesheet row (0-3)
     * RPG Maker XP directions: 2=Down, 4=Left, 6=Right, 8=Up
     * Spritesheet rows: 0=Down, 1=Left, 2=Right, 3=Up
     */
    private static int getRowFromDirection(int direction) {
        switch (direction) {
            case 2: return 0; // Down
            case 4: return 1; // Left  
            case 6: return 2; // Right
            case 8: return 3; // Up
            default: return 0; // Default to down
        }
    }
    
    /**
     * Convert spritesheet row (0-3) to RPG Maker XP direction value
     * Spritesheet rows: 0=Down, 1=Left, 2=Right, 3=Up  
     * RPG Maker XP directions: 2=Down, 4=Left, 6=Right, 8=Up
     */
    private static int getDirectionFromRow(int row) {
        switch (row) {
            case 0: return 2; // Down
            case 1: return 4; // Left
            case 2: return 6; // Right
            case 3: return 8; // Up
            default: return 2; // Default to down
        }
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