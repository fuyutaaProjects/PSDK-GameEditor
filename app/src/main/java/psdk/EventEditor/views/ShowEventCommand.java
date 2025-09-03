package psdk.EventEditor.views;

import psdk.EventEditor.model.EventCommand;

public class ShowEventCommand {
    

    /**
     * Static method to display event command details in terminal (simple version)
     */
    public static void displayEventCommandDetails(EventCommand command) {
        displayEventCommandDetails(command, 1);
    }
    

    /**
     * Static method to display event command details in terminal
     */
    public static void displayEventCommandDetails(EventCommand command, int commandNumber) {
        System.out.println("Command #" + commandNumber + ":");
        System.out.println("  Code: " + command.getCode() + " (" + getCommandName(command.getCode()) + ")");
        System.out.println("  Name: " + command.getName());
        System.out.println("  Indent: \"" + command.getIndent() + "\"");
        
        // Display parameters
        if (command.getParameters() != null && command.getParameters().length() > 0) {
            System.out.println("  Parameters (" + command.getParameters().length() + "):");
            for (int paramIndex = 0; paramIndex < command.getParameters().length(); paramIndex++) {
                try {
                    Object param = command.getParameters().get(paramIndex);
                    System.out.println("    [" + paramIndex + "] " + param + " (" + param.getClass().getSimpleName() + ")");
                } catch (Exception e) {
                    System.out.println("    [" + paramIndex + "] <error reading parameter>");
                }
            }
        } else {
            System.out.println("  Parameters: (none)");
        }
    }
    
    /**
     * Display command details with additional parameter interpretation
     */
    public static void displayEventCommandDetailsWithInterpretation(EventCommand command, int commandNumber) {
        displayEventCommandDetails(command, commandNumber);
        
        // Add interpretation for specific command types
        interpretCommandParameters(command);
    }
    
    /**
     * Interpret parameters for specific command types
     */
    private static void interpretCommandParameters(EventCommand command) {
        int code = command.getCode();
        
        switch (code) {
            case 101: // Show Text
                interpretShowTextParameters(command);
                break;
            case 102: // Show Choices
                interpretShowChoicesParameters(command);
                break;
            case 121: // Control Switches
                interpretControlSwitchesParameters(command);
                break;
            case 122: // Control Variables
                interpretControlVariablesParameters(command);
                break;
            case 201: // Transfer Player
                interpretTransferPlayerParameters(command);
                break;
            case 241: // Play BGM
                interpretPlayBGMParameters(command);
                break;
            case 355: // Script
                interpretScriptParameters(command);
                break;
            default:
                // No specific interpretation for this command type
                break;
        }
    }
    
