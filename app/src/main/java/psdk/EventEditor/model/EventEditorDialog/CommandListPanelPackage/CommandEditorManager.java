package psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage;

import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanel;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRouteEditorDialog;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.ShowTextEditorDialog;

/**
 * Manages opening specific command editors and handling command modifications.
 */
public class CommandEditorManager {
    
    private static final int COMMAND_SHOW_TEXT = 101;
    private static final int COMMAND_SET_MOVE_ROUTE = 209;
    
    private final CommandListPanel panel;

    public CommandEditorManager(CommandListPanel panel) {
        this.panel = panel;
    }

    public void openCommandEditor(EventCommand selectedCommand) {
        if (selectedCommand.getCode() == COMMAND_SET_MOVE_ROUTE) {
            openSetMoveRouteEditor(selectedCommand);
        } else {
            openSpecificCommandEditor(selectedCommand);
        }
    }

    public void openSpecificCommandEditor(EventCommand commandToEdit) {
        if (panel.getParentDialog() == null) {
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

    public void openSetMoveRouteEditor(EventCommand commandToEdit) {
        if (panel.getParentDialog() == null) {
            showError("Parent dialog not set for editor.", "Error");
            return;
        }

        boolean modified = handleSetMoveRouteEditor(commandToEdit, commandToEdit);

        if (modified) {
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
        EventPage currentPage = getCurrentPage();
        if (currentPage == null) return false;
        
        List<EventCommand> commands = currentPage.getCommands();
        int commandIndex = findCommandIndex(commands, original);
        if (commandIndex == -1) {
            System.err.println("Could not find Set Move Route command in command list");
            return false;
        }
        
        if (!verifySetMoveRouteSync(commands, commandIndex)) {
            int choice = JOptionPane.showConfirmDialog(
                panel,
                "The Set Move Route command is not properly synchronized with its 509 commands.\n" +
                "Do you want to repair the synchronization before editing?",
                "Synchronization Issue",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (choice == JOptionPane.YES_OPTION) {
                repairSetMoveRouteSync(commands, commandIndex);
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        
        SetMoveRouteEditorDialog dialog = new SetMoveRouteEditorDialog(panel.getParentDialog(), commands, commandIndex);
        dialog.setVisible(true);
        
        if (dialog.isCommandModified()) {
            panel.updateCommandList(commands);
            return true;
        }
        return false;
    }

    private boolean handleShowTextEditor(EventCommand original, EventCommand copy) {
        ShowTextEditorDialog dialog = new ShowTextEditorDialog(panel.getParentDialog(), copy);
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

    private EventPage getCurrentPage() {
        if (panel.getPageTabbedPane() == null || panel.getEvent() == null) {
            return null;
        }
        
        int currentPageIndex = panel.getPageTabbedPane().getSelectedIndex();
        if (currentPageIndex == -1 || currentPageIndex >= panel.getEvent().getPages().size()) {
            return null;
        }
        return panel.getEvent().getPages().get(currentPageIndex);
    }

    private int findCommandIndex(List<EventCommand> commands, EventCommand target) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i) == target) {
                return i;
            }
        }
        return -1;
    }

    private boolean verifySetMoveRouteSync(List<EventCommand> commands, int setMoveRouteIndex) {
        if (setMoveRouteIndex >= commands.size() || commands.get(setMoveRouteIndex).getCode() != COMMAND_SET_MOVE_ROUTE) {
            return false;
        }
        
        EventCommand setMoveCommand = commands.get(setMoveRouteIndex);
        try {
            JSONObject moveRouteParams = setMoveCommand.getParameters().getJSONObject(1);
            JSONArray moveList = moveRouteParams.getJSONArray("list");
            
            int expectedCount = 0;
            for (int i = 0; i < moveList.length(); i++) {
                JSONObject moveCmd = moveList.getJSONObject(i);
                if (moveCmd.getInt("code") != 0) {
                    expectedCount++;
                }
            }
            
            int actualCount = 0;
            for (int i = setMoveRouteIndex + 1; i < commands.size(); i++) {
                if (commands.get(i).getCode() == 509) {
                    actualCount++;
                } else {
                    break;
                }
            }
            
            return expectedCount == actualCount;
            
        } catch (JSONException e) {
            System.err.println("Error verifying Set Move Route sync: " + e.getMessage());
            return false;
        }
    }

    private void repairSetMoveRouteSync(List<EventCommand> commands, int setMoveRouteIndex) {
        try {
            EventCommand setMoveCommand = commands.get(setMoveRouteIndex);
            JSONObject moveRouteParams = setMoveCommand.getParameters().getJSONObject(1);
            JSONArray moveList = moveRouteParams.getJSONArray("list");
            
            // Remove old 509 commands
            int i = setMoveRouteIndex + 1;
            while (i < commands.size() && commands.get(i).getCode() == 509) {
                commands.remove(i);
                panel.getCommandListModel().remove(i);
            }
            
            // Create new 509 commands
            int insertIndex = setMoveRouteIndex + 1;
            for (int j = 0; j < moveList.length(); j++) {
                JSONObject moveCmd = moveList.getJSONObject(j);
                if (moveCmd.getInt("code") != 0) {
                    EventCommand cmd509 = new EventCommand(509, "0", new JSONArray().put(moveCmd));
                    commands.add(insertIndex, cmd509);
                    panel.getCommandListModel().add(insertIndex, cmd509);
                    insertIndex++;
                }
            }
            
        } catch (JSONException e) {
            System.err.println("Error repairing Set Move Route sync: " + e.getMessage());
            JOptionPane.showMessageDialog(
                panel,
                "Error repairing synchronization: " + e.getMessage(),
                "Repair Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showNoEditorMessage(int commandCode) {
        JOptionPane.showMessageDialog(
            panel,
            "No specific editor available for command code: " + commandCode,
            "Editor Not Found",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateCommandAfterEdit(EventCommand commandToEdit) {
        JList<EventCommand> commandList = panel.getCommandList();
        int selectedIndex = commandList.getSelectedIndex();
        if (selectedIndex != -1) {
            panel.getCommandListModel().set(selectedIndex, commandToEdit);
            updateCommandInEventPage(selectedIndex, commandToEdit);
        }
    }

    private void updateCommandInEventPage(int index, EventCommand command) {
        if (panel.getPageTabbedPane() == null || panel.getEvent() == null) {
            return;
        }
        
        int currentPageIndex = panel.getPageTabbedPane().getSelectedIndex();
        if (currentPageIndex != -1 && currentPageIndex < panel.getEvent().getPages().size()) {
            EventPage currentPage = panel.getEvent().getPages().get(currentPageIndex);
            if (currentPage != null && index < currentPage.getCommands().size()) {
                currentPage.getCommands().set(index, command);
            }
        }
    }

    private void showError(String message, String title) {
        JOptionPane.showMessageDialog(panel, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void notifyModification() {
        if (panel.getModificationListener() != null) {
            panel.getModificationListener().onCommandsModified();
        }
    }
}