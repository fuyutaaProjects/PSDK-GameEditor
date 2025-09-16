package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRoutePackage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for move command codes, names, and editability settings
 */
public class MoveCommandRegistry {
    
    public static final Map<String, Integer> MOVE_COMMAND_CODES = new LinkedHashMap<>();
    public static final Map<Integer, String> MOVE_COMMAND_NAMES_BY_CODE = new LinkedHashMap<>();
    public static final Map<Integer, Boolean> MOVE_COMMAND_EDITABILITY = new LinkedHashMap<>();

    static {
        initializeCommands();
        initializeEditability();
    }

    private static void initializeCommands() {
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
    }

    private static void initializeEditability() {
        for (Integer code : MOVE_COMMAND_NAMES_BY_CODE.keySet()) {
            MOVE_COMMAND_EDITABILITY.put(code, false);
        }

        // Set editable commands
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

    public static boolean isCommandEditable(int code) {
        return MOVE_COMMAND_EDITABILITY.getOrDefault(code, false);
    }

    public static String getCommandName(int code) {
        return MOVE_COMMAND_NAMES_BY_CODE.getOrDefault(code, "Code " + code);
    }
}