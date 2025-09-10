package psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import libs.json.JSONArray;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanel;

/**
 * Handles command operations like add, delete, move.
 */
public class CommandOperations {
    
    private static final int COMMAND_COMMENT = 108;
    
    private final CommandListPanel panel;

    public CommandOperations(CommandListPanel panel) {
        this.panel = panel;
    }

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
        JList<EventCommand> commandList = panel.getCommandList();
        DefaultListModel<EventCommand> commandListModel = panel.getCommandListModel();
        
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
        JList<EventCommand> commandList = panel.getCommandList();
        int selectedIndex = commandList.getSelectedIndex();
        if (selectedIndex <= 0) return;

        moveCommand(selectedIndex, selectedIndex - 1);
        commandList.setSelectedIndex(selectedIndex - 1);
        notifyModification();
    }

    public void moveCommandDown() {
        JList<EventCommand> commandList = panel.getCommandList();
        DefaultListModel<EventCommand> commandListModel = panel.getCommandListModel();
        
        int selectedIndex = commandList.getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex >= commandListModel.size() - 1) return;

        moveCommand(selectedIndex, selectedIndex + 1);
        commandList.setSelectedIndex(selectedIndex + 1);
        notifyModification();
    }

    private EventPage getCurrentPage() {
        if (panel.getPageTabbedPane() == null || panel.getEvent() == null) {
            showWarning("Please select an event page first.", "No Page Selected");
            return null;
        }
        
        int currentPageIndex = panel.getPageTabbedPane().getSelectedIndex();
        if (currentPageIndex == -1 || currentPageIndex >= panel.getEvent().getPages().size()) {
            showWarning("Please select an event page first.", "No Page Selected");
            return null;
        }

        EventPage currentPage = panel.getEvent().getPages().get(currentPageIndex);
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
        JList<EventCommand> commandList = panel.getCommandList();
        DefaultListModel<EventCommand> commandListModel = panel.getCommandListModel();
        
        int selectedIndex = commandList.getSelectedIndex();
        return (selectedIndex == -1 || commandListModel.isEmpty()) ? 
            commandListModel.size() : selectedIndex + 1;
    }

    private void insertCommand(EventCommand command, int index, EventPage page) {
        DefaultListModel<EventCommand> commandListModel = panel.getCommandListModel();
        
        if (index >= commandListModel.size()) {
            commandListModel.addElement(command);
            page.getCommands().add(command);
        } else {
            commandListModel.add(index, command);
            page.getCommands().add(index, command);
        }
    }

    private void selectNewCommand(EventCommand command) {
        panel.getCommandList().setSelectedValue(command, true);
    }

    private boolean confirmDelete() {
        int confirm = JOptionPane.showConfirmDialog(
            panel,
            "Are you sure you want to delete the selected command?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        return confirm == JOptionPane.YES_OPTION;
    }

    private void removeCommand(int index, EventCommand command) {
        panel.getCommandListModel().remove(index);
        
        EventPage currentPage = getCurrentPage();
        if (currentPage != null) {
            currentPage.getCommands().remove(command);
        }
    }

    private void updateSelectionAfterDelete(int deletedIndex) {
        DefaultListModel<EventCommand> commandListModel = panel.getCommandListModel();
        JList<EventCommand> commandList = panel.getCommandList();
        
        if (commandListModel.size() > 0) {
            int newIndex = Math.min(deletedIndex, commandListModel.size() - 1);
            commandList.setSelectedIndex(newIndex);
        }
    }

    private void moveCommand(int fromIndex, int toIndex) {
        DefaultListModel<EventCommand> commandListModel = panel.getCommandListModel();
        
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
        }
    }

    private void showWarning(String message, String title) {
        JOptionPane.showMessageDialog(panel, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private void notifyModification() {
        if (panel.getModificationListener() != null) {
            panel.getModificationListener().onCommandsModified();
        }
    }
}