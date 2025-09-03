package psdk.EventEditor.views;

import psdk.EventEditor.model.Editor;
import libs.json.JSONObject;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TileGridToolbar extends JToolBar {

    // Color palette
    private static final Color BACKGROUND_PRIMARY = new Color(0x3a3843);
    private static final Color BACKGROUND_SECONDARY = new Color(0x28242c);
    private static final Color BACKGROUND_DARKER = new Color(0x1d1c22);
    private static final Color TEXT_COLOR = new Color(0xe0e0e0);

    private Editor editor;
    private int currentMapId;
    private JSONObject currentMapDataJson;
    private GridOverlayVisualizer gridVisualizer; 

    public TileGridToolbar(Editor editor, int initialMapId, JSONObject initialMapDataJson, GridOverlayVisualizer gridVisualizer) {
        this.editor = editor;
        this.currentMapId = initialMapId;
        this.currentMapDataJson = initialMapDataJson; 
        this.gridVisualizer = gridVisualizer;

        // Apply dark theme styling
        setBackground(BACKGROUND_PRIMARY);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BACKGROUND_DARKER, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        setFloatable(false);

        initComponents();
    }

    public void setCurrentMapData(int mapId, JSONObject mapDataJson) {
        this.currentMapId = mapId;
        this.currentMapDataJson = mapDataJson;
        if (editor != null) {
            editor.setCurrentMapDataJson(mapDataJson);
        }
    }

    private void initComponents() {
        JButton saveToYmlButton = createStyledButton("Save Map to YAML");
        saveToYmlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editor != null && currentMapDataJson != null && currentMapId != 0) { // Check mapId too
                    boolean success = editor.saveMapDataToYml(currentMapId, currentMapDataJson);
                    if (success) {
                        showStyledDialog("Map saved to YAML successfully!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        showStyledDialog("Failed to save map to YAML. Check console for details.", "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    showStyledDialog(
                        "Map data or Editor instance not available. Make sure a map is loaded and its data is passed to the toolbar.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        add(saveToYmlButton);

        JButton toggleGridButton = createStyledButton("Toggle Grid");
        toggleGridButton.addActionListener(e -> {
            if (this.gridVisualizer != null) {
                this.gridVisualizer.toggleGridVisibility();
            } else {
                showStyledDialog("Grid Visualizer is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        add(toggleGridButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        
        // Base styling
        button.setBackground(BACKGROUND_SECONDARY);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BACKGROUND_DARKER, 1),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(BACKGROUND_DARKER);
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(BACKGROUND_SECONDARY);
            }
        });
        
        return button;
    }

    private void showStyledDialog(String message, String title, int messageType) {
        // Create a styled option pane
        JOptionPane optionPane = new JOptionPane(message, messageType);
        optionPane.setBackground(BACKGROUND_PRIMARY);
        optionPane.setForeground(TEXT_COLOR);
        
        // Show the dialog
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
}