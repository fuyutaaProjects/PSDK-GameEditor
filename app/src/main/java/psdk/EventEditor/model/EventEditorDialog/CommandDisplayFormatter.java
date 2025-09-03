package psdk.EventEditor.model.EventEditorDialog;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;
import psdk.EventEditor.model.EventCommand;

public class CommandDisplayFormatter {

    public String getCommandDisplayName(EventCommand command) {
        switch (command.getCode()) {
            // Basic commands
            case 101: return formatShowText(command);
            case 102: return formatShowChoices(command);
            case 103: return formatInputNumber(command);
            case 104: return formatChangeItems(command);
            case 105: return formatChangeGold(command);
            case 106: return formatChangeVariables(command);
            case 107: return formatChangeSwitches(command);
            case 108: return formatComment(command);
            case 111: return formatConditionalBranch(command);
            case 112: return "Loop";
            case 113: return "Break Loop";
            case 115: return "Exit Event Processing";
            case 116: return "Erase Event";
            case 123: return formatControlSelfSwitch(command);
            case 132: return formatChangeBattleBGM(command);
            
            // Map and player commands
            case 201: return formatTransferPlayer(command);
            case 202: return formatSetEventLocation(command);
            case 203: return formatScrollMap(command);
            case 204: return formatChangeMapSettings(command);
            case 209: return formatSetMovementRoute(command);
            case 210: return "Wait for Move's Completion";
            case 211: return "Change Tileset...";
            case 212: return "Change Parallax...";
            case 221: return "Wait...";
            case 224: return formatScreenFlash(command);
            case 225: return formatScreenShake(command);
            
            // Audio/Visual commands
            case 230: return "Play BGM...";
            case 231: return formatShowPicture(command);
            case 232: return "Play ME...";
            case 233: return "Play SE...";
            case 234: return "Stop SE...";
            case 235: return formatErasePicture(command);
            case 241: return formatPlayBGM(command);
            case 242: return formatFadeOutBGM(command);
            case 245: return formatPlayBGS(command);
            case 246: return formatFadeOutBGS(command);
            case 250: return formatPlaySE(command);
            case 251: return "Stop SE";
            case 355: return formatScript(command);
            
            // Continuation commands
            case 402: return formatWhenChoice(command);
            case 404: return "End Choice";
            case 408: return formatCommentContinuation(command);
            case 411: return "Else";
            case 412: return "Branch End";
            case 655: return formatScriptContinuation(command);
            
            // Movement commands
            case 509: return formatMovementCommand(command);
            
            case 0: return "End";
            default: return formatUnknownCommand(command);
        }
    }

    // Basic command formatters
    private String formatShowText(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                String text = params.getString(0);
                text = text.replace("\n", " ").replace("\r", "");
                if (text.length() > 50) {
                    text = text.substring(0, 47) + "...";
                }
                return "Show Text: \"" + text + "\"";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Show Text";
    }

    private String formatShowChoices(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                JSONArray choices = params.getJSONArray(0);
                StringBuilder choicesText = new StringBuilder();
                for (int i = 0; i < Math.min(choices.length(), 3); i++) {
                    if (i > 0) choicesText.append(", ");
                    choicesText.append("\"").append(choices.getString(i)).append("\"");
                }
                if (choices.length() > 3) {
                    choicesText.append(", ...");
                }
                return "Show Choices: " + choicesText.toString();
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Show Choices";
    }

    private String formatInputNumber(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 2) {
                int variableId = params.getInt(0);
                int digits = params.getInt(1);
                return "Input Number: Variable " + String.format("%04d", variableId) + " (" + digits + " digits)";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Input Number";
    }

    private String formatChangeItems(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 3) {
                int itemId = params.getInt(0);
                int operation = params.getInt(1);
                int amount = params.getInt(2);
                String opText = (operation == 0) ? "+" : "-";
                return "Change Items: Item " + String.format("%03d", itemId) + " " + opText + amount;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Change Items";
    }

    private String formatChangeGold(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 2) {
                int operation = params.getInt(0);
                int amount = params.getInt(1);
                String opText = (operation == 0) ? "+" : "-";
                return "Change Gold: " + opText + amount;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Change Gold";
    }

