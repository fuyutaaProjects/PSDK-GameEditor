package psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanel;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.CommentEditorDialog;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.ScriptEditorDialog;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRouteEditorDialog;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.ShowTextEditorDialog;
import static psdk.EventEditor.model.EventEditorDialog.EventCommandCodes.*;

/**
 * Manages opening specific command editors and handling command modifications.
 */
public class CommandEditorManager {
    
    private final CommandListPanel panel;

    public CommandEditorManager(CommandListPanel panel) {
        this.panel = panel;
    }

    public void openCommandEditor(EventCommand selectedCommand) {
        openSpecificCommandEditor(selectedCommand);
    }

    public void openSpecificCommandEditor(EventCommand commandToEdit) {
        if (panel.getParentDialog() == null) {
            showError("Parent dialog not set for editor.", "Error");
            return;
        }

        boolean modified = false;

        switch (commandToEdit.getCode()) {
            case SET_MOVEMENT_ROUTE:
                EventCommand commandCopy = createCommandCopy(commandToEdit);
                modified = handleSetMoveRouteEditor(commandToEdit, commandCopy);
                break;
            case SHOW_TEXT:
                EventCommand showTextCopy = createCommandCopy(commandToEdit);
                modified = handleShowTextEditor(commandToEdit, showTextCopy);
                break;
            case COMMENT:
                modified = handleCommentEditor(commandToEdit);
                break;
            case SCRIPT:
                modified = handleScriptEditor(commandToEdit);
                break;
            default:
                showNoEditorMessage(commandToEdit.getCode());
                return;
        }

        if (modified) {
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
            updateCommandAfterEdit(original);
            return true;
        }
        return false;
    }

    private boolean handleCommentEditor(EventCommand commentCommand) {
        EventPage currentPage = getCurrentPage();
        if (currentPage == null) return false;
        
        List<EventCommand> commands = currentPage.getCommands();
        int commentIndex = findCommandIndex(commands, commentCommand);
        if (commentIndex == -1) {
            System.err.println("Could not find Comment command in command list");
            return false;
        }
        
        // Collect the comment command (108) and all following continuation commands (408)
        List<EventCommand> commentCommands = new ArrayList<>();
        commentCommands.add(commands.get(commentIndex));
        
        // Add all following 408 commands
        int nextIndex = commentIndex + 1;
        while (nextIndex < commands.size() && commands.get(nextIndex).getCode() == COMMENT_CONTINUATION) {
            commentCommands.add(commands.get(nextIndex));
            nextIndex++;
        }
        
        // Open the comment editor
        CommentEditorDialog dialog = new CommentEditorDialog(panel.getParentDialog(), commentCommands);
        dialog.setVisible(true);
        
        if (dialog.isCommandModified()) {
            List<EventCommand> modifiedComments = dialog.getModifiedCommentCommands();
            
            // Remove the old comment commands from the list
            for (int i = 0; i < commentCommands.size(); i++) {
                commands.remove(commentIndex);
                panel.getCommandListModel().remove(commentIndex);
            }
            
            // Insert the new comment commands
            for (int i = 0; i < modifiedComments.size(); i++) {
                commands.add(commentIndex + i, modifiedComments.get(i));
                panel.getCommandListModel().add(commentIndex + i, modifiedComments.get(i));
            }
            
            // Update the selection to the first comment command
            JList<EventCommand> commandList = panel.getCommandList();
            if (commentIndex < panel.getCommandListModel().getSize()) {
                commandList.setSelectedIndex(commentIndex);
            }
            
            return true;
        }
        return false;
    }

    private boolean handleScriptEditor(EventCommand scriptCommand) {
        EventPage currentPage = getCurrentPage();
        if (currentPage == null) return false;
        
        List<EventCommand> commands = currentPage.getCommands();
        int scriptIndex = findCommandIndex(commands, scriptCommand);
        if (scriptIndex == -1) {
            System.err.println("Could not find Script command in command list");
            return false;
        }
        
        // Collect the script command (355) and all following continuation commands (655)
        List<EventCommand> scriptCommands = new ArrayList<>();
        scriptCommands.add(commands.get(scriptIndex));
        
        // Add all following 655 commands
        int nextIndex = scriptIndex + 1;
        while (nextIndex < commands.size() && commands.get(nextIndex).getCode() == SCRIPT_CONTINUATION) {
            scriptCommands.add(commands.get(nextIndex));
            nextIndex++;
        }
        
        // Open the script editor
        ScriptEditorDialog dialog = new ScriptEditorDialog(panel.getParentDialog(), scriptCommands);
        dialog.setVisible(true);
        
        if (dialog.isCommandModified()) {
            List<EventCommand> modifiedScripts = dialog.getModifiedScriptCommands();
            
            // Remove the old script commands from the list
            for (int i = 0; i < scriptCommands.size(); i++) {
                commands.remove(scriptIndex);
                panel.getCommandListModel().remove(scriptIndex);
            }
            
            // Insert the new script commands
            for (int i = 0; i < modifiedScripts.size(); i++) {
                commands.add(scriptIndex + i, modifiedScripts.get(i));
                panel.getCommandListModel().add(scriptIndex + i, modifiedScripts.get(i));
            }
            
            // Update the selection to the first script command
            JList<EventCommand> commandList = panel.getCommandList();
            if (scriptIndex < panel.getCommandListModel().getSize()) {
                commandList.setSelectedIndex(scriptIndex);
            }
            
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
        if (setMoveRouteIndex >= commands.size() || commands.get(setMoveRouteIndex).getCode() != SET_MOVEMENT_ROUTE) {
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