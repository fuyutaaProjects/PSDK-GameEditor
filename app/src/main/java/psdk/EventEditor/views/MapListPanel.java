package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import psdk.EventEditor.model.Editor;
import psdk.EventEditor.model.YmlGetter;

public class MapListPanel extends JPanel {

    // Color palette
    private static final Color BACKGROUND_PRIMARY = new Color(0x3a3843);
    private static final Color BACKGROUND_SECONDARY = new Color(0x28242c);
    private static final Color BACKGROUND_DARKER = new Color(0x1d1c22);
    private static final Color TEXT_COLOR = new Color(0xe0e0e0);

    public interface MapSelectionCallback {
        void onMapSelected(File ymlFile);
    }

    private Editor editor;
    private MapSelectionCallback callback;
    private String projectRootPath; 
    
    private JTextField searchField;
    private JPanel mapButtonsPanel;
    private List<File> allYmlFiles; // The YML files are the Map files.
    private int maxButtonWidth; // Store the maximum width needed

    public MapListPanel(Editor editor, MapSelectionCallback callback, String projectRootPath) {
        this.editor = editor;
        this.callback = callback;
        this.projectRootPath = projectRootPath;

        setLayout(new BorderLayout());
        
        // Apply dark theme styling
        setBackground(BACKGROUND_PRIMARY);
        
        // Create styled border
        TitledBorder titledBorder = BorderFactory.createTitledBorder("Maps");
        titledBorder.setTitleColor(TEXT_COLOR);
        titledBorder.setBorder(BorderFactory.createLineBorder(BACKGROUND_SECONDARY, 1));
        setBorder(titledBorder);

        // --- Styled search field ---
        searchField = new JTextField();
        searchField.setBackground(BACKGROUND_SECONDARY);
        searchField.setForeground(TEXT_COLOR);
        searchField.setCaretColor(TEXT_COLOR);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BACKGROUND_DARKER, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        searchField.setToolTipText("Rechercher une map par nom ou numéro...");
        add(searchField, BorderLayout.NORTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterMapButtons();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterMapButtons();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterMapButtons();
            }
        });

        mapButtonsPanel = new JPanel();
        mapButtonsPanel.setLayout(new BoxLayout(mapButtonsPanel, BoxLayout.Y_AXIS));
        mapButtonsPanel.setBackground(BACKGROUND_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(mapButtonsPanel);
        scrollPane.setBackground(BACKGROUND_PRIMARY);
        scrollPane.getViewport().setBackground(BACKGROUND_PRIMARY);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // Style scrollbars
        scrollPane.getVerticalScrollBar().setBackground(BACKGROUND_SECONDARY);
        scrollPane.getHorizontalScrollBar().setBackground(BACKGROUND_SECONDARY);
        
        add(scrollPane, BorderLayout.CENTER);

        allYmlFiles = editor.getYmlFiles(); 
        calculateMaxButtonWidth(); // Calculate the maximum width needed
        
        // Set minimum size for the entire panel to prevent shrinking
        Dimension minSize = new Dimension(maxButtonWidth + 30, 200); // +30 for scrollbar and padding
        setMinimumSize(minSize);
        setPreferredSize(minSize);
        
        loadMapButtons(mapButtonsPanel);
    }

    private void calculateMaxButtonWidth() {
        maxButtonWidth = 0;
        
        if (allYmlFiles != null) {
            for (File file : allYmlFiles) {
                String buttonText = getButtonTextForFile(file);
                
                // Create a temporary button to measure its preferred width
                JButton tempButton = new JButton(buttonText);
                int buttonWidth = tempButton.getPreferredSize().width;
                
                if (buttonWidth > maxButtonWidth) {
                    maxButtonWidth = buttonWidth;
                }
            }
        }
        
        // Add some padding to ensure buttons don't get clipped
        maxButtonWidth += 20;
    }

    private String getButtonTextForFile(File file) {
        String mapFileName = file.getName();
        String mapNumberStr = "";
        
        if (mapFileName.startsWith("Map") && mapFileName.length() >= 6) {
            mapNumberStr = mapFileName.substring(3, 6);
            if (!mapNumberStr.matches("\\d{3}")) {
                mapNumberStr = "???";
            }
        } else if (mapFileName.equalsIgnoreCase("MapInfos.rxdata.yml")) {
            mapNumberStr = "SYS"; 
        }

        String mapName = YmlGetter.getMapName(file, projectRootPath);

        if (mapName != null && !mapName.isEmpty()) {
            return mapNumberStr + " - " + mapName;
        } else {
            return mapNumberStr + " - " + mapFileName.replace(".rxdata.yml", "");
        }
    }

    private void loadMapButtons(JPanel panelToLoadInto) {
        panelToLoadInto.removeAll();
        
        List<File> filesToDisplay;
        String searchText = searchField.getText().toLowerCase();

        if (searchText.isEmpty()) {
            filesToDisplay = allYmlFiles;
        } else {
            // Filter map results corresponding to the search field input
            filesToDisplay = allYmlFiles.stream()
                .filter(file -> {
                    String mapFileName = file.getName();
                    String mapNumberStr = "";
                    if (mapFileName.startsWith("Map") && mapFileName.length() >= 6) {
                        mapNumberStr = mapFileName.substring(3, 6);
                    }
                    String mapName = YmlGetter.getMapName(file, projectRootPath);
                    if (mapName == null || mapName.isEmpty()) {
                        mapName = mapFileName.replace(".rxdata.yml", "");
                    }
                    return mapNumberStr.contains(searchText) || mapName.toLowerCase().contains(searchText);
                })
                .collect(Collectors.toList());
        }

        if (filesToDisplay != null) {
            for (File file : filesToDisplay) {
                String buttonText = getButtonTextForFile(file);
                
                JButton mapButton = createStyledButton(buttonText);
                mapButton.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Set both maximum and preferred width to the calculated maximum
                Dimension buttonSize = new Dimension(maxButtonWidth, mapButton.getMinimumSize().height);
                mapButton.setMaximumSize(buttonSize);
                mapButton.setPreferredSize(buttonSize);

                mapButton.addActionListener(e -> {
                    if (callback != null) {
                        callback.onMapSelected(file);
                    }
                });
                panelToLoadInto.add(mapButton);
            }
        }
        panelToLoadInto.revalidate();
        panelToLoadInto.repaint();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        
        // Base styling
        button.setBackground(BACKGROUND_SECONDARY);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BACKGROUND_DARKER, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
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

    private void filterMapButtons() {
        loadMapButtons(mapButtonsPanel);
    }
}