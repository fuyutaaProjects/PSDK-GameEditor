package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.views.EventCommandListCellRenderer;
import psdk.EventEditor.views.MoveCommandQuickInsertDialog;

public class SetMoveRouteEditorDialog extends JDialog {

    private JSONObject originalMoveRouteParams;
    private JSONArray modifiedMoveRouteList;
    private JSONArray modified209Parameters;

    private JComboBox<String> targetComboBox;
    private JCheckBox repeatCheckBox;
    private JCheckBox skipIfCannotMoveCheckBox;
    private JCheckBox waitForCompletionCheckBox;

    private JList<EventCommand> moveCommandList;
    private DefaultListModel<EventCommand> moveCommandListModel;
    private EventCommandListCellRenderer moveCommandCellRenderer;

    private boolean commandModified = false;

    private Map<Integer, String> eventTargetMap;

    public static final Map<String, Integer> MOVE_COMMAND_CODES = new LinkedHashMap<>();
    public static final Map<Integer, String> MOVE_COMMAND_NAMES_BY_CODE = new LinkedHashMap<>();
    public static final Map<Integer, Boolean> MOVE_COMMAND_EDITABILITY = new LinkedHashMap<>();

    static {
        String[] col1Names = {
            "Move Down", "Move Left", "Move Right", "Move Up",
            "Move Lower Left", "Move Lower Right", "Move Upper Left", "Move Upper Right",
            "Move at Random", "Move toward Player", "Move away from Player",
            "1 Step Forward", "1 Step Backward", "Jump...", "Wait..."
        };
        int[] col1Codes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

        String[] col2Names = {
            "Turn Down", "Turn Left", "Turn Right", "Turn Up",
            "Turn 90° Right", "Turn 90° Left", "Turn 180°", "Turn 90° Right or Left",
            "Turn at Random", "Turn toward Player", "Turn away from Player",
            "Switch ON...", "Switch OFF...", "Change Speed...", "Change Freq..."
        };
        int[] col2Codes = {16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};

        String[] col3Names = {
            "Move Animation ON", "Move Animation OFF", "Stop Animation ON", "Stop Animation OFF",
            "Direction Fix ON", "Direction Fix OFF", "Through ON", "Through OFF",
            "Always on Top ON", "Always on Top OFF", "Change Graphic...",
            "Change Opacity...", "Change Blending...", "Play SE...", "Script..."
        };
        int[] col3Codes = {31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45};

        int maxRows = Math.max(col1Names.length, Math.max(col2Names.length, col3Names.length));

        for (int i = 0; i < maxRows; i++) {
            if (i < col1Names.length) {
                MOVE_COMMAND_CODES.put(col1Names[i], col1Codes[i]);
                MOVE_COMMAND_NAMES_BY_CODE.put(col1Codes[i], col1Names[i]);
            }
            if (i < col2Names.length) {
                MOVE_COMMAND_CODES.put(col2Names[i], col2Codes[i]);
                MOVE_COMMAND_NAMES_BY_CODE.put(col2Codes[i], col2Names[i]);
            }
            if (i < col3Names.length) {
                MOVE_COMMAND_CODES.put(col3Names[i], col3Codes[i]);
                MOVE_COMMAND_NAMES_BY_CODE.put(col3Codes[i], col3Names[i]);
            }
        }
        
        for (Integer code : MOVE_COMMAND_NAMES_BY_CODE.keySet()) {
            MOVE_COMMAND_EDITABILITY.put(code, false);
        }

        MOVE_COMMAND_EDITABILITY.put(14, true);  // Jump...
        MOVE_COMMAND_EDITABILITY.put(15, true);  // Wait...
        MOVE_COMMAND_EDITABILITY.put(27, true);  // Switch ON...
        MOVE_COMMAND_EDITABILITY.put(28, true);  // Switch OFF...
        MOVE_COMMAND_EDITABILITY.put(29, true);  // Change Speed...
        MOVE_COMMAND_EDITABILITY.put(30, true);  // Change Freq...
        MOVE_COMMAND_EDITABILITY.put(41, true);  // Change Graphic...
        MOVE_COMMAND_EDITABILITY.put(42, true);  // Change Opacity...
        MOVE_COMMAND_EDITABILITY.put(43, true);  // Change Blending...
        MOVE_COMMAND_EDITABILITY.put(44, true);  // Play SE...
        MOVE_COMMAND_EDITABILITY.put(45, true);  // Script...
    }