    private String formatChangeVariables(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 5) {
                int startId = params.getInt(0);
                int endId = params.getInt(1);
                int operation = params.getInt(2);
                int operandType = params.getInt(3);
                int value = params.getInt(4);
                
                String varRange = (startId == endId) ? 
                    String.format("%04d", startId) : 
                    String.format("%04d-%04d", startId, endId);
                    
                String opText = getOperationText(operation);
                String valueText = (operandType == 1) ? 
                    "Variable " + String.format("%04d", value) : 
                    String.valueOf(value);
                
                return "Change Variables: " + varRange + " " + opText + " " + valueText;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Change Variables";
    }

    private String formatChangeSwitches(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 3) {
                int startId = params.getInt(0);
                int endId = params.getInt(1);
                int operation = params.getInt(2);
                
                String switchRange = (startId == endId) ? 
                    String.format("%04d", startId) : 
                    String.format("%04d-%04d", startId, endId);
                    
                String opText = (operation == 0) ? "ON" : "OFF";
                return "Change Switches: " + switchRange + " = " + opText;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Change Switches";
    }

    private String formatComment(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                String comment = params.getString(0);
                if (comment.length() > 50) {
                    comment = comment.substring(0, 47) + "...";
                }
                return "Comment: " + comment;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Comment";
    }

    private String formatConditionalBranch(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 1) {
                int conditionType = params.getInt(0);
                StringBuilder conditionText = new StringBuilder("Conditional Branch: ");
                
                switch (conditionType) {
                    case 0: // Switch
                        if (params.length() >= 3) {
                            int switchId = params.getInt(1);
                            int expectedValue = params.getInt(2);
                            conditionText.append("Switch ").append(String.format("%04d", switchId))
                                       .append(" == ").append(expectedValue == 0 ? "ON" : "OFF");
                        }
                        break;
                    case 1: // Variable
                        if (params.length() >= 4) {
                            int varId = params.getInt(1);
                            int comparison = params.getInt(2);
                            int value = params.getInt(3);
                            String[] operators = {"==", ">=", "<=", ">", "<", "!="};
                            String op = (comparison < operators.length) ? operators[comparison] : "?";
                            conditionText.append("Variable ").append(String.format("%04d", varId))
                                       .append(" ").append(op).append(" ").append(value);
                        }
                        break;
                    case 2: // Self Switch
                        if (params.length() >= 3) {
                            String selfSwitch = params.getString(1);
                            int expectedValue = params.getInt(2);
                            conditionText.append("Self Switch ").append(selfSwitch)
                                       .append(" == ").append(expectedValue == 0 ? "ON" : "OFF");
                        }
                        break;
                    default:
                        conditionText.append("Unknown condition");
                        break;
                }
                return conditionText.toString();
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Conditional Branch";
    }

    private String formatControlSelfSwitch(EventCommand command) {
        try {
            String selfSwitchChar = command.getParameters().getString(0);
            int opId = command.getParameters().getInt(1);
            String operation = (opId == 0) ? "ON" : "OFF";
            return "Control Self Switch: " + selfSwitchChar + " = " + operation;
        } catch (JSONException | IndexOutOfBoundsException e) {
            System.err.println("Error parsing parameters for Control Self Switch (123): " + e.getMessage());
            return "Control Self Switch (Error: " + command.getParameters().toString() + ")";
        }
    }

    // Audio/Visual formatters
    private String formatChangeBattleBGM(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                JSONObject bgm = params.getJSONObject(0);
                String name = bgm.has("name") ? bgm.getString("name") : "";
                int volume = bgm.has("volume") ? bgm.getInt("volume") : 100;
                int pitch = bgm.has("pitch") ? bgm.getInt("pitch") : 100;
                return "Change Battle BGM: \"" + name + "\", " + volume + ", " + pitch;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Change Battle BGM";
    }

    private String formatPlayBGM(EventCommand command) {
        return formatAudioCommand(command, "Play BGM");
    }

    private String formatPlayBGS(EventCommand command) {
        return formatAudioCommand(command, "Play BGS");
    }

