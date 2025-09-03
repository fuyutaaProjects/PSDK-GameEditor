package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventCommand;

public class MoveCommandQuickInsertDialog extends JDialog {

    private JTextField searchField;
    private JList<EventCommand> commandListView;
    private DefaultListModel<EventCommand> commandListModel;
    private JScrollPane scrollPane;

    private Entry<String, Integer> selectedCommandToInsert;

    // This map will be passed from SetMoveRouteEditorDialog
    private final Map<String, Integer> allMoveCommandCodes;

    public MoveCommandQuickInsertDialog(Dialog owner, Map<String, Integer> moveCommandCodes) {
        // Use 'Dialog' as owner, and make it modal (true) so it blocks the parent
        super(owner, "Quick Insert Command", true); 
        setUndecorated(true);
        setResizable(false);
        setFocusableWindowState(true);
        setAlwaysOnTop(true);
        this.allMoveCommandCodes = moveCommandCodes;

        initComponents();
        populateCommandList(allMoveCommandCodes);
        addKeyAndMouseListeners();
        pack();
        setLocation(owner.getX() + owner.getWidth() / 2 - getWidth() / 2, 
                    owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        searchField.setPreferredSize(new Dimension(280, 30));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(searchField, BorderLayout.NORTH);

        commandListModel = new DefaultListModel<>();
        commandListView = new JList<>(commandListModel);
        commandListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandListView.setLayoutOrientation(JList.VERTICAL);
        commandListView.setVisibleRowCount(15);
        commandListView.setCellRenderer(new EventCommandListCellRenderer());
        commandListView.setFocusable(true);
        commandListView.setFont(new Font("SansSerif", Font.PLAIN, 12));

        scrollPane = new JScrollPane(commandListView);
        scrollPane.setPreferredSize(new Dimension(280, 300));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel("<html><center><small>Flèches pour naviguer, Entrée pour sélectionner<br>Shift + : pour fermer</small></center></html>");
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        hintLabel.setBorder(new EmptyBorder(5, 0, 5, 0));
        panel.add(hintLabel, BorderLayout.SOUTH);

        add(panel);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterCommandList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterCommandList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterCommandList();
            }
        });
    }

    private void addKeyAndMouseListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Shift + : to close the dialog without selection
                if (e.getKeyCode() == KeyEvent.VK_COLON && e.isShiftDown()) {
                    selectedCommandToInsert = null;
                    dispose();
                    e.consume(); // Consume to prevent further processing
                } 
                // ESC to close without selection
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    selectedCommandToInsert = null;
                    dispose();
                    e.consume();
                }
            }
        });

        // Key Listener for Search Field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (commandListModel.getSize() > 0) {
                        commandListView.requestFocusInWindow(); // Move focus to list
                        commandListView.setSelectedIndex(0);
                        commandListView.ensureIndexIsVisible(0);
                    }
                    e.consume(); // Consume to prevent default behavior
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (commandListModel.getSize() > 0) {
                        selectAndClose(); // Select the first item if Enter is pressed in search
                    }
                    e.consume();
                }
            }
        });

        // Key Listener for Command List
        commandListView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectAndClose(); // Select the highlighted item
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    // Only transfer focus to search field if at the very top of the list
                    if (commandListView.getSelectedIndex() == 0) {
                        searchField.requestFocusInWindow(); // Move focus to search bar
                        e.consume(); // Consume only if focus is transferred
                    }
                    // Otherwise, let JList handle the UP movement naturally (do not consume)
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    // Let JList handle the DOWN movement naturally (do not consume)
                    // No custom logic needed here unless we want to move focus to a 'next' element after the list
                }
            }
        });

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                commandListView.clearSelection();
            }
        });
    }

    private void filterCommandList() {
        String searchText = searchField.getText().toLowerCase();
        commandListModel.clear();

        for (Map.Entry<String, Integer> entry : allMoveCommandCodes.entrySet()) {
            if (entry.getKey().toLowerCase().contains(searchText)) {
                // Create a dummy EventCommand for display in the list
                // The actual parameters will be handled in SetMoveRouteEditorDialog when inserting
                commandListModel.addElement(new EventCommand(509, entry.getKey(), new JSONArray()));
            }
        }
        if (commandListModel.getSize() > 0) {
            commandListView.setSelectedIndex(0); // Select the first item after filtering
            commandListView.ensureIndexIsVisible(0);
        }
    }

    private void populateCommandList(Map<String, Integer> commandsMap) {
        commandListModel.clear();
        for (Map.Entry<String, Integer> entry : commandsMap.entrySet()) {
            // Create a dummy EventCommand for display
            commandListModel.addElement(new EventCommand(509, entry.getKey(), new JSONArray()));
        }
        if (commandListModel.getSize() > 0) {
            commandListView.setSelectedIndex(0);
            commandListView.ensureIndexIsVisible(0);
        }
    }

    private void selectAndClose() {
        if (commandListView.getSelectedIndex() != -1) {
            // Get the selected command's display name
            String commandName = commandListView.getSelectedValue().getIndent(); // Using getIndent as it stores the name
            // Find the corresponding code from the original map
            Integer commandCode = allMoveCommandCodes.get(commandName);
            if (commandCode != null) {
                // Create an Entry to return both name and code
                selectedCommandToInsert = Map.entry(commandName, commandCode);
            }
        }
        dispose();
    }

    /**
     * Returns the selected command name and code as a Map.Entry.
     * Returns null if no command was selected or dialog was cancelled.
     */
    public Entry<String, Integer> getSelectedCommand() {
        return selectedCommandToInsert;
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b) {
            // Request focus on the search field when the dialog becomes visible
            SwingUtilities.invokeLater(() -> searchField.requestFocusInWindow());
        }
    }

    /**
     * Static helper method to get default parameters for a given move command code.
     * This avoids prompting the user for basic parameters during quick insert.
     * @param moveCode The code of the move command.
     * @return A JSONArray containing default parameters for the command.
     */
    public static JSONArray getDefaultParametersForCommand(int moveCode) {
        JSONArray params = new JSONArray();
        switch (moveCode) {
            case 14: // Jump... (x, y offsets)
                params.put(0).put(0);
                break;
            case 15: // Wait... (frames)
                params.put(0);
                break;
            case 27: // Switch ON... (switch ID)
            case 28: // Switch OFF... (switch ID)
                params.put(1); // Default to switch ID 1
                break;
            case 29: // Change Speed... (speed 1-6)
            case 30: // Change Freq... (frequency 1-6)
                params.put(3); // Default speed/freq 3
                break;
            case 41: // Change Graphic... (char name, index, direction, pattern)
                params.put("").put(0).put(2).put(0); // Default to empty char, index 0, dir Down, pattern 0
                break;
            case 42: // Change Opacity... (opacity 0-255)
                params.put(255); // Default to full opacity
                break;
            case 43: // Change Blending... (mode 0-2)
                params.put(0); // Default to Normal blending
                break;
            case 44: // Play SE... (name, volume, pitch)
                JSONObject seJson = new JSONObject();
                try {
                    seJson.put("name", "");
                    seJson.put("volume", 100);
                    seJson.put("pitch", 100);
                } catch (JSONException e) {
                    System.err.println("Error creating default SE JSON: " + e.getMessage());
                }
                params.put(seJson);
                break;
            case 45: // Script... (script string)
                params.put(""); // Default to empty script
                break;
            default:
                // No parameters needed for other commands (e.g., simple moves, turns)
                break;
        }
        return params;
    }
}