    /**
     * Interpret Show Text command parameters
     */
    private static void interpretShowTextParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() > 0) {
            System.out.println("  >> Text: " + command.getParameters().optString(0, ""));
        }
    }
    
    /**
     * Interpret Show Choices command parameters
     */
    private static void interpretShowChoicesParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() > 0) {
            try {
                Object choicesParam = command.getParameters().get(0);
                if (choicesParam instanceof libs.json.JSONArray) {
                    libs.json.JSONArray choices = (libs.json.JSONArray) choicesParam;
                    System.out.println("  >> Choices:");
                    for (int i = 0; i < choices.length(); i++) {
                        System.out.println("    - " + choices.optString(i, ""));
                    }
                }
            } catch (Exception e) {
                System.out.println("  >> Error interpreting choices");
            }
        }
    }
    
    /**
     * Interpret Control Switches command parameters
     */
    private static void interpretControlSwitchesParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() >= 3) {
            try {
                int startSwitch = command.getParameters().getInt(0);
                int endSwitch = command.getParameters().getInt(1);
                int value = command.getParameters().getInt(2);
                
                System.out.println("  >> Switch Range: " + startSwitch + " to " + endSwitch);
                System.out.println("  >> Value: " + (value == 0 ? "OFF" : "ON"));
            } catch (Exception e) {
                System.out.println("  >> Error interpreting switch control");
            }
        }
    }
    
    /**
     * Interpret Control Variables command parameters
     */
    private static void interpretControlVariablesParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() >= 4) {
            try {
                int startVar = command.getParameters().getInt(0);
                int endVar = command.getParameters().getInt(1);
                int operation = command.getParameters().getInt(2);
                int operand = command.getParameters().getInt(3);
                
                System.out.println("  >> Variable Range: " + startVar + " to " + endVar);
                System.out.println("  >> Operation: " + getVariableOperationName(operation));
                System.out.println("  >> Operand: " + operand);
            } catch (Exception e) {
                System.out.println("  >> Error interpreting variable control");
            }
        }
    }
    
    /**
     * Interpret Transfer Player command parameters
     */
    private static void interpretTransferPlayerParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() >= 5) {
            try {
                int mapId = command.getParameters().getInt(1);
                int x = command.getParameters().getInt(2);
                int y = command.getParameters().getInt(3);
                int direction = command.getParameters().getInt(4);
                
                System.out.println("  >> Map ID: " + mapId);
                System.out.println("  >> Position: (" + x + ", " + y + ")");
                System.out.println("  >> Direction: " + getDirectionName(direction));
            } catch (Exception e) {
                System.out.println("  >> Error interpreting transfer player");
            }
        }
    }
    
    /**
     * Interpret Play BGM command parameters
     */
    private static void interpretPlayBGMParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() > 0) {
            try {
                Object bgmParam = command.getParameters().get(0);
                if (bgmParam instanceof libs.json.JSONObject) {
                    libs.json.JSONObject bgm = (libs.json.JSONObject) bgmParam;
                    System.out.println("  >> BGM Name: " + bgm.optString("name", ""));
                    System.out.println("  >> Volume: " + bgm.optInt("volume", 100));
                    System.out.println("  >> Pitch: " + bgm.optInt("pitch", 100));
                }
            } catch (Exception e) {
                System.out.println("  >> Error interpreting BGM");
            }
        }
    }
    
    /**
     * Interpret Script command parameters
     */
    private static void interpretScriptParameters(EventCommand command) {
        if (command.getParameters() != null && command.getParameters().length() > 0) {
            System.out.println("  >> Script: " + command.getParameters().optString(0, ""));
        }
    }
    
    /**
     * Returns a human-readable name for variable operations
     */
    private static String getVariableOperationName(int operation) {
        switch (operation) {
            case 0: return "Set";
            case 1: return "Add";
            case 2: return "Sub";
            case 3: return "Mul";
            case 4: return "Div";
            case 5: return "Mod";
            default: return "Unknown";
        }
    }
    
    /**
     * Returns a human-readable name for directions
     */
    private static String getDirectionName(int direction) {
        switch (direction) {
            case 2: return "Down";
            case 4: return "Left";
            case 6: return "Right";
            case 8: return "Up";
            default: return "Unknown";
        }
    }

    /**
     * Returns a human-readable name for common RPG Maker XP event command codes
     */
    public static String getCommandName(int code) {
        switch (code) {
            case 0: return "End";
            case 101: return "Show Text";
            case 102: return "Show Choices";
            case 103: return "Input Number";
            case 104: return "Change Text Options";
            case 105: return "Button Input Processing";
            case 106: return "Wait";
            case 108: return "Comment";
            case 111: return "Conditional Branch";
            case 112: return "Loop";
            case 113: return "Break Loop";
            case 115: return "Exit Event Processing";
            case 117: return "Call Common Event";
            case 118: return "Label";
            case 119: return "Jump to Label";
            case 121: return "Control Switches";
            case 122: return "Control Variables";
            case 123: return "Control Self Switch";
            case 124: return "Control Timer";
            case 125: return "Change Gold";
            case 126: return "Change Items";
            case 127: return "Change Weapons";
            case 128: return "Change Armor";
            case 129: return "Change Party Member";
            case 132: return "Change Battle BGM";
            case 133: return "Change Battle End ME";
            case 134: return "Change Save Access";
            case 135: return "Change Menu Access";
            case 136: return "Change Encounter";
            case 201: return "Transfer Player";
            case 202: return "Set Event Location";
            case 203: return "Scroll Map";
            case 204: return "Change Map Settings";
            case 205: return "Change Fog Color Tone";
            case 206: return "Change Fog Opacity";
            case 207: return "Show Animation";
            case 208: return "Change Transparent Flag";
            case 209: return "Set Move Route";
            case 210: return "Wait for Move's Completion";
            case 221: return "Prepare for Transition";
            case 222: return "Execute Transition";
            case 223: return "Change Screen Color Tone";
            case 224: return "Screen Flash";
            case 225: return "Screen Shake";
            case 231: return "Show Picture";
            case 232: return "Move Picture";
            case 233: return "Rotate Picture";
            case 234: return "Change Picture Color Tone";
            case 235: return "Erase Picture";
            case 236: return "Set Weather Effects";
            case 241: return "Play BGM";
            case 242: return "Fade Out BGM";
            case 243: return "Play BGS";
            case 244: return "Fade Out BGS";
            case 245: return "Memorize BGM/BGS";
            case 246: return "Restore BGM/BGS";
            case 247: return "Play ME";
            case 248: return "Play SE";
            case 249: return "Stop SE";
            case 301: return "Battle Processing";
            case 302: return "Shop Processing";
            case 303: return "Name Input Processing";
            case 311: return "Change HP";
            case 312: return "Change SP";
            case 313: return "Change State";
            case 314: return "Recover All";
            case 315: return "Change EXP";
            case 316: return "Change Level";
            case 317: return "Change Parameters";
            case 318: return "Change Skills";
            case 319: return "Change Equipment";
            case 320: return "Change Actor Name";
            case 321: return "Change Actor Class";
            case 322: return "Change Actor Graphic";
            case 331: return "Change Enemy HP";
            case 332: return "Change Enemy SP";
            case 333: return "Change Enemy State";
            case 334: return "Enemy Recover All";
            case 335: return "Enemy Appearance";
            case 336: return "Enemy Transform";
            case 337: return "Show Battle Animation";
            case 338: return "Deal Damage";
            case 339: return "Force Action";
            case 340: return "Abort Battle";
            case 351: return "Call Menu Screen";
            case 352: return "Call Save Screen";
            case 353: return "Game Over";
            case 354: return "Return to Title Screen";
            case 355: return "Script";
            case 401: return "Show Text (continued)";
            case 402: return "Show Choices (when [**])";
            case 403: return "Show Choices (when Cancel)";
            case 404: return "Input Number (continued)";
            case 408: return "Comment (continued)";
            case 411: return "Conditional Branch (else)";
            case 412: return "Loop (repeat)";
            case 413: return "Break Loop (continued)";
            case 509: return "Set Move Route (continued)";
            case 601: return "If Win";
            case 602: return "If Escape";
            case 603: return "If Lose";
            case 604: return "Battle Processing (End)";
            case 605: return "Shop Processing (continued)";
            case 655: return "Script (continued)";
            default: return "Unknown Command";
        }
    }
}