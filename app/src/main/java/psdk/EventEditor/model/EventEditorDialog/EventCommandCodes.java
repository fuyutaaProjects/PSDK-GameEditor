package psdk.EventEditor.model.EventEditorDialog;

/**
 * RPG Maker XP command codes
 * Used par CommandDisplayFormatter et CommandEditorManager
 */
public final class EventCommandCodes {
    
    // Disallow instantiation
    private EventCommandCodes() {}
    
    // Basic commands
    public static final int SHOW_TEXT = 101;
    public static final int SHOW_CHOICES = 102;
    public static final int INPUT_NUMBER = 103;
    public static final int CHANGE_ITEMS = 104;
    public static final int CHANGE_GOLD = 105;
    public static final int CHANGE_VARIABLES = 106;
    public static final int CHANGE_SWITCHES = 107;
    public static final int COMMENT = 108;
    public static final int CONDITIONAL_BRANCH = 111;
    public static final int LOOP = 112;
    public static final int BREAK_LOOP = 113;
    public static final int EXIT_EVENT_PROCESSING = 115;
    public static final int ERASE_EVENT = 116;
    public static final int CONTROL_SELF_SWITCH = 123;
    public static final int CHANGE_BATTLE_BGM = 132;
    
    // Map and player commands
    public static final int TRANSFER_PLAYER = 201;
    public static final int SET_EVENT_LOCATION = 202;
    public static final int SCROLL_MAP = 203;
    public static final int CHANGE_MAP_SETTINGS = 204;
    public static final int SET_MOVEMENT_ROUTE = 209;
    public static final int WAIT_FOR_MOVES_COMPLETION = 210;
    public static final int CHANGE_TILESET = 211;
    public static final int CHANGE_PARALLAX = 212;
    public static final int WAIT = 221;
    public static final int SCREEN_FLASH = 224;
    public static final int SCREEN_SHAKE = 225;
    
    // Audio/Visual commands
    public static final int PLAY_BGM_OLD = 230;
    public static final int SHOW_PICTURE = 231;
    public static final int PLAY_ME = 232;
    public static final int PLAY_SE_OLD = 233;
    public static final int STOP_SE_OLD = 234;
    public static final int ERASE_PICTURE = 235;
    public static final int PLAY_BGM = 241;
    public static final int FADE_OUT_BGM = 242;
    public static final int PLAY_BGS = 245;
    public static final int FADE_OUT_BGS = 246;
    public static final int PLAY_SE = 250;
    public static final int STOP_SE = 251;
    public static final int SCRIPT = 355;
    
    // Continuation commands
    public static final int WHEN_CHOICE = 402;
    public static final int END_CHOICE = 404;
    public static final int COMMENT_CONTINUATION = 408;
    public static final int ELSE = 411;
    public static final int BRANCH_END = 412;
    public static final int SCRIPT_CONTINUATION = 655;
    
    // Movement commands
    public static final int MOVEMENT_COMMAND = 509;
    
    // Special
    public static final int END = 0;
    
    /**
     * Retourne le nom par défaut d'une commande basé sur son code
     * Utilisé comme fallback quand aucun formateur spécifique n'est défini
     */
    public static String getDefaultCommandName(int code) {
        switch (code) {
            case SHOW_TEXT: return "Show Text";
            case SHOW_CHOICES: return "Show Choices";
            case INPUT_NUMBER: return "Input Number";
            case CHANGE_ITEMS: return "Change Items";
            case CHANGE_GOLD: return "Change Gold";
            case CHANGE_VARIABLES: return "Change Variables";
            case CHANGE_SWITCHES: return "Change Switches";
            case COMMENT: return "Comment";
            case CONDITIONAL_BRANCH: return "Conditional Branch";
            case LOOP: return "Loop";
            case BREAK_LOOP: return "Break Loop";
            case EXIT_EVENT_PROCESSING: return "Exit Event Processing";
            case ERASE_EVENT: return "Erase Event";
            case CONTROL_SELF_SWITCH: return "Control Self Switch";
            case CHANGE_BATTLE_BGM: return "Change Battle BGM";
            
            case TRANSFER_PLAYER: return "Transfer Player";
            case SET_EVENT_LOCATION: return "Set Event Location";
            case SCROLL_MAP: return "Scroll Map";
            case CHANGE_MAP_SETTINGS: return "Change Map Settings";
            case SET_MOVEMENT_ROUTE: return "Set Movement Route";
            case WAIT_FOR_MOVES_COMPLETION: return "Wait for Move's Completion";
            case CHANGE_TILESET: return "Change Tileset";
            case CHANGE_PARALLAX: return "Change Parallax";
            case WAIT: return "Wait";
            case SCREEN_FLASH: return "Screen Flash";
            case SCREEN_SHAKE: return "Screen Shake";
            
            case PLAY_BGM_OLD: return "Play BGM";
            case SHOW_PICTURE: return "Show Picture";
            case PLAY_ME: return "Play ME";
            case PLAY_SE_OLD: return "Play SE";
            case STOP_SE_OLD: return "Stop SE";
            case ERASE_PICTURE: return "Erase Picture";
            case PLAY_BGM: return "Play BGM";
            case FADE_OUT_BGM: return "Fade Out BGM";
            case PLAY_BGS: return "Play BGS";
            case FADE_OUT_BGS: return "Fade Out BGS";
            case PLAY_SE: return "Play SE";
            case STOP_SE: return "Stop SE";
            case SCRIPT: return "Script";
            
            case WHEN_CHOICE: return "When Choice";
            case END_CHOICE: return "End Choice";
            case COMMENT_CONTINUATION: return "Comment (continued)";
            case ELSE: return "Else";
            case BRANCH_END: return "Branch End";
            case SCRIPT_CONTINUATION: return "Script (continued)";
            
            case MOVEMENT_COMMAND: return "Movement Command";
            
            case END: return "End";
            default: return "Unknown Command (" + code + ")";
        }
    }
    
    /**
     * Vérifie si une commande a un éditeur spécialisé disponible
     */
    public static boolean hasEditor(int code) {
        switch (code) {
            case SET_MOVEMENT_ROUTE:
            case SHOW_TEXT:
            case COMMENT:
            case SCRIPT:
                return true;
            default:
                return false;
        }
    }
}