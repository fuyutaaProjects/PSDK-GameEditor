package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import psdk.EventEditor.ConfigManager;
import psdk.EventEditor.Core;

public class MainMenu extends JPanel {

    // Color palette
    private static final Color BACKGROUND_PRIMARY = new Color(0x3a3843);
    private static final Color BACKGROUND_SECONDARY = new Color(0x28242c);
    private static final Color BACKGROUND_DARKER = new Color(0x1d1c22);
    private static final Color TEXT_COLOR = new Color(0xe0e0e0);
    private static final Color SUCCESS_COLOR = new Color(0x4caf50);
    private static final Color ERROR_COLOR = new Color(0xf44336);

    private JTextField projectPathField;
    private JLabel statusLabel;

    public MainMenu(String initialProjectPath) {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_PRIMARY);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_PRIMARY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel pathLabel = new JLabel("Chemin du projet RPG Maker XP :");
        pathLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        centerPanel.add(pathLabel, gbc);

        projectPathField = new JTextField(40);
        projectPathField.setBackground(BACKGROUND_SECONDARY);
        projectPathField.setForeground(TEXT_COLOR);
        projectPathField.setCaretColor(TEXT_COLOR);
        projectPathField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BACKGROUND_DARKER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        if (initialProjectPath != null) {
            projectPathField.setText(initialProjectPath);
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        centerPanel.add(projectPathField, gbc);

        JButton applyPathButton = createStyledButton("Apply path");
        applyPathButton.addActionListener(e -> applyProjectPath());
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        centerPanel.add(applyPathButton, gbc);

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(statusLabel, gbc);

        JButton openEditor = createStyledButton("Open Editor");
        openEditor.addActionListener(e -> openEditor());
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        centerPanel.add(openEditor, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void applyProjectPath() {
        String path = projectPathField.getText().trim();
        
        if (path.isEmpty()) {
            showError("Veuillez saisir un chemin de projet.");
            return;
        }
        
        if (isValidRpgMakerProject(path)) {
            ConfigManager.saveProjectPath(path);
            showSuccess("Chemin du projet appliqué avec succès !");
        } else {
            showError("Le chemin spécifié n'est pas un projet RPG Maker XP valide (dossiers Data ou Graphics manquants).");
        }
    }
    
    private void openEditor() {
        String currentPath = ConfigManager.loadProjectPath();
        
        if (currentPath == null || currentPath.trim().isEmpty()) {
            showError("Veuillez d'abord appliquer un chemin de projet valide.");
            return;
        }
        
        if (!isValidRpgMakerProject(currentPath)) {
            showError("Le projet configuré n'est plus valide. Veuillez configurer un nouveau chemin.");
            return;
        }
        
        Core.showEditor();
    }
    
    private boolean isValidRpgMakerProject(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        File projectDir = new File(path);
        File dataDir = new File(projectDir, "Data");
        File graphicsDir = new File(projectDir, "Graphics");
        
        return projectDir.exists() && projectDir.isDirectory() 
            && dataDir.exists() && dataDir.isDirectory() 
            && graphicsDir.exists() && graphicsDir.isDirectory();
    }
    
    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(SUCCESS_COLOR);
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(ERROR_COLOR);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        
        // Base styling
        button.setBackground(BACKGROUND_SECONDARY);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BACKGROUND_DARKER, 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
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
}