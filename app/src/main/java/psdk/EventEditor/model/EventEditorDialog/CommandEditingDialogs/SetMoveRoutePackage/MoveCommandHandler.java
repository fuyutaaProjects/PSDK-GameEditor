package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRoutePackage;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;

/**
 * Handles move command insertion and editing operations
 */
public class MoveCommandHandler {

    private final JDialog parentDialog;
    
    public MoveCommandHandler(JDialog parentDialog) {
        this.parentDialog = parentDialog;
    }

    /**
     * Insert a command from button click with parameter prompts
     */
    public JSONArray insertCommandFromButton(int moveCode, String commandName) {
        JSONArray moveParams = new JSONArray();

        switch (commandName) {
            case "Jump...":
                return handleJumpCommand(moveParams);
            case "Wait...":
                return handleWaitCommand(moveParams);
            case "Switch ON...":
                return handleSwitchOnCommand(moveParams);
            case "Switch OFF...":
                return handleSwitchOffCommand(moveParams);
            case "Change Speed...":
                return handleChangeSpeedCommand(moveParams);
            case "Change Freq...":
                return handleChangeFreqCommand(moveParams);
            case "Change Opacity...":
                return handleChangeOpacityCommand(moveParams);
            case "Change Blending...":
                return handleChangeBlendingCommand(moveParams);
            case "Play SE...":
                return handlePlaySECommand(moveParams);
            case "Script...":
                return handleScriptCommand(moveParams);
            default:
                return moveParams;
        }
    }

    private JSONArray handleJumpCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter X,Y offsets for Jump (e.g., 10,0):");
            if (input != null && !input.isEmpty()) {
                String[] parts = input.split(",");
                if (parts.length == 2) {
                    moveParams.put(Integer.parseInt(parts[0].trim()));
                    moveParams.put(Integer.parseInt(parts[1].trim()));
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid X,Y format. Using default [0,0].", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(0).put(0);
        }
        return moveParams;
    }

    private JSONArray handleWaitCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter wait frames (e.g., 4):");
            if (input != null && !input.isEmpty()) {
                moveParams.put(Integer.parseInt(input.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid frame number. Using default [0].", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(0);
        }
        return moveParams;
    }

    private JSONArray handleSwitchOnCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter Switch ID to turn ON (e.g., 1):");
            if (input != null && !input.isEmpty()) {
                moveParams.put(Integer.parseInt(input.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid Switch ID. Using default [1].", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(1);
        }
        return moveParams;
    }

    private JSONArray handleSwitchOffCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter Switch ID to turn OFF (e.g., 1):");
            if (input != null && !input.isEmpty()) {
                moveParams.put(Integer.parseInt(input.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid Switch ID. Using default [1].", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(1);
        }
        return moveParams;
    }

    private JSONArray handleChangeSpeedCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter Speed (1-6):");
            if (input != null && !input.isEmpty()) {
                moveParams.put(Integer.parseInt(input.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid Speed. Using default [3].", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(3);
        }
        return moveParams;
    }

    private JSONArray handleChangeFreqCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter Frequency (1-6):");
            if (input != null && !input.isEmpty()) {
                moveParams.put(Integer.parseInt(input.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid Frequency. Using default [3].", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(3);
        }
        return moveParams;
    }

    private JSONArray handleChangeOpacityCommand(JSONArray moveParams) {
        try {
            String input = JOptionPane.showInputDialog(parentDialog, "Enter Opacity (0-255):");
            if (input != null && !input.isEmpty()) {
                moveParams.put(Integer.parseInt(input.trim()));
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid Opacity. Using default (255).", "Error", JOptionPane.ERROR_MESSAGE);
            moveParams.put(255);
        }
        return moveParams;
    }

    private JSONArray handleChangeBlendingCommand(JSONArray moveParams) {
        String[] blendingOptions = {"Normal", "Add", "Subtract"};
        String selectedBlending = (String) JOptionPane.showInputDialog(
            parentDialog,
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
        return moveParams;
    }

    private JSONArray handlePlaySECommand(JSONArray moveParams) {
        String seName = JOptionPane.showInputDialog(parentDialog, "Enter SE Name (e.g., 'Absorb'):");
        if (seName == null) seName = "";
        try {
            String volumeStr = JOptionPane.showInputDialog(parentDialog, "Enter Volume (0-100, default 100):");
            int volume = (volumeStr != null && !volumeStr.isEmpty()) ? Integer.parseInt(volumeStr) : 100;
            String pitchStr = JOptionPane.showInputDialog(parentDialog, "Enter Pitch (50-150, default 100):");
            int pitch = (pitchStr != null && !pitchStr.isEmpty()) ? Integer.parseInt(pitchStr) : 100;

            JSONObject seJson = new JSONObject();
            seJson.put("name", seName);
            seJson.put("volume", volume);
            seJson.put("pitch", pitch);
            moveParams.put(seJson);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(parentDialog, "Invalid number for Volume/Pitch. Using defaults.", "Error", JOptionPane.ERROR_MESSAGE);
            try {
                JSONObject seJson = new JSONObject();
                seJson.put("name", seName);
                seJson.put("volume", 100);
                seJson.put("pitch", 100);
                moveParams.put(seJson);
            } catch (JSONException ex) {
                System.err.println("Error creating default SE JSON: " + ex.getMessage());
            }
        } catch (JSONException e) {
            System.err.println("Error creating SE JSON: " + e.getMessage());
        }
        return moveParams;
    }

    private JSONArray handleScriptCommand(JSONArray moveParams) {
        String script = JOptionPane.showInputDialog(parentDialog, "Enter Script Line:");
        moveParams.put(script != null ? script : "");
        return moveParams;
    }

    /**
     * Edit editable move command parameters
     */
    public boolean editMoveCommandParameters(JSONObject innerMoveCmdJson, int commandCode, String commandName, int indexInList) {
        if (!MoveCommandRegistry.isCommandEditable(commandCode)) {
            System.out.println("Command " + commandName + " (Code: " + commandCode + ") is not editable.");
            return false;
        }

        try {
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

            int result = JOptionPane.showConfirmDialog(parentDialog,
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
                    System.out.println("Parameters updated for command at index " + indexInList + ": " + newParams.toString());
                    return true;
                } catch (JSONException ex) {
                    JOptionPane.showMessageDialog(parentDialog, "Invalid JSON Array format for parameters. Please use: [] or [param1, param2, ...]", "Input Error", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Invalid JSON input for parameters: " + ex.getMessage());
                }
            }
        } catch (JSONException e) {
            JOptionPane.showMessageDialog(parentDialog, "Error editing move command: " + e.getMessage(), "Edit Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error editing inner move command: " + e.getMessage());
        }
        return false;
    }
}