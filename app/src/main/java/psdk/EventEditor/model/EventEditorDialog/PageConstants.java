package psdk.EventEditor.model.EventEditorDialog;

import java.awt.*;

/**
 * Constants used throughout the page properties components
 */
public class PageConstants {
    
    // Colors
    public static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    // Dimensions
    public static final Dimension GRAPHIC_PANEL_SIZE = new Dimension(200, 200);
    public static final Dimension OPTIONS_PANEL_SIZE = new Dimension(150, 150);
    public static final Dimension CHARACTER_PREVIEW_SIZE = new Dimension(96, 96);
    public static final Dimension BUTTON_SIZE = new Dimension(30, 25);
    
    // Direction mappings for RPG Maker XP
    public static final String[] DIRECTION_ARROWS = {"←", "→", "↓", "↑"};
    public static final int[] DIRECTION_VALUES = {8, 6, 2, 4};
    
    // Movement options
    public static final String[] MOVE_TYPES = {"Fixed", "Random", "Approach", "Custom"};
    public static final String[] MOVE_SPEEDS = {
        "1: Slowest", "2: Slower", "3: Slow", "4: Normal", "5: Fast", "6: Faster"
    };
    public static final String[] MOVE_FREQUENCIES = {
        "1: Lowest", "2: Lower", "3: Low", "4: Normal", "5: High"
    };
    
    // Variable comparison operators
    public static final String[] VARIABLE_COMPARISONS = {"=", "≥", "≤", ">", "<", "≠"};
    
    // Self switch options
    public static final String[] SELF_SWITCH_OPTIONS = {"A", "B", "C", "D"};
    
    // Trigger names
    public static final String[] TRIGGER_NAMES = {
        "Action Button", "Player Touch", "Event Touch", "Autorun", "Parallel Process"
    };
    
    // Default values
    public static final int DEFAULT_MOVE_SPEED_INDEX = 2; // "3: Slow"
    public static final int DEFAULT_MOVE_FREQ_INDEX = 2;  // "3: Low"
    public static final String DEFAULT_DIRECTION = "↓";
    public static final int DEFAULT_DIRECTION_VALUE = 2;
}