    public SetMoveRouteEditorDialog(Dialog owner, EventCommand setMoveRouteCommand) {
        super(owner, "Set Move Route", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 680);
        setLocationRelativeTo(owner);

        try {
            this.modified209Parameters = new JSONArray(setMoveRouteCommand.getParameters().toString());
            this.originalMoveRouteParams = this.modified209Parameters.optJSONObject(1);
            
            this.modifiedMoveRouteList = new JSONArray();
            JSONArray originalList = this.originalMoveRouteParams.getJSONArray("list");
            for (int i = 0; i < originalList.length(); i++) {
                JSONObject currentCmd = originalList.getJSONObject(i);
                if (currentCmd.optInt("code", -1) != 0) { 
                    this.modifiedMoveRouteList.put(new JSONObject(currentCmd.toString()));
                }
            }
        } catch (JSONException e) {
            System.err.println("Error deep copying move route parameters: " + e.getMessage());
            this.modified209Parameters = setMoveRouteCommand.getParameters();
            this.originalMoveRouteParams = this.modified209Parameters.optJSONObject(1);
            this.modifiedMoveRouteList = (this.originalMoveRouteParams != null) ? this.originalMoveRouteParams.optJSONArray("list") : new JSONArray();
            if (this.modifiedMoveRouteList == null) this.modifiedMoveRouteList = new JSONArray();
            JSONArray temp = new JSONArray();
            for(int i = 0; i < this.modifiedMoveRouteList.length(); i++) {
                try {
                    JSONObject currentCmd = this.modifiedMoveRouteList.getJSONObject(i);
                    if (currentCmd.optInt("code", -1) != 0) {
                        temp.put(currentCmd);
                    }
                } catch (JSONException ex) {
                    System.err.println("Error filtering existing list during fallback: " + ex.getMessage());
                }
            }
            this.modifiedMoveRouteList = temp;
        }

        initializeEventTargetMap();
        initComponents();
        loadMoveRouteData();
        setupKeyBindings();
    }

    private void initializeEventTargetMap() {
        eventTargetMap = new HashMap<>();
        eventTargetMap.put(-1, "Player");
        eventTargetMap.put(0, "This Event");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcTop = new GridBagConstraints();
        gbcTop.insets = new Insets(5, 5, 5, 5);
        gbcTop.anchor = GridBagConstraints.WEST;
        gbcTop.fill = GridBagConstraints.HORIZONTAL;

        gbcTop.gridx = 0;
        gbcTop.gridy = 0;
        topPanel.add(new JLabel("Target:"), gbcTop);
        gbcTop.gridx = 1;
        gbcTop.gridy = 0;
        targetComboBox = new JComboBox<>();
        targetComboBox.addItem("Player");
        targetComboBox.addItem("This Event");
        for(Integer id : eventTargetMap.keySet()){
            if(id != 0 && id != -1){
                targetComboBox.addItem(eventTargetMap.get(id));
            }
        }
        topPanel.add(targetComboBox, gbcTop);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerContainerPanel = new JPanel(new BorderLayout(10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.20);
        splitPane.setDividerLocation(0.20);

        JPanel moveCommandListPanel = new JPanel(new BorderLayout());
        moveCommandListPanel.setBorder(BorderFactory.createTitledBorder("Current Move Commands"));

        moveCommandListModel = new DefaultListModel<>();
        moveCommandList = new JList<>(moveCommandListModel);
        moveCommandCellRenderer = new EventCommandListCellRenderer();
        moveCommandList.setCellRenderer(moveCommandCellRenderer);
        moveCommandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        moveCommandList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int index = moveCommandList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        EventCommand selectedMoveCommand = moveCommandListModel.getElementAt(index);
                        editInnerMoveCommand(selectedMoveCommand, index);
                    }
                }
            }
        });
        
        moveCommandList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    deleteInnerMoveCommand();
                } 
                else if (e.getKeyCode() == KeyEvent.VK_COLON && e.isShiftDown()) {
                    showQuickInsertDialog();
                    e.consume();
                }
            }
        });

        JScrollPane scrollPaneList = new JScrollPane(moveCommandList);
        moveCommandListPanel.add(scrollPaneList, BorderLayout.CENTER);

        JPanel moveCommandButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton editMoveButton = new JButton("Edit...");
        JButton moveMoveUpButton = new JButton("Move Up");
        JButton moveMoveDownButton = new JButton("Move Down");

        editMoveButton.addActionListener(e -> {
            int selectedIndex = moveCommandList.getSelectedIndex();
            if (selectedIndex != -1) {
                EventCommand selectedMoveCommand = moveCommandListModel.getElementAt(selectedIndex);
                editInnerMoveCommand(selectedMoveCommand, selectedIndex);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a move command to edit.", "No Command Selected", JOptionPane.WARNING_MESSAGE);
            }
        });
        moveMoveUpButton.addActionListener(e -> moveInnerMoveCommandUp());
        moveMoveDownButton.addActionListener(e -> moveInnerMoveCommandDown());

        moveCommandButtonPanel.add(editMoveButton);
        moveCommandButtonPanel.add(moveMoveUpButton);
        moveCommandButtonPanel.add(moveMoveDownButton);
        moveCommandListPanel.add(moveCommandButtonPanel, BorderLayout.SOUTH);

        splitPane.setLeftComponent(moveCommandListPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Available Move Commands"));

        JPanel buttonsGridPanel = new JPanel(new GridBagLayout());
        createMoveCommandButtons(buttonsGridPanel);

        rightPanel.add(buttonsGridPanel, BorderLayout.CENTER);
        splitPane.setRightComponent(rightPanel);

        centerContainerPanel.add(splitPane, BorderLayout.CENTER);

        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        repeatCheckBox = new JCheckBox("Repeat Action");
        skipIfCannotMoveCheckBox = new JCheckBox("Ignore If Can't Move");

        optionsPanel.add(repeatCheckBox);
        optionsPanel.add(skipIfCannotMoveCheckBox);

        waitForCompletionCheckBox = new JCheckBox("Wait for Completion (Hidden)");
        waitForCompletionCheckBox.setVisible(false);

        centerContainerPanel.add(optionsPanel, BorderLayout.SOUTH);

        mainPanel.add(centerContainerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            saveChanges();
            commandModified = true;
            dispose();
        });
        cancelButton.addActionListener(e -> {
            commandModified = false;
            dispose();
        });

        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupKeyBindings() {
        JRootPane rootPane = this.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();

        // Shift + Enter saves and closes the window
        KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(shiftEnter, "saveAndClose");
        actionMap.put("saveAndClose", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveChanges();
                commandModified = true;
                dispose();
            }
        });
    }

    private void showQuickInsertDialog() {
        MoveCommandQuickInsertDialog quickInsertDialog = new MoveCommandQuickInsertDialog(this, MOVE_COMMAND_CODES);
        quickInsertDialog.setVisible(true);

        Map.Entry<String, Integer> selectedCommandData = quickInsertDialog.getSelectedCommand();
        if (selectedCommandData != null) {
            int moveCode = selectedCommandData.getValue();
            String commandName = selectedCommandData.getKey();
            
            JSONArray defaultParams = MoveCommandQuickInsertDialog.getDefaultParametersForCommand(moveCode);

            insertCommandInternal(moveCode, commandName, defaultParams);
        }
    }


    private void createMoveCommandButtons(JPanel panel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        int col = 0;
        int row = 0;
        final int COLUMNS_PER_ROW = 3;

        for (Map.Entry<String, Integer> entry : MOVE_COMMAND_CODES.entrySet()) {
            String commandName = entry.getKey();
            int currentMoveCode = entry.getValue();

            JButton button = new JButton(commandName);
            button.addActionListener(e -> {
                if (currentMoveCode == 41) {
                    String defaultGraphicName = "";
                    int defaultCol = 0; // UI coordinate (0-3)
                    int defaultRow = 0; // UI coordinate (0-3)  
                    int defaultHue = 0;

                    // encoding defaultCol (0) to RMXP : (0 * 2) + 2 = 2
                    ChangeGraphicDialog graphicDialog = new ChangeGraphicDialog(this, defaultGraphicName, 
                        (defaultCol * 2) + 2, defaultRow, defaultHue);
                    graphicDialog.setVisible(true);

                    if (graphicDialog.isOkPressed()) {
                        String newGraphicName = graphicDialog.getSelectedGraphicName();
                        int newEncodedX = graphicDialog.getSelectedColForRpgMaker();
                        int newRow = graphicDialog.getSelectedRow();
                        int newHue = graphicDialog.getHueValue();

                        JSONArray newParams = new JSONArray();
                        newParams.put(newGraphicName); // index 0
                        newParams.put(newHue);         // index 1  
                        newParams.put(newEncodedX);    // index 2
                        newParams.put(newRow);         // index 3

                        insertCommandInternal(currentMoveCode, commandName, newParams);
                    }
                } else {
                    insertCommandFromButton(currentMoveCode, commandName);
                }
            });

            gbc.gridx = col;
            gbc.gridy = row;
            panel.add(button, gbc);

            col++;
            if (col >= COLUMNS_PER_ROW) {
                col = 0;
                row++;
            }
        }
    }

    private void insertCommandFromButton(int moveCode, String commandName) {
        JSONArray moveParams = new JSONArray(); 

        switch (commandName) {
            case "Jump...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter X,Y offsets for Jump (e.g., 10,0):");
                    if (input != null && !input.isEmpty()) {
                        String[] parts = input.split(",");
                        if (parts.length == 2) {
                            moveParams.put(Integer.parseInt(parts[0].trim()));
                            moveParams.put(Integer.parseInt(parts[1].trim()));
                        }
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid X,Y format. Using default [0,0].", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(0).put(0); 
                }
                break;
            case "Wait...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter wait frames (e.g., 4):");
                    if (input != null && !input.isEmpty()) {
                        moveParams.put(Integer.parseInt(input.trim()));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid frame number. Using default [0].", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(0); 
                }
                break;
            case "Switch ON...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter Switch ID to turn ON (e.g., 1):");
                    if (input != null && !input.isEmpty()) {
                        moveParams.put(Integer.parseInt(input.trim()));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Switch ID. Using default [1].", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(1); 
                }
                break;
            case "Switch OFF...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter Switch ID to turn OFF (e.g., 1):");
                    if (input != null && !input.isEmpty()) {
                        moveParams.put(Integer.parseInt(input.trim()));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Switch ID. Using default [1].", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(1); 
                }
                break;
            case "Change Speed...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter Speed (1-6):");
                    if (input != null && !input.isEmpty()) {
                        moveParams.put(Integer.parseInt(input.trim()));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Speed. Using default [3].", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(3); 
                }
                break;
            case "Change Freq...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter Frequency (1-6):");
                    if (input != null && !input.isEmpty()) {
                        moveParams.put(Integer.parseInt(input.trim()));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Frequency. Using default [3].", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(3); 
                }
                break;
            case "Change Opacity...":
                try {
                    String input = JOptionPane.showInputDialog(this, "Enter Opacity (0-255):");
                    if (input != null && !input.isEmpty()) {
                        moveParams.put(Integer.parseInt(input.trim()));
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid Opacity. Using default (255).", "Error", JOptionPane.ERROR_MESSAGE);
                    moveParams.put(255);
                }
                break;
            case "Change Blending...":
                String[] blendingOptions = {"Normal", "Add", "Subtract"};
                String selectedBlending = (String) JOptionPane.showInputDialog(
                    this,
                    "Select Blending Mode:",
                    "Change Blending",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    blendingOptions,
                    blendingOptions[0]
                );
                if (selectedBlending != null) {
                    int blendingMode = 0;
                    if (selectedBlending.equals("Add")) blendingMode = 1;
                    else if (selectedBlending.equals("Subtract")) blendingMode = 2;
                    moveParams.put(blendingMode);
                } else {
                    moveParams.put(0);
                }
                break;
            case "Play SE...":
                String seName = JOptionPane.showInputDialog(this, "Enter SE Name (e.g., 'Absorb'):");
                if (seName == null) seName = "";
                try {
                    String volumeStr = JOptionPane.showInputDialog(this, "Enter Volume (0-100, default 100):");
                    int volume = (volumeStr != null && !volumeStr.isEmpty()) ? Integer.parseInt(volumeStr) : 100;
                    String pitchStr = JOptionPane.showInputDialog(this, "Enter Pitch (50-150, default 100):");
                    int pitch = (pitchStr != null && !pitchStr.isEmpty()) ? Integer.parseInt(pitchStr) : 100;

                    JSONObject seJson = new JSONObject();
                    seJson.put("name", seName);
                    seJson.put("volume", volume);
                    seJson.put("pitch", pitch);
                    moveParams.put(seJson);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Invalid number for Volume/Pitch. Using defaults.", "Error", JOptionPane.ERROR_MESSAGE);
                    JSONObject seJson = new JSONObject();
                    seJson.put("name", seName);
                    seJson.put("volume", 100);
                    seJson.put("pitch", 100);
                    moveParams.put(seJson);
                }
                break;
            case "Script...":
                String script = JOptionPane.showInputDialog(this, "Enter Script Line:");
                moveParams.put(script != null ? script : "");
                break;
            default:
                break;
        }

        insertCommandInternal(moveCode, commandName, moveParams);
    }

    private void insertCommandInternal(int moveCode, String commandName, JSONArray parameters) {
        JSONObject newMoveCmdJson = new JSONObject();
        try {
            newMoveCmdJson.put("code", moveCode);
            newMoveCmdJson.put("parameters", parameters);
            
            int selectedIndex = moveCommandList.getSelectedIndex();
            int insertPosition = (selectedIndex != -1) ? selectedIndex + 1 : modifiedMoveRouteList.length();

            modifiedMoveRouteList = insertNewJSONObjectAt(modifiedMoveRouteList, newMoveCmdJson, insertPosition);
            refreshMoveCommandListFromJSON(); 
            
            moveCommandList.setSelectedIndex(insertPosition);
            moveCommandList.ensureIndexIsVisible(insertPosition);
            moveCommandList.requestFocusInWindow();

        } catch (JSONException e) {
            JOptionPane.showMessageDialog(this, "Error creating new move command JSON: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error creating new move command JSON: " + e.getMessage());
        }
    }


    private void loadMoveRouteData() {
        try {
            int targetId = modified209Parameters.getInt(0);
            String targetName = eventTargetMap.get(targetId);
            if (targetName != null) {
                targetComboBox.setSelectedItem(targetName);
            } else {
                String customTarget = "Event " + targetId;
                boolean found = false;
                for (int i = 0; i < targetComboBox.getItemCount(); i++) {
                    if (targetComboBox.getItemAt(i).equals(customTarget)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    targetComboBox.addItem(customTarget);
                }
                targetComboBox.setSelectedItem(customTarget);
            }

            if (originalMoveRouteParams != null) { 
                repeatCheckBox.setSelected(originalMoveRouteParams.optBoolean("repeat", false));
                skipIfCannotMoveCheckBox.setSelected(originalMoveRouteParams.optBoolean("skippable", false));
            } else {
                System.err.println("Warning: originalMoveRouteParams is null when loading data for SetMoveRouteEditorDialog.");
                repeatCheckBox.setSelected(false);
                skipIfCannotMoveCheckBox.setSelected(false);
            }

            refreshMoveCommandListFromJSON();

        } catch (JSONException e) {
            System.err.println("Error loading move route data: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading move route data: " + e.getMessage(), "Loading Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMoveCommandListFromJSON() {
        moveCommandListModel.clear();
        for (int i = 0; i < modifiedMoveRouteList.length(); i++) {
            try {
                JSONObject moveCmdJson = modifiedMoveRouteList.getJSONObject(i);
                if (moveCmdJson.optInt("code", -1) != 0) { 
                    moveCommandListModel.addElement(new EventCommand(509, "1", new JSONArray().put(moveCmdJson)));
                }
            } catch (JSONException e) {
                System.err.println("Error refreshing move command list (inner command parsing): " + e.getMessage());
            }
        }
    }

    private void editInnerMoveCommand(EventCommand commandToEdit, int indexInList) {
        try {
            JSONObject innerMoveCmdJson = commandToEdit.getParameters().getJSONObject(0);
            int commandCode = innerMoveCmdJson.getInt("code");
            String commandName = MOVE_COMMAND_NAMES_BY_CODE.getOrDefault(commandCode, "Code " + commandCode);

            if (commandCode == 41) {
                String graphicName = "";
                int encodedX = 2; // Valeur par défaut RMXP pour x=0
                int row = 0;
                int hue = 0; 

                JSONArray params = innerMoveCmdJson.optJSONArray("parameters");
                if (params != null && params.length() >= 4) { 
                    graphicName = params.optString(0, "");
                    hue = params.optInt(1, 0);
                    encodedX = params.optInt(2, 2);
                    row = params.optInt(3, 0);
                }

                ChangeGraphicDialog graphicDialog = new ChangeGraphicDialog(this, graphicName, encodedX, row, hue);
                graphicDialog.setVisible(true);

                if (graphicDialog.isOkPressed()) {
                    String newGraphicName = graphicDialog.getSelectedGraphicName();
                    int newEncodedX = graphicDialog.getSelectedColForRpgMaker();
                    int newRow = graphicDialog.getSelectedRow();
                    int newHue = graphicDialog.getHueValue();
                    
                    JSONArray newParams = new JSONArray();
                    newParams.put(newGraphicName); // index 0
                    newParams.put(newHue);         // index 1
                    newParams.put(newEncodedX);    // index 2  
                    newParams.put(newRow);         // index 3

                    if (!newParams.toString().equals(params.toString())) {
                        innerMoveCmdJson.put("parameters", newParams);
                        commandToEdit.setParameters(new JSONArray().put(innerMoveCmdJson));
                        moveCommandListModel.set(indexInList, commandToEdit);
                        modifiedMoveRouteList.put(indexInList, innerMoveCmdJson);
                        commandModified = true;
                        System.out.println("Graphic parameters updated for command at index " + indexInList + ": " + newParams.toString());
                    } else {
                        System.out.println("No changes detected for graphic parameters.");
                    }
                }
                return;
            }

            if (MOVE_COMMAND_EDITABILITY.getOrDefault(commandCode, false)) {
                JSONArray originalParams = innerMoveCmdJson.getJSONArray("parameters");
                String initialParamText;
                if (originalParams.length() == 0) {
                    initialParamText = ""; 
                } else {
                    initialParamText = originalParams.toString();
                    if (initialParamText.startsWith("[") && initialParamText.endsWith("]")) {
                        initialParamText = initialParamText.substring(1, initialParamText.length() - 1);
                    }
                }

                JTextArea parametersTextArea = new JTextArea(initialParamText, 5, 30);
                parametersTextArea.setWrapStyleWord(true);
                parametersTextArea.setLineWrap(true);
                JScrollPane scrollPane = new JScrollPane(parametersTextArea);
                scrollPane.setPreferredSize(new java.awt.Dimension(400, 200));

                int result = JOptionPane.showConfirmDialog(this,
                    scrollPane,
                    "Edit Parameters for " + commandName + " (Code: " + commandCode + ")",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    String editedText = parametersTextArea.getText().trim();
                    JSONArray newParams;
                    try {
                        if (editedText.isEmpty()) {
                            newParams = new JSONArray();
                        } else {
                            if (!editedText.startsWith("[") || !editedText.endsWith("]")) {
                                editedText = "[" + editedText + "]";
                            }
                            newParams = new JSONArray(editedText);
                        }

                        innerMoveCmdJson.put("parameters", newParams);
                        commandToEdit.setParameters(new JSONArray().put(innerMoveCmdJson));
                        moveCommandListModel.set(indexInList, commandToEdit);
                        modifiedMoveRouteList.put(indexInList, innerMoveCmdJson);
                        commandModified = true;
                        System.out.println("Parameters updated for command at index " + indexInList + ": " + newParams.toString());
                    } catch (JSONException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid JSON Array format for parameters. Please use: [] or [param1, param2, ...]", "Input Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Invalid JSON input for parameters: " + ex.getMessage());
                    }
                }
            } else {
                System.out.println("Command " + commandName + " (Code: " + commandCode + ") is not editable.");
            }

        } catch (JSONException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error editing move command: " + e.getMessage(), "Edit Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error editing inner move command: " + e.getMessage());
        }
    }


    private void deleteInnerMoveCommand() {
        int selectedIndex = moveCommandList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a move command to delete.", "No Command Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        moveCommandListModel.remove(selectedIndex);
        modifiedMoveRouteList.remove(selectedIndex);

        System.out.println("DEBUG: Inner move command deleted.");

        if (moveCommandListModel.size() > 0) {
            if (selectedIndex < moveCommandListModel.size()) {
                moveCommandList.setSelectedIndex(selectedIndex);
            } else {
                moveCommandList.setSelectedIndex(moveCommandListModel.size() - 1);
            }
            moveCommandList.ensureIndexIsVisible(moveCommandList.getSelectedIndex());
        }
    }

    private void moveInnerMoveCommandUp() {
        int selectedIndex = moveCommandList.getSelectedIndex();
        if (selectedIndex <= 0) {
            return;
        }

        JSONObject jsonToMove = null;
        try {
            jsonToMove = modifiedMoveRouteList.getJSONObject(selectedIndex);
        } catch (JSONException e) {
            System.err.println("Error getting JSON object for move up: " + e.getMessage());
            return;
        }

        modifiedMoveRouteList.remove(selectedIndex);
        modifiedMoveRouteList = insertNewJSONObjectAt(modifiedMoveRouteList, jsonToMove, selectedIndex - 1);

        refreshMoveCommandListFromJSON();
        moveCommandList.setSelectedIndex(selectedIndex - 1);
        moveCommandList.ensureIndexIsVisible(selectedIndex - 1);
        System.out.println("DEBUG: Inner move command moved up in JSON.");
    }

    private void moveInnerMoveCommandDown() {
        int selectedIndex = moveCommandList.getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex >= moveCommandListModel.size() - 1) { 
            return;
        }

        JSONObject jsonToMove = null;
        try {
            jsonToMove = modifiedMoveRouteList.getJSONObject(selectedIndex);
        } catch (JSONException e) {
            System.err.println("Error getting JSON object for move down: " + e.getMessage());
            return;
        }

        modifiedMoveRouteList.remove(selectedIndex);
        modifiedMoveRouteList = insertNewJSONObjectAt(modifiedMoveRouteList, jsonToMove, selectedIndex + 1);

        refreshMoveCommandListFromJSON();
        moveCommandList.setSelectedIndex(selectedIndex + 1);
        moveCommandList.ensureIndexIsVisible(selectedIndex + 1);
        System.out.println("DEBUG: Inner move command moved down in JSON.");
    }

    private JSONArray insertNewJSONObjectAt(JSONArray originalArray, JSONObject objToInsert, int insertIndex) {
        JSONArray newArray = new JSONArray();
        int length = originalArray.length();

        if (insertIndex < 0) insertIndex = 0;
        if (insertIndex > length) insertIndex = length;

        for (int i = 0; i < length; i++) {
            if (i == insertIndex) {
                newArray.put(objToInsert);
            }
            try {
                newArray.put(originalArray.getJSONObject(i));
            } catch (JSONException e) {
                System.err.println("Error copying JSON during new insertion: " + e.getMessage());
            }
        }
        if (insertIndex == length && newArray.length() < length + 1) {
            newArray.put(objToInsert);
        }
        return newArray;
    }


    private void saveChanges() {
        try {
            int targetId = 0;
            String selectedTarget = (String) targetComboBox.getSelectedItem();
            if ("Player".equals(selectedTarget)) {
                targetId = -1;
            } else if ("This Event".equals(selectedTarget)) {
                targetId = 0;
            } else if (selectedTarget.startsWith("Event ")) {
                try {
                    targetId = Integer.parseInt(selectedTarget.substring("Event ".length()));
                } catch (NumberFormatException e) {
                    System.err.println("Could not parse custom event ID: " + selectedTarget);
                }
            } else {
                for (Map.Entry<Integer, String> entry : eventTargetMap.entrySet()) {
                    if (entry.getValue().equals(selectedTarget)) {
                        targetId = entry.getKey();
                        break;
                    }
                }
            }
            modified209Parameters.put(0, targetId);

            JSONObject routeData = new JSONObject();
            routeData.put("repeat", repeatCheckBox.isSelected());
            routeData.put("skippable", skipIfCannotMoveCheckBox.isSelected());
            
            JSONArray finalMoveRouteList = new JSONArray();
            for(int i = 0; i < modifiedMoveRouteList.length(); i++) {
                finalMoveRouteList.put(modifiedMoveRouteList.getJSONObject(i));
            }
            finalMoveRouteList.put(new JSONObject().put("code", 0).put("parameters", new JSONArray()));

            routeData.put("list", finalMoveRouteList);
            modified209Parameters.put(1, routeData);

            modified209Parameters.put(2, !skipIfCannotMoveCheckBox.isSelected()); 

            System.out.println("DEBUG: SetMoveRouteEditorDialog: Changes saved to modified209Parameters.");
            System.out.println("DEBUG: Final 209 Parameters: " + modified209Parameters.toString(2));

        } catch (JSONException e) {
            JOptionPane.showMessageDialog(this, "Error saving move route data: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error saving move route data: " + e.getMessage());
        }
    }

    public boolean isCommandModified() {
        return commandModified;
    }

    public JSONArray getModified209Parameters() {
        return modified209Parameters;
    }
}