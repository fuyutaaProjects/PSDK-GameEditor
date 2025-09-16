package psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage;

import javax.swing.JList;
import psdk.EventEditor.model.EventCommand;

public class IndentCalculator {

    /**
     * Calculates the effective indent level for a command, taking into account
     * movement commands that should be indented relative to their parent Set Move Route.
     */
    public int calculateIndent(EventCommand command, JList<?> list, int currentIndex) {
        int baseIndentLevel = parseIndentLevel(command);

        int commandCode = command.getCode();
        
        // Commands that are continuation of other commands (should be indented)
        if (isContinuationCommand(commandCode)) {
            return calculateContinuationIndent(command, list, currentIndex, baseIndentLevel);
        }

        // Special handling for commands inside choice branches
        if (isInsideChoiceBranch(list, currentIndex)) {
            return calculateChoiceBranchIndent(list, currentIndex);
        }

        // Special handling for movement commands (code 509)
        if (commandCode == 509) {
            return calculateMovementCommandIndent(command, list, currentIndex, baseIndentLevel);
        }

        return baseIndentLevel;
    }

    private int parseIndentLevel(EventCommand command) {
        try {
            return Integer.parseInt(String.valueOf(command.getIndent()));
        } catch (NumberFormatException e) {
            // If indent is not a number, it's likely the command name from MoveCommandQuickInsertDialog
            return 0; 
        }
    }

    private boolean isContinuationCommand(int commandCode) {
        return commandCode == 408 || commandCode == 655 || commandCode == 402;
    }

    private int calculateContinuationIndent(EventCommand command, JList<?> list, int currentIndex, int baseIndentLevel) {
        int commandCode = command.getCode();
        
        // Look backwards to find the parent command
        for (int i = currentIndex - 1; i >= 0; i--) {
            Object prevItem = list.getModel().getElementAt(i);
            if (prevItem instanceof EventCommand) {
                EventCommand prevCommand = (EventCommand) prevItem;
                int prevCode = prevCommand.getCode();
                
                // Direct parent-child relationships
                if (isDirectParentChild(commandCode, prevCode)) {
                    return parseIndentLevel(prevCommand) + 1;
                }
                
                // Continuation chain handling
                if (isContinuationChain(commandCode, prevCode)) {
                    int parentIndent = parseIndentLevel(prevCommand);
                    return Math.max(parentIndent, baseIndentLevel + 1);
                }
            }
        }
        return baseIndentLevel;
    }

    private boolean isDirectParentChild(int childCode, int parentCode) {
        return (childCode == 408 && parentCode == 108) ||  // comment continuation
               (childCode == 655 && parentCode == 355) ||  // script continuation
               (childCode == 402 && parentCode == 102);    // choice continuation
    }

    private boolean isContinuationChain(int commandCode, int prevCode) {
        return (commandCode == 408 && (prevCode == 108 || prevCode == 408)) ||
               (commandCode == 655 && (prevCode == 355 || prevCode == 655)) ||
               (commandCode == 402 && (prevCode == 102 || prevCode == 402));
    }

    /**
     * Checks if the current command is inside a choice branch (between When [X] and End Choice)
     */
    private boolean isInsideChoiceBranch(JList<?> list, int currentIndex) {
        int commandCode = ((EventCommand) list.getModel().getElementAt(currentIndex)).getCode();
        
        // Don't indent the When [X] commands themselves or End Choice
        if (commandCode == 402 || commandCode == 404) {
            return false;
        }
        
        return hasWhenCommandBefore(list, currentIndex) && hasEndChoiceAfter(list, currentIndex);
    }

    private boolean hasWhenCommandBefore(JList<?> list, int currentIndex) {
        for (int i = currentIndex - 1; i >= 0; i--) {
            Object item = list.getModel().getElementAt(i);
            if (item instanceof EventCommand) {
                EventCommand cmd = (EventCommand) item;
                if (cmd.getCode() == 402) {
                    return true;
                } else if (cmd.getCode() == 404 || cmd.getCode() == 102) {
                    break; // Hit an End Choice or another Show Choices
                }
            }
        }
        return false;
    }

    private boolean hasEndChoiceAfter(JList<?> list, int currentIndex) {
        for (int i = currentIndex + 1; i < list.getModel().getSize(); i++) {
            Object item = list.getModel().getElementAt(i);
            if (item instanceof EventCommand) {
                EventCommand cmd = (EventCommand) item;
                if (cmd.getCode() == 404) {
                    return true;
                } else if (cmd.getCode() == 102) {
                    break; // Hit another Show Choices
                }
            }
        }
        return false;
    }

    private int calculateChoiceBranchIndent(JList<?> list, int currentIndex) {
        // Find the When [X] command that starts this branch
        for (int i = currentIndex - 1; i >= 0; i--) {
            Object prevItem = list.getModel().getElementAt(i);
            if (prevItem instanceof EventCommand) {
                EventCommand prevCommand = (EventCommand) prevItem;
                if (prevCommand.getCode() == 402) { // When [X]
                    return parseIndentLevel(prevCommand) + 1;
                }
            }
        }
        return 0; // Fallback
    }

    private int calculateMovementCommandIndent(EventCommand command, JList<?> list, int currentIndex, int baseIndentLevel) {
        // Look backwards to find the most recent Set Move Route command (code 209)
        for (int i = currentIndex - 1; i >= 0; i--) {
            Object prevItem = list.getModel().getElementAt(i);
            if (prevItem instanceof EventCommand) {
                EventCommand prevCommand = (EventCommand) prevItem;
                
                if (prevCommand.getCode() == 209) {
                    // Found a Set Move Route command, indent the movement command relative to it
                    return parseIndentLevel(prevCommand) + 1;
                }
                
                // If we encounter another non-509 command at the same or lower indent level,
                // we've likely moved out of the current move route context
                int prevIndent = parseIndentLevel(prevCommand);
                if (prevCommand.getCode() != 509 && prevIndent <= baseIndentLevel) {
                    break;
                }
            }
        }
        return baseIndentLevel;
    }
}
