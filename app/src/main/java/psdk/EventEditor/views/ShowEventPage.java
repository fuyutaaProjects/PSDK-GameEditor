package psdk.EventEditor.views;

import java.util.List;

import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventPage;

public class ShowEventPage {
    
    /**
     * Static method to display event page details in terminal
     */
    public static void displayEventPageDetails(EventPage page, int pageIndex) {
        System.out.println("--- PAGE " + pageIndex + " ---");
        System.out.println("Page Index: " + page.getPage_index());
        
        // Movement and behavior properties
        System.out.println("Move Type: " + page.getMove_type() + " (" + getMoveTypeName(page.getMove_type()) + ")");
        System.out.println("Trigger: " + page.getTrigger() + " (" + getTriggerName(page.getTrigger()) + ")");
        System.out.println("Through: " + page.isThrough());
        System.out.println("Move Speed: " + page.getMoveSpeed() + " (" + getMoveSpeedName(page.getMoveSpeed()) + ")");
        System.out.println("Move Frequency: " + page.getMoveFrequency() + " (" + getMoveFrequencyName(page.getMoveFrequency()) + ")");
        System.out.println("Always On Top: " + page.isAlwaysOnTop());
        System.out.println("Walk Animation: " + page.isWalkAnime());
        System.out.println("Step Animation: " + page.isStepAnime());
        System.out.println("Direction Fix: " + page.isDirectionFix());
        
        // Display graphic information
        displayGraphicInfo(page);
        
        // Display conditions
        displayConditionInfo(page);
        
        // Display move route info
        displayMoveRouteInfo(page);
        
        // Display commands
        displayCommandsInfo(page);
    }
    
    /**
     * Display graphic information for the event page
     */
    private static void displayGraphicInfo(EventPage page) {
        System.out.println("--- GRAPHIC INFO ---");
        System.out.println("Character: " + (page.getCharacterName().isEmpty() ? "(none)" : page.getCharacterName()));
        System.out.println("Character Index: " + page.getCharacterIndex());
        System.out.println("Direction: " + page.getDirection() + " (" + getDirectionName(page.getDirection()) + ")");
        System.out.println("Pattern: " + page.getPattern());
        System.out.println("Opacity: " + page.getOpacity());
        System.out.println("Blend Type: " + page.getBlendType() + " (" + getBlendTypeName(page.getBlendType()) + ")");
    }
    
    /**
     * Display condition information for the event page
     */
    private static void displayConditionInfo(EventPage page) {
        System.out.println("--- CONDITIONS ---");
        System.out.println("Switch1 Valid: " + page.getConditionSwitch1Valid());
        if (page.getConditionSwitch1Valid()) {
            System.out.println("Switch1 ID: " + page.getConditionSwitch1Id());
        }
        
        // Note: Only showing Switch1 for now, but could be extended for other conditions
        // when the other getters are implemented in EventPage
    }
    
    /**
     * Display move route information for the event page
     */
    private static void displayMoveRouteInfo(EventPage page) {
        System.out.println("--- MOVE ROUTE ---");
        if (page.getMoveRoute() != null) {
            System.out.println("Repeat: " + page.getMoveRoute().optBoolean("repeat", true));
            System.out.println("Skippable: " + page.getMoveRoute().optBoolean("skippable", false));
            if (page.getMoveRoute().has("list")) {
                System.out.println("Move Commands: " + page.getMoveRoute().optJSONArray("list").length());
            }
        } else {
            System.out.println("No move route defined");
        }
    }
    
    /**
     * Display commands information for the event page
     */
    private static void displayCommandsInfo(EventPage page) {
        System.out.println("--- COMMANDS ---");
        System.out.println("Total Commands: " + page.getCommands().size());
        System.out.println("................................................................................");
        
        List<EventCommand> commands = page.getCommands();
        for (int cmdIndex = 0; cmdIndex < commands.size(); cmdIndex++) {
            EventCommand command = commands.get(cmdIndex);
            ShowEventCommand.displayEventCommandDetails(command, cmdIndex + 1);
            
            if (cmdIndex < commands.size() - 1) {
                System.out.println("  ................................................................");
            }
        }
    }
    
    /**
     * Returns a human-readable name for move types
     */
    private static String getMoveTypeName(int moveType) {
        switch (moveType) {
            case 0: return "Fixed";
            case 1: return "Random";
            case 2: return "Approach";
            case 3: return "Custom";
            default: return "Unknown";
        }
    }
    
    /**
     * Returns a human-readable name for trigger types
     */
    private static String getTriggerName(int trigger) {
        switch (trigger) {
            case 0: return "Action Button";
            case 1: return "Player Touch";
            case 2: return "Event Touch";
            case 3: return "Autorun";
            case 4: return "Parallel Process";
            default: return "Unknown";
        }
    }
    
    /**
     * Returns a human-readable name for move speeds
     */
    private static String getMoveSpeedName(int speed) {
        switch (speed) {
            case 1: return "x8 Slower";
            case 2: return "x4 Slower";
            case 3: return "x2 Slower";
            case 4: return "Normal";
            case 5: return "x2 Faster";
            case 6: return "x4 Faster";
            default: return "Unknown";
        }
    }
    
    /**
     * Returns a human-readable name for move frequencies
     */
    private static String getMoveFrequencyName(int frequency) {
        switch (frequency) {
            case 1: return "Lowest";
            case 2: return "Lower";
            case 3: return "Normal";
            case 4: return "Higher";
            case 5: return "Highest";
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
     * Returns a human-readable name for blend types
     */
    private static String getBlendTypeName(int blendType) {
        switch (blendType) {
            case 0: return "Normal";
            case 1: return "Additive";
            case 2: return "Subtractive";
            default: return "Unknown";
        }
    }
}