    private String formatPlaySE(EventCommand command) {
        return formatAudioCommand(command, "Play SE");
    }

    private String formatFadeOutBGM(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 1) {
                int seconds = params.getInt(0);
                return "Fade Out BGM: " + seconds + "s";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Fade Out BGM";
    }

    private String formatAudioCommand(EventCommand command, String commandName) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                JSONObject audio = params.getJSONObject(0);
                String name = audio.has("name") ? audio.getString("name") : "";
                int volume = audio.has("volume") ? audio.getInt("volume") : 100;
                int pitch = audio.has("pitch") ? audio.getInt("pitch") : 100;
                return commandName + ": \"" + name + "\", " + volume + ", " + pitch;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return commandName;
    }

    private String formatFadeOutBGS(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 1) {
                int seconds = params.getInt(0);
                return "Fade Out BGS: " + seconds + "s";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Fade Out BGS";
    }

    private String formatShowPicture(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 2) {
                int pictureId = params.getInt(0);
                String pictureName = params.getString(1);
                return "Show Picture: #" + pictureId + " \"" + pictureName + "\"";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Show Picture";
    }

    private String formatErasePicture(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 1) {
                int pictureId = params.getInt(0);
                return "Erase Picture: #" + pictureId;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Erase Picture";
    }

    // Map/Player formatters
    private String formatTransferPlayer(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 4) {
                int mapId = params.getInt(1);
                int x = params.getInt(2);
                int y = params.getInt(3);
                return "Transfer Player: Map " + String.format("%03d", mapId) + " (" + x + "," + y + ")";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Transfer Player";
    }

    private String formatSetEventLocation(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 3) {
                int eventId = params.getInt(0);
                int x = params.getInt(1);
                int y = params.getInt(2);
                String eventName = (eventId == -1) ? "Player" : (eventId == 0 ? "This Event" : "Event " + eventId);
                return "Set Event Location: " + eventName + " to (" + x + "," + y + ")";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Set Event Location";
    }

    private String formatScrollMap(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 3) {
                int direction = params.getInt(0);
                int distance = params.getInt(1);
                int speed = params.getInt(2);
                String directionName = getDirectionName(direction);
                return "Scroll Map: " + directionName + " " + distance + " tiles (Speed " + speed + ")";
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Scroll Map";
    }
    
    private String formatScreenFlash(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            JSONObject flashData = (JSONObject) params.get(0);
            int duration = params.getInt(1);
            
            int red = flashData.getInt("red");
            int green = flashData.getInt("green");
            int blue = flashData.getInt("blue");
            int alpha = flashData.getInt("alpha");
            
            return "Screen Flash: RGB(" + red + "," + green + "," + blue + ") " + alpha + " duration:" + duration + "s";
            
        } catch (JSONException e) {
            System.err.println("Error parsing Screen Flash: " + e.getMessage());
            return "Screen Flash";
        }
    }

    private String formatSetMovementRoute(EventCommand command) {
        StringBuilder routeDisplayName = new StringBuilder("Set Move Route: ");
        try {
            JSONArray params = command.getParameters();
            int targetId = params.getInt(0);
            if (targetId == -1) {
                routeDisplayName.append("Player");
            } else if (targetId == 0) { 
                routeDisplayName.append("This Event");
            } else {
                routeDisplayName.append("Event ").append(targetId);
            }
        } catch (JSONException | IndexOutOfBoundsException e) {
            System.err.println("Error parsing parameters for Set Movement Route (209): " + e.getMessage());
            routeDisplayName.append(" (Error: ").append(command.getParameters().toString()).append(")");
        }
        return routeDisplayName.toString();
    }

    private String formatChangeMapSettings(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            
            if (params.length() >= 8) {
                String fogName = params.getString(1);
                int opacity = params.getInt(3);
                int zoom = params.getInt(5);
                return "Change Map Settings (Fog): \"" + fogName + "\" opacity:" + opacity + " zoom:" + zoom;
            }
        } catch (JSONException e) {
            System.err.println("JSONException in formatChangeMapSettings: " + e.getMessage());
        }
        return "Change Map Settings (Fog)";
    }

    private String formatScreenShake(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 3) {
                int power = params.getInt(0);
                int speed = params.getInt(1);
                int duration = params.getInt(2);
                return "Screen Shake: power:" + power + " speed:" + speed + " duration:" + duration;
            }
        } catch (JSONException e) {
            // Fall back to showing parameters
        }
        return "Screen Shake" + (command.getParameters().length() > 0 ? " (" + command.getParameters().toString() + ")" : "");
    }

    private String formatScript(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                String script = params.getString(0);
                if (script.length() > 50) {
                    script = script.substring(0, 47) + "...";
                }
                return "Script: " + script;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "Script";
    }

    // Continuation command formatters
    private String formatWhenChoice(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() >= 2) {
                int choiceIndex = params.getInt(0);
                String choiceText = params.getString(1);
                return "When [" + choiceIndex + "]: " + choiceText;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return "When [Choice]";
    }

    private String formatCommentContinuation(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                String comment = params.getString(0);
                if (comment.length() > 50) {
                    comment = comment.substring(0, 47) + "...";
                }
                return ": " + comment;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return ": (comment continuation)";
    }

    private String formatScriptContinuation(EventCommand command) {
        try {
            JSONArray params = command.getParameters();
            if (params.length() > 0) {
                String script = params.getString(0);
                if (script.length() > 50) {
                    script = script.substring(0, 47) + "...";
                }
                return ": " + script;
            }
        } catch (JSONException e) {
            // Fall back to basic display
        }
        return ": (script continuation)";
    }

    // Movement command formatter (kept large due to complexity)
    private String formatMovementCommand(EventCommand command) {
        // Check if this is a dummy command from MoveCommandQuickInsertDialog
        if (command.getParameters().length() == 0) {
            try {
                Integer.parseInt(command.getIndent());
                // Regular 509 with empty params, fall through to error handling
            } catch (NumberFormatException e) {
                // 'indent' holds the command name
                return command.getIndent();
            }
        }

        try {
            JSONObject moveObject = command.getParameters().getJSONObject(0);
            int moveCode = moveObject.getInt("code"); 
            return formatMovementByCode(moveCode, moveObject);
        } catch (JSONException | IndexOutOfBoundsException e) {
            System.err.println("Error parsing parameters for Movement Command (509): " + e.getMessage());
            return "Movement Command (509) (Error: " + command.getParameters().toString() + ")";
        }
    }

    private String formatMovementByCode(int moveCode, JSONObject moveObject) throws JSONException {
        switch (moveCode) {
            case 1: return "Move Down";
            case 2: return "Move Left";
            case 3: return "Move Right";
            case 4: return "Move Up";
            case 5: return "Move Lower Left";
            case 6: return "Move Lower Right";
            case 7: return "Move Upper Left";
            case 8: return "Move Upper Right";
            case 9: return "Move at Random";
            case 10: return "Move toward Player";
            case 11: return "Move away from Player";
            case 12: return "1 Step Forward";
            case 13: return "1 Step Backward";
            case 14: return formatJumpCommand(moveObject);
            case 15: return formatWaitCommand(moveObject);
            case 16: return "Turn Down";
            case 17: return "Turn Left";
            case 18: return "Turn Right";
            case 19: return "Turn Up";
            case 20: return "Turn 90 Right";
            case 21: return "Turn 90 Left";
            case 22: return "Turn 180";
            case 23: return "Turn 90 Right or Left";
            case 24: return "Turn at Random";
            case 25: return "Turn Toward Player";
            case 26: return "Turn Away From Player";
            case 27: return formatSwitchCommand(moveObject, "Switch ON");
            case 28: return formatSwitchCommand(moveObject, "Switch OFF");
            case 29: return formatChangeCommand(moveObject, "Change Speed");
            case 30: return formatChangeCommand(moveObject, "Change Freq");
            case 31: return "Move Animation ON";
            case 32: return "Move Animation OFF";
            case 33: return "Stop Animation ON";
            case 34: return "Stop Animation OFF";
            case 35: return "Direction Fix ON";
            case 36: return "Direction Fix OFF";
            case 37: return "Through ON";
            case 38: return "Through OFF";
            case 39: return "Always on Top ON";
            case 40: return "Always on Top OFF";
            case 41: return formatGraphicCommand(moveObject);
            case 42: return formatChangeCommand(moveObject, "Change Opacity");
            case 43: return formatBlendingCommand(moveObject);
            case 44: return formatSECommand(moveObject);
            case 45: return formatScriptCommand(moveObject);
            case 0: return "End of Route";
            default: return "Unknown Movement (" + moveCode + ")";
        }
    }

    // Movement command helpers
    private String formatJumpCommand(JSONObject moveObject) throws JSONException {
        String result = "Jump";
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 2) {
            int jumpX = moveObject.getJSONArray("parameters").getInt(0);
            int jumpY = moveObject.getJSONArray("parameters").getInt(1);
            result += ": " + (jumpX >= 0 ? "+" : "") + jumpX + "," + (jumpY >= 0 ? "+" : "") + jumpY;
        } else {
            result += ": (No parameters)";
        }
        return result;
    }

    private String formatWaitCommand(JSONObject moveObject) throws JSONException {
        String result = "Wait";
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 1) {
            result += ": " + moveObject.getJSONArray("parameters").getInt(0) + " frame(s)";
        } else {
            result += ": (No parameters)";
        }
        return result;
    }

    private String formatSwitchCommand(JSONObject moveObject, String commandName) throws JSONException {
        String result = commandName;
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 1) {
            result += ": " + String.format("%04d", moveObject.getJSONArray("parameters").getInt(0));
        }
        return result;
    }

    private String formatChangeCommand(JSONObject moveObject, String commandName) throws JSONException {
        String result = commandName;
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 1) {
            result += ": " + moveObject.getJSONArray("parameters").getInt(0);
        }
        return result;
    }

    private String formatGraphicCommand(JSONObject moveObject) throws JSONException {
        String result = "Graphic";
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 4) {
            String charName = moveObject.getJSONArray("parameters").getString(0);
            result += charName.isEmpty() ? ": (None)" : ": " + charName;
        }
        return result;
    }

    private String formatBlendingCommand(JSONObject moveObject) throws JSONException {
        String result = "Change Blending";
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 1) {
            int blendType = moveObject.getJSONArray("parameters").getInt(0);
            switch (blendType) {
                case 0: result += ": Normal"; break;
                case 1: result += ": Add"; break;
                case 2: result += ": Subtract"; break;
                default: result += ": " + blendType; break;
            }
        }
        return result;
    }

    private String formatSECommand(JSONObject moveObject) throws JSONException {
        String result = "SE";
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 1) {
            try {
                JSONObject seData = moveObject.getJSONArray("parameters").getJSONObject(0);
                String seName = seData.has("name") ? seData.getString("name") : "";
                int seVolume = seData.has("volume") ? seData.getInt("volume") : 0;
                int sePitch = seData.has("pitch") ? seData.getInt("pitch") : 0;
                result += ": \"" + seName + "\", " + seVolume + ", " + sePitch;
            } catch (JSONException seEx) {
                System.err.println("Error parsing SE parameters for 509 (Code 44): " + seEx.getMessage());
                result += ": (Error)";
            }
        }
        return result;
    }

    private String formatScriptCommand(JSONObject moveObject) throws JSONException {
        String result = "Script";
        if (moveObject.has("parameters") && moveObject.getJSONArray("parameters").length() >= 1) {
            result += ": " + moveObject.getJSONArray("parameters").getString(0);
        }
        return result;
    }

    private String formatUnknownCommand(EventCommand command) {
        return "Code " + command.getCode() + 
               (command.getParameters().length() > 0 ? " (" + command.getParameters().toString() + ")" : "");
    }

    // Utility methods
    private String getOperationText(int operation) {
        switch (operation) {
            case 0: return "=";
            case 1: return "+=";
            case 2: return "-=";
            case 3: return "*=";
            case 4: return "/=";
            case 5: return "%=";
            default: return "?=";
        }
    }

    private String getDirectionName(int direction) {
        switch (direction) {
            case 2: return "Down";
            case 4: return "Left";
            case 6: return "Right";
            case 8: return "Up";
            default: return "Direction " + direction;
        }
    }
}