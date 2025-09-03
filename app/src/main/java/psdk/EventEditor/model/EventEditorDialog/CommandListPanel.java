package psdk.EventEditor.model.EventEditorDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import libs.json.JSONArray;
import libs.json.JSONException;
import psdk.EventEditor.model.Event;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRouteEditorDialog;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.ShowTextEditorDialog;
import psdk.EventEditor.views.EventCommandListCellRenderer;

/**
 * Panel for displaying and managing event commands in a list format.
 * Supports adding, deleting, moving, and editing commands.
 */
public class CommandListPanel extends JPanel {
    
    // Constants for command codes
    private static final int COMMAND_COMMENT = 108;
    private static final int COMMAND_SHOW_TEXT = 101;
    private static final int COMMAND_SET_MOVE_ROUTE = 209;
    
    // UI Constants
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    private static final Color LIST_BACKGROUND_COLOR = Color.WHITE;
    private static final Dimension PREFERRED_SIZE = new Dimension(0, 300);
    
    // Core components
    private final JList<EventCommand> commandList;
    private final DefaultListModel<EventCommand> commandListModel;
    private final CommandOperations commandOperations;
    private final CommandEditorManager editorManager;
    
    // External dependencies
    private JTabbedPane pageTabbedPane;
    private PagePropertiesPanel pagePropertiesPanel;
    private Event event;
    private CommandListModificationListener modificationListener;
    private JDialog parentDialog;

    public CommandListPanel() {
        this.commandListModel = new DefaultListModel<>();
        this.commandList = createCommandList();
        this.commandOperations = new CommandOperations();
        this.editorManager = new CommandEditorManager();
        
        initializeLayout();
    }

    /**
     * Creates and configures the command list component.
     */
    private JList<EventCommand> createCommandList() {
        JList<EventCommand> list = new JList<>(commandListModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setBackground(LIST_BACKGROUND_COLOR);
        list.setCellRenderer(new EventCommandListCellRenderer());
        list.addMouseListener(new CommandListMouseListener());
        return list;
    }

    /**
     * Initializes the panel layout and components.
     */
    private void initializeLayout() {
        JPanel mainPanel = createMainPanel();
        mainPanel.add(createScrollPane(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Creates the main panel with border and background.
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
            "List of Event Commands:"
        );
        panel.setBorder(border);
        panel.setPreferredSize(PREFERRED_SIZE);
        
        return panel;
    }

    /**
     * Creates the scroll pane for the command list.
     */
    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(commandList);
        scrollPane.setBackground(LIST_BACKGROUND_COLOR);
        return scrollPane;
    }

    /**
     * Creates the button panel with all command operation buttons.
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("New...", this::addCommand),
            new ButtonConfig("Indent", this::indentCommand),
            new ButtonConfig("Outdent", this::outdentCommand),
            new ButtonConfig("Copy", this::copyCommand),
            new ButtonConfig("Paste", this::pasteCommand),
            new ButtonConfig("Delete", this::deleteCommand),
            new ButtonConfig("Move Up", this::moveCommandUp),
            new ButtonConfig("Move Down", this::moveCommandDown)
        };
        
        for (ButtonConfig config : buttons) {
            JButton button = createStyledButton(config.text, config.action);
            buttonPanel.add(button);
        }
        
        return buttonPanel;
    }

    /**
     * Creates a styled button with the given text and action.
     */
    private JButton createStyledButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    /**
     * Updates the command list with new commands.
     */
    public void updateCommandList(List<EventCommand> commands) {
        commandListModel.clear();
        if (commands != null) {
            commands.forEach(commandListModel::addElement);
        }
        System.out.println("DEBUG: Command list updated with " + commandListModel.size() + " commands.");
    }

    /**
     * Loads event pages into the tabbed pane.
     */
    public void loadEventPages(Event event) {
        if (!validatePageTabbedPane()) return;
        
        pageTabbedPane.removeAll();
        
        if (event.getPages() == null || event.getPages().isEmpty()) {
            createDefaultPage(event);
        } else {
            loadExistingPages(event);
        }
        
        selectFirstPageAndUpdateUI(event);
    }

    /**
     * Validates that the page tabbed pane is available.
     */
    private boolean validatePageTabbedPane() {
        if (pageTabbedPane == null) {
            System.err.println("ERROR: pageTabbedPane is null in CommandListPanel. Cannot load event pages.");
            return false;
        }
        return true;
    }

    /**
     * Creates a default page when no pages exist.
     */
    private void createDefaultPage(Event event) {
        EventPage defaultPage = new EventPage();
        event.getPages().add(defaultPage);
        JPanel pagePanel = createPagePanel(defaultPage, 1);
        pageTabbedPane.addTab("1", pagePanel);
    }

    /**
     * Loads existing pages into the tabbed pane.
     */
    private void loadExistingPages(Event event) {
        for (int i = 0; i < event.getPages().size(); i++) {
            EventPage page = event.getPages().get(i);
            JPanel pagePanel = createPagePanel(page, i + 1);
            pageTabbedPane.addTab(String.valueOf(i + 1), pagePanel);
        }
    }

    /**
     * Selects the first page and updates the UI accordingly.
     */
    private void selectFirstPageAndUpdateUI(Event event) {
        if (pageTabbedPane.getTabCount() > 0) {
            pageTabbedPane.setSelectedIndex(0);
            EventPage firstPage = event.getPages().get(0);
            updateCommandList(firstPage.getCommands());
            updatePagePropertiesPanel(firstPage);
        }
    }

    /**
     * Updates the page properties panel if available.
     */
    private void updatePagePropertiesPanel(EventPage page) {
        if (pagePropertiesPanel != null) {
            pagePropertiesPanel.updatePageProperties(page);
        }
    }

    /**
     * Creates a panel for an event page.
     */
    private JPanel createPagePanel(EventPage page, int pageNumber) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        return panel;
    }

