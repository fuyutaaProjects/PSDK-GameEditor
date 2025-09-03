package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

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
import psdk.EventEditor.model.EventCommand;

public class InsertCommandDialog extends JDialog {

    private JTextField searchField;
    private JList<EventCommand> commandListView;
    private DefaultListModel<EventCommand> commandListModel;
    private JScrollPane scrollPane;

    private EventCommand selectedCommandToInsert;

    private static final Map<String, Integer> ALL_MOVE_COMMAND_CODES = new LinkedHashMap<>();
    static {
        // First Column (Movement)
        ALL_MOVE_COMMAND_CODES.put("Move Down", 1);
        ALL_MOVE_COMMAND_CODES.put("Move Left", 2);
        ALL_MOVE_COMMAND_CODES.put("Move Right", 3);
        ALL_MOVE_COMMAND_CODES.put("Move Up", 4);
        ALL_MOVE_COMMAND_CODES.put("Move Lower Left", 5);
        ALL_MOVE_COMMAND_CODES.put("Move Lower Right", 6);
        ALL_MOVE_COMMAND_CODES.put("Move Upper Left", 7);
        ALL_MOVE_COMMAND_CODES.put("Move Upper Right", 8);
        ALL_MOVE_COMMAND_CODES.put("Move at Random", 9);
        ALL_MOVE_COMMAND_CODES.put("Move toward Player", 10);
        ALL_MOVE_COMMAND_CODES.put("Move away from Player", 11);
        ALL_MOVE_COMMAND_CODES.put("1 Step Forward", 12);
        ALL_MOVE_COMMAND_CODES.put("1 Step Backward", 13);
        ALL_MOVE_COMMAND_CODES.put("Jump...", 14);
        ALL_MOVE_COMMAND_CODES.put("Wait...", 15);

        // Second Column (Turns & Switches)
        ALL_MOVE_COMMAND_CODES.put("Turn Down", 16);
        ALL_MOVE_COMMAND_CODES.put("Turn Left", 17);
        ALL_MOVE_COMMAND_CODES.put("Turn Right", 18);
        ALL_MOVE_COMMAND_CODES.put("Turn Up", 19);
        ALL_MOVE_COMMAND_CODES.put("Turn 90° Right", 20);
        ALL_MOVE_COMMAND_CODES.put("Turn 90° Left", 21);
        ALL_MOVE_COMMAND_CODES.put("Turn 180°", 22);
        ALL_MOVE_COMMAND_CODES.put("Turn 90° Right or Left", 23);
        ALL_MOVE_COMMAND_CODES.put("Turn at Random", 24);
        ALL_MOVE_COMMAND_CODES.put("Turn toward player", 25);
        ALL_MOVE_COMMAND_CODES.put("Turn away from Player", 26);
        ALL_MOVE_COMMAND_CODES.put("Switch ON...", 27);
        ALL_MOVE_COMMAND_CODES.put("Switch OFF...", 28);
        ALL_MOVE_COMMAND_CODES.put("Change Speed...", 29);
        ALL_MOVE_COMMAND_CODES.put("Change Freq...", 30);

        // Third Column (Options & Graphics)
        ALL_MOVE_COMMAND_CODES.put("Move Animation ON", 31);
        ALL_MOVE_COMMAND_CODES.put("Move Animation OFF", 32);
        ALL_MOVE_COMMAND_CODES.put("Stop Animation ON", 33);
        ALL_MOVE_COMMAND_CODES.put("Stop Animation OFF", 34);
        ALL_MOVE_COMMAND_CODES.put("Direction Fix ON", 35);
        ALL_MOVE_COMMAND_CODES.put("Direction Fix OFF", 36);
        ALL_MOVE_COMMAND_CODES.put("Through ON", 37);
        ALL_MOVE_COMMAND_CODES.put("Through OFF", 38);
        ALL_MOVE_COMMAND_CODES.put("Always on Top ON", 39);
        ALL_MOVE_COMMAND_CODES.put("Always on Top OFF", 40);
        ALL_MOVE_COMMAND_CODES.put("Change Graphic...", 41);
        ALL_MOVE_COMMAND_CODES.put("Change Opacity...", 42);
        ALL_MOVE_COMMAND_CODES.put("Change Blending...", 43);
        ALL_MOVE_COMMAND_CODES.put("Play SE...", 44);
        ALL_MOVE_COMMAND_CODES.put("Script...", 45);
    }

    public InsertCommandDialog(Frame owner) {
        super(owner, "Insert Move Command", false);
        setUndecorated(true);
        setResizable(false);
        setFocusableWindowState(true);
        setAlwaysOnTop(true);

        initComponents();
        populateCommandList(ALL_MOVE_COMMAND_CODES);
        addKeyAndMouseListeners();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        searchField = new JTextField();
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            new EmptyBorder(5, 5, 5, 5)
        ));
        searchField.setPreferredSize(new Dimension(250, 30));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        panel.add(searchField, BorderLayout.NORTH);

        commandListModel = new DefaultListModel<>();
        commandListView = new JList<>(commandListModel);
        commandListView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commandListView.setLayoutOrientation(JList.VERTICAL);
        commandListView.setVisibleRowCount(10);
        commandListView.setCellRenderer(new EventCommandListCellRenderer());
        commandListView.setFocusable(true);
        commandListView.setFont(new Font("SansSerif", Font.PLAIN, 12));

        scrollPane = new JScrollPane(commandListView);
        scrollPane.setPreferredSize(new Dimension(250, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        JLabel hintLabel = new JLabel("<html><center><small>Arrow keys to navigate, Enter to select<br>Shift + / to close</small></center></html>");
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
        // Global Key Listener for the dialog
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_SLASH) {
                    // Shift + / to close the dialog without selection
                    selectedCommandToInsert = null;
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // ESC to close
                    selectedCommandToInsert = null;
                    dispose();
                }
            }
        });

        // Key Listener for Search Field
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (commandListModel.getSize() > 0) {
                        commandListView.requestFocusInWindow();
                        commandListView.setSelectedIndex(0);
                        commandListView.ensureIndexIsVisible(0);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (commandListModel.getSize() > 0) {
                        commandListView.requestFocusInWindow();
                        commandListView.setSelectedIndex(0);
                        commandListView.ensureIndexIsVisible(0);
                        selectAndClose(); // Try to select the first item if enter is pressed in search
                    }
                }
            }
        });

        // Key Listener for Command List
        commandListView.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    selectAndClose();
                } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (commandListView.getSelectedIndex() == 0) {
                        searchField.requestFocusInWindow();
                    }
                }
            }
        });

        // Mouse Listener for Command List (double-click to select)
        commandListView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    selectAndClose();
                }
            }
        });
    }

    private void filterCommandList() {
        String searchText = searchField.getText().toLowerCase();
        commandListModel.clear();

        for (Map.Entry<String, Integer> entry : ALL_MOVE_COMMAND_CODES.entrySet()) {
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
            selectedCommandToInsert = commandListView.getSelectedValue();
        }
        dispose();
    }

    public EventCommand getSelectedCommand() {
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
}