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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRoutePackage.MoveCommandHandler;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRoutePackage.MoveCommandRegistry;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRoutePackage.MoveCommandUI;
import psdk.EventEditor.utils.DialogKeyBindingUtils;
import psdk.EventEditor.views.EventCommandListCellRenderer;
import psdk.EventEditor.views.MoveCommandQuickInsertDialog;

public class SetMoveRouteEditorDialog extends JDialog implements 
        MoveCommandUI.MoveCommandInsertCallback, 
        MoveCommandUI.MoveCommandGraphicCallback,
        DialogKeyBindingUtils.SaveableDialog {

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

    private List<EventCommand> fullCommandList;
    private int setMoveRouteIndex;
    private boolean commandModified = false;
    private Map<Integer, String> eventTargetMap;

    private MoveCommandHandler commandHandler;
    private MoveCommandUI commandUI;

    public SetMoveRouteEditorDialog(Dialog owner, List<EventCommand> commandList, int commandIndex) {
        super(owner, "Set Move Route", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(800, 680);
        setLocationRelativeTo(owner);

        this.fullCommandList = commandList;
        this.setMoveRouteIndex = commandIndex;
        this.commandHandler = new MoveCommandHandler(this);
        this.commandUI = new MoveCommandUI(this, this);
        
        initializeFromCommand(commandList.get(commandIndex));
        initializeEventTargetMap();
        initComponents();
        loadMoveRouteData();
        setupKeyBindings();
    }

    public SetMoveRouteEditorDialog(Dialog owner, EventCommand setMoveRouteCommand) {
        this(owner, Arrays.asList(setMoveRouteCommand), 0);
    }

    private void initializeFromCommand(EventCommand setMoveRouteCommand) {
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
            handleFallbackInitialization(setMoveRouteCommand);
        }
    }

    private void handleFallbackInitialization(EventCommand setMoveRouteCommand) {
        this.modified209Parameters = setMoveRouteCommand.getParameters();
        this.originalMoveRouteParams = this.modified209Parameters.optJSONObject(1);
        this.modifiedMoveRouteList = (this.originalMoveRouteParams != null) ? 
            this.originalMoveRouteParams.optJSONArray("list") : new JSONArray();
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

    private void initializeEventTargetMap() {
        eventTargetMap = new HashMap<>();
        eventTargetMap.put(-1, "Player");
        eventTargetMap.put(0, "This Event");
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
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

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerContainerPanel = new JPanel(new BorderLayout(10, 10));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.20);
        splitPane.setDividerLocation(0.20);

        splitPane.setLeftComponent(createMoveCommandListPanel());
        splitPane.setRightComponent(createAvailableCommandsPanel());

        centerContainerPanel.add(splitPane, BorderLayout.CENTER);
        centerContainerPanel.add(createOptionsPanel(), BorderLayout.SOUTH);

        return centerContainerPanel;
    }

    private JPanel createMoveCommandListPanel() {
        JPanel moveCommandListPanel = new JPanel(new BorderLayout());
        moveCommandListPanel.setBorder(BorderFactory.createTitledBorder("Current Move Commands"));

        moveCommandListModel = new DefaultListModel<>();
        moveCommandList = new JList<>(moveCommandListModel);
        moveCommandCellRenderer = new EventCommandListCellRenderer();
        moveCommandList.setCellRenderer(moveCommandCellRenderer);
        moveCommandList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setupMoveCommandListListeners();

        JScrollPane scrollPaneList = new JScrollPane(moveCommandList);
        moveCommandListPanel.add(scrollPaneList, BorderLayout.CENTER);
        moveCommandListPanel.add(createMoveCommandButtonPanel(), BorderLayout.SOUTH);

        return moveCommandListPanel;
    }

    private void setupMoveCommandListListeners() {
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
    }

    private JPanel createMoveCommandButtonPanel() {
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
        return moveCommandButtonPanel;
    }

    private JPanel createAvailableCommandsPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Available Move Commands"));

        JPanel buttonsGridPanel = new JPanel(new GridBagLayout());
        commandUI.createMoveCommandButtons(buttonsGridPanel, commandHandler);

        rightPanel.add(buttonsGridPanel, BorderLayout.CENTER);
        return rightPanel;
    }

    private JPanel createOptionsPanel() {
        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        repeatCheckBox = new JCheckBox("Repeat Action");
        skipIfCannotMoveCheckBox = new JCheckBox("Ignore If Can't Move");

        optionsPanel.add(repeatCheckBox);
        optionsPanel.add(skipIfCannotMoveCheckBox);

        waitForCompletionCheckBox = new JCheckBox("Wait for Completion (Hidden)");
        waitForCompletionCheckBox.setVisible(false);

        return optionsPanel;
    }

    private JPanel createBottomPanel() {
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
        return bottomPanel;
    }

    private void setupKeyBindings() {
        DialogKeyBindingUtils.setupStandardKeyBindings(this, this);
    }

    @Override
    public void saveAndClose() {
        saveChanges();
        commandModified = true;
        dispose();
    }

    @Override
    public void cancelAndClose() {
        commandModified = false;
        dispose();
    }

    // MoveCommandInsertCallback implementation
    @Override
    public void insertCommand(int moveCode, String commandName, JSONArray parameters) {
        insertCommandInternal(moveCode, commandName, parameters);
    }

    // MoveCommandGraphicCallback implementation
    @Override
    public void openGraphicDialog(int moveCode, String commandName) {
        String defaultGraphicName = "";

        ChangeGraphicDialog graphicDialog = new ChangeGraphicDialog(this, defaultGraphicName, 0, 2, 0); // characterIndex=0, direction=2 (Down), pattern=0
        graphicDialog.setVisible(true);

        if (graphicDialog.isOkPressed()) {
            String newGraphicName = graphicDialog.getSelectedGraphicName();
            int newCharacterIndex = graphicDialog.getCharacterIndex();
            int newDirection = graphicDialog.getSelectedDirection();
            int newPattern = graphicDialog.getSelectedPattern();

            JSONArray newParams = new JSONArray();
            newParams.put(newGraphicName);
            newParams.put(0);
            newParams.put(newDirection);
            newParams.put(newPattern);

            insertCommandInternal(moveCode, commandName, newParams);
        }
    }

    private void showQuickInsertDialog() {
        MoveCommandQuickInsertDialog quickInsertDialog = new MoveCommandQuickInsertDialog(this, MoveCommandRegistry.MOVE_COMMAND_CODES);
        quickInsertDialog.setVisible(true);

        Map.Entry<String, Integer> selectedCommandData = quickInsertDialog.getSelectedCommand();
        if (selectedCommandData != null) {
            int moveCode = selectedCommandData.getValue();
            String commandName = selectedCommandData.getKey();
            
            JSONArray defaultParams = MoveCommandQuickInsertDialog.getDefaultParametersForCommand(moveCode);
            insertCommandInternal(moveCode, commandName, defaultParams);
        }
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
            String commandName = MoveCommandRegistry.getCommandName(commandCode);

            if (commandCode == 41) {
                handleGraphicCommandEdit(commandToEdit, indexInList, innerMoveCmdJson);
                return;
            }

            if (commandHandler.editMoveCommandParameters(innerMoveCmdJson, commandCode, commandName, indexInList)) {
                commandToEdit.setParameters(new JSONArray().put(innerMoveCmdJson));
                moveCommandListModel.set(indexInList, commandToEdit);
                modifiedMoveRouteList.put(indexInList, innerMoveCmdJson);
                commandModified = true;
            }

        } catch (JSONException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Error editing move command: " + e.getMessage(), "Edit Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error editing inner move command: " + e.getMessage());
        }
    }

    private void handleGraphicCommandEdit(EventCommand commandToEdit, int indexInList, JSONObject innerMoveCmdJson) throws JSONException {
        String graphicName = "";
        int encodedX = 2;
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
            int newCharacterIndex = graphicDialog.getCharacterIndex();
            int newDirection = graphicDialog.getSelectedDirection();
            int newPattern = graphicDialog.getSelectedPattern();
            int newHue = graphicDialog.getHueValue();
            
            JSONArray newParams = new JSONArray();
            newParams.put(newGraphicName);
            newParams.put(newHue);
            newParams.put(newDirection);
            newParams.put(newPattern);

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

            EventCommand setMoveCommand = fullCommandList.get(setMoveRouteIndex);
            setMoveCommand.setParameters(modified209Parameters);
            
            updateCommand509List();

            System.out.println("DEBUG: SetMoveRouteEditorDialog: Changes saved and 509 commands synchronized.");
            System.out.println("DEBUG: Final 209 Parameters: " + modified209Parameters.toString(2));

        } catch (JSONException e) {
            JOptionPane.showMessageDialog(this, "Error saving move route data: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error saving move route data: " + e.getMessage());
        }
    }

    private void updateCommand509List() {
        try {
            // Remove old 509 commands
            int currentIndex = setMoveRouteIndex + 1;
            while (currentIndex < fullCommandList.size() && 
                   fullCommandList.get(currentIndex).getCode() == 509) {
                fullCommandList.remove(currentIndex);
            }
            
            // Add new 509 commands
            int insertIndex = setMoveRouteIndex + 1;
            for (int i = 0; i < modifiedMoveRouteList.length(); i++) {
                JSONObject moveCmd = modifiedMoveRouteList.getJSONObject(i);
                EventCommand cmd509 = new EventCommand(509, "0", new JSONArray().put(moveCmd));
                fullCommandList.add(insertIndex, cmd509);
                insertIndex++;
            }
            
            System.out.println("DEBUG: Updated " + modifiedMoveRouteList.length() + " command 509 entries");
            
        } catch (JSONException e) {
            System.err.println("Error updating 509 commands: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error synchronizing move commands: " + e.getMessage(), 
                "Synchronization Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isCommandModified() {
        return commandModified;
    }

    public JSONArray getModified209Parameters() {
        return modified209Parameters;
    }
}