    // Command operations
    private void addCommand() {
        commandOperations.addCommand();
    }

    private void deleteCommand() {
        commandOperations.deleteCommand();
    }

    private void moveCommandUp() {
        commandOperations.moveCommandUp();
    }

    private void moveCommandDown() {
        commandOperations.moveCommandDown();
    }

    private void indentCommand() {
        System.out.println("DEBUG: Indent command not yet implemented");
    }

    private void outdentCommand() {
        System.out.println("DEBUG: Outdent command not yet implemented");
    }

    private void copyCommand() {
        System.out.println("DEBUG: Copy command not yet implemented");
    }

    private void pasteCommand() {
        System.out.println("DEBUG: Paste command not yet implemented");
    }

    /**
     * Mouse listener for handling double-clicks on command list.
     */
    private class CommandListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                EventCommand selectedCommand = commandList.getSelectedValue();
                if (selectedCommand != null) {
                    editorManager.openSpecificCommandEditor(selectedCommand);
                }
            }
        }
    }

    /**
     * Handles command operations like add, delete, move.
     */
    private class CommandOperations {
        
        public void addCommand() {
            EventPage currentPage = getCurrentPage();
            if (currentPage == null) return;

            EventCommand newCommand = createNewCommand();
            int insertIndex = calculateInsertIndex();
            
            insertCommand(newCommand, insertIndex, currentPage);
            selectNewCommand(newCommand);
            notifyModification();
        }

        public void deleteCommand() {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex == -1) {
                showWarning("Please select a command to delete.", "No Command Selected");
                return;
            }

            if (confirmDelete()) {
                EventCommand commandToRemove = commandListModel.getElementAt(selectedIndex);
                removeCommand(selectedIndex, commandToRemove);
                updateSelectionAfterDelete(selectedIndex);
                notifyModification();
            }
        }

        public void moveCommandUp() {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex <= 0) return;

            moveCommand(selectedIndex, selectedIndex - 1);
            commandList.setSelectedIndex(selectedIndex - 1);
            notifyModification();
        }

        public void moveCommandDown() {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex == -1 || selectedIndex >= commandListModel.size() - 1) return;

            moveCommand(selectedIndex, selectedIndex + 1);
            commandList.setSelectedIndex(selectedIndex + 1);
            notifyModification();
        }

        private EventPage getCurrentPage() {
            int currentPageIndex = pageTabbedPane.getSelectedIndex();
            if (currentPageIndex == -1 || currentPageIndex >= event.getPages().size()) {
                showWarning("Please select an event page first.", "No Page Selected");
                return null;
            }

            EventPage currentPage = event.getPages().get(currentPageIndex);
            if (currentPage == null) {
                showWarning("Selected page is invalid.", "Invalid Page");
                return null;
            }

            return currentPage;
        }

        private EventCommand createNewCommand() {
            return new EventCommand(COMMAND_COMMENT, "0", new JSONArray().put("New Comment Added"));
        }

        private int calculateInsertIndex() {
            int selectedIndex = commandList.getSelectedIndex();
            return (selectedIndex == -1 || commandListModel.isEmpty()) ? 
                commandListModel.size() : selectedIndex + 1;
        }

        private void insertCommand(EventCommand command, int index, EventPage page) {
            if (index >= commandListModel.size()) {
                commandListModel.addElement(command);
                page.getCommands().add(command);
            } else {
                commandListModel.add(index, command);
                page.getCommands().add(index, command);
            }
        }

        private void selectNewCommand(EventCommand command) {
            commandList.setSelectedValue(command, true);
            System.out.println("DEBUG: New command (Code: " + command.getCode() + ") added to model and list.");
        }

        private boolean confirmDelete() {
            int confirm = JOptionPane.showConfirmDialog(
                CommandListPanel.this,
                "Are you sure you want to delete the selected command?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );
            return confirm == JOptionPane.YES_OPTION;
        }

        private void removeCommand(int index, EventCommand command) {
            commandListModel.remove(index);
            
            EventPage currentPage = getCurrentPage();
            if (currentPage != null) {
                currentPage.getCommands().remove(command);
                System.out.println("DEBUG: Command (Code: " + command.getCode() + ") removed from EventPage model and list.");
            }
        }

        private void updateSelectionAfterDelete(int deletedIndex) {
            if (commandListModel.size() > 0) {
                int newIndex = Math.min(deletedIndex, commandListModel.size() - 1);
                commandList.setSelectedIndex(newIndex);
            }
        }

        private void moveCommand(int fromIndex, int toIndex) {
            // Move in UI model
            EventCommand commandToMove = commandListModel.getElementAt(fromIndex);
            commandListModel.remove(fromIndex);
            commandListModel.add(toIndex, commandToMove);

            // Move in data model
            EventPage currentPage = getCurrentPage();
            if (currentPage != null) {
                List<EventCommand> commands = currentPage.getCommands();
                EventCommand modelCommand = commands.remove(fromIndex);
                commands.add(toIndex, modelCommand);
                System.out.println("DEBUG: Command moved in EventPage model.");
            }
        }
    }

    /**
     * Manages opening specific command editors.
     */
    private class CommandEditorManager {
        
        public void openSpecificCommandEditor(EventCommand commandToEdit) {
            System.out.println("DEBUG: Attempting to open specific editor for command code: " + commandToEdit.getCode());
            
            if (parentDialog == null) {
                showError("Parent dialog not set for editor.", "Error");
                return;
            }

            EventCommand commandCopy = createCommandCopy(commandToEdit);
            boolean modified = false;

            switch (commandToEdit.getCode()) {
                case COMMAND_SET_MOVE_ROUTE:
                    modified = handleSetMoveRouteEditor(commandToEdit, commandCopy);
                    break;
                case COMMAND_SHOW_TEXT:
                    modified = handleShowTextEditor(commandToEdit, commandCopy);
                    break;
                default:
                    showNoEditorMessage(commandToEdit.getCode());
                    return;
            }

            if (modified) {
                updateCommandAfterEdit(commandToEdit);
                notifyModification();
            }
        }

        private EventCommand createCommandCopy(EventCommand original) {
            try {
                JSONArray parametersCopy = original.getParameters() != null ? 
                    new JSONArray(original.getParameters().toString()) : new JSONArray();
                return new EventCommand(original.getCode(), original.getIndent(), parametersCopy);
            } catch (JSONException e) {
                System.err.println("Error deep copying EventCommand for specific editor: " + e.getMessage());
                return new EventCommand(original.getCode(), original.getIndent(), new JSONArray());
            }
        }

        private boolean handleSetMoveRouteEditor(EventCommand original, EventCommand copy) {
            SetMoveRouteEditorDialog dialog = new SetMoveRouteEditorDialog(parentDialog, copy);
            dialog.setVisible(true);
            
            if (dialog.isCommandModified()) {
                original.setParameters(dialog.getModified209Parameters());
                return true;
            }
            return false;
        }

        private boolean handleShowTextEditor(EventCommand original, EventCommand copy) {
            ShowTextEditorDialog dialog = new ShowTextEditorDialog(parentDialog, copy);
            dialog.setVisible(true);
            
            if (dialog.isCommandModified()) {
                EventCommand modified = dialog.getModifiedCommand();
                original.setCode(modified.getCode());
                original.setIndent(modified.getIndent());
                original.setParameters(modified.getParameters());
                return true;
            }
            return false;
        }

        private void showNoEditorMessage(int commandCode) {
            JOptionPane.showMessageDialog(
                CommandListPanel.this,
                "No specific editor available for command code: " + commandCode,
                "Editor Not Found",
                JOptionPane.INFORMATION_MESSAGE
            );
            System.out.println("DEBUG: No specific editor for command code: " + commandCode);
        }

        private void updateCommandAfterEdit(EventCommand commandToEdit) {
            int selectedIndex = commandList.getSelectedIndex();
            if (selectedIndex != -1) {
                commandListModel.set(selectedIndex, commandToEdit);
                updateCommandInEventPage(selectedIndex, commandToEdit);
                System.out.println("DEBUG: Command updated in EventPage model at index " + selectedIndex + " after specific editor.");
            }
        }

        private void updateCommandInEventPage(int index, EventCommand command) {
            int currentPageIndex = pageTabbedPane.getSelectedIndex();
            if (currentPageIndex != -1 && currentPageIndex < event.getPages().size()) {
                EventPage currentPage = event.getPages().get(currentPageIndex);
                if (currentPage != null && index < currentPage.getCommands().size()) {
                    currentPage.getCommands().set(index, command);
                }
            }
        }
    }

    /**
     * Helper class for button configuration.
     */
    private static class ButtonConfig {
        final String text;
        final Runnable action;

        ButtonConfig(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }

    // Utility methods
    private void showWarning(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void notifyModification() {
        if (modificationListener != null) {
            modificationListener.onCommandsModified();
        }
        System.out.println("DEBUG: Event marked as modified");
    }

    // Setters
    public void setEvent(Event event) {
        this.event = event;
    }

    public void setPageTabbedPane(JTabbedPane pageTabbedPane) {
        this.pageTabbedPane = pageTabbedPane;
    }

    public void setPagePropertiesPanel(PagePropertiesPanel pagePropertiesPanel) {
        this.pagePropertiesPanel = pagePropertiesPanel;
    }

    public void setModificationListener(CommandListModificationListener listener) {
        this.modificationListener = listener;
    }

    public void setParentDialog(JDialog parentDialog) {
        this.parentDialog = parentDialog;
    }
}