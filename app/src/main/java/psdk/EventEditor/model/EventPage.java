package psdk.EventEditor.model;

import java.util.ArrayList;
import java.util.List;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;

public class EventPage {
    // Basic page properties
    private int page_index;
    private int move_type;
    private int trigger;
    
    // Propriétés manquantes ajoutées
    private boolean through;
    private int move_frequency;
    private boolean always_on_top;
    private boolean walk_anime;
    private int move_speed;
    private boolean step_anime;
    private boolean direction_fix;
    private JSONObject move_route;

    // Conditions - Stored as JSONObject, but also exposed with individual properties if needed
    private JSONObject condition;
    // Example individual condition properties if EventLoader needs them directly
    private boolean conditionSwitch1Valid; // Example
    private int conditionSwitch1Id;      // Example
    private boolean conditionSwitch2Valid; // Example
    private int conditionSwitch2Id;      // Example
    private boolean conditionSelfSwitchValid; // Example
    private String conditionSelfSwitchCh; // Example
    private boolean conditionVariableValid; // Example
    private int conditionVariableId;     // Example
    private int conditionVariableValue;  // Example

    // Graphics - Stored as JSONObject, because it's not just a character name
    // it includes other properties like direction, pattern, opacity, etc.
    private JSONObject graphic;
    private String characterName;
    private int characterIndex; // Usually 0 or 1, not character_name specific index
    private int direction;      // 2:Down, 4:Left, 6:Right, 8:Up
    private int pattern;        // 0, 1, 2 for animation frames
    private int opacity;
    private int blendType;      // 0:Normal, 1:Additive, 2:Subtractive

    private List<EventCommand> commands; // List of commands for the page

    // Default Constructor
    public EventPage() {
        this.commands = new ArrayList<>();
        this.graphic = new JSONObject();
        this.condition = new JSONObject();
        this.move_route = new JSONObject();
        
        // Initialize other fields to default values as per RPG Maker XP
        this.page_index = 0;
        this.move_type = 0;
        this.trigger = 0;
        this.through = false;
        this.move_frequency = 3;
        this.always_on_top = false;
        this.walk_anime = true;
        this.move_speed = 3;
        this.step_anime = false;
        this.direction_fix = false;
        
        this.characterName = "";
        this.characterIndex = 0;
        this.direction = 2; // Default Down
        this.pattern = 0;
        this.opacity = 255;
        this.blendType = 0;
        
        // Initialiser move_route avec les valeurs par défaut
        try {
            this.move_route.put("repeat", true);
            this.move_route.put("skippable", false);
            JSONArray moveList = new JSONArray();
            JSONObject moveCmd = new JSONObject();
            moveCmd.put("code", 0);
            moveCmd.put("parameters", new JSONArray());
            moveList.put(moveCmd);
            this.move_route.put("list", moveList);
        } catch (JSONException e) {
            System.err.println("Error initializing move_route: " + e.getMessage());
        }
    }

    // Constructor with all fields
    public EventPage(int page_index, int move_type, int trigger,
                     JSONObject graphic, JSONObject condition, List<EventCommand> commands) {
        this();  // Appelle le constructeur par défaut pour initialiser
        this.page_index = page_index;
        this.move_type = move_type;
        this.trigger = trigger;
        this.commands = commands;
        
        // Load graphic and condition JSON and populate individual fields
        setGraphic(graphic);
        setCondition(condition);
    }

    public boolean isThrough() { return through; }
    public void setThrough(boolean through) { this.through = through; }

    public int getMoveFrequency() { return move_frequency; }
    public void setMoveFrequency(int move_frequency) { this.move_frequency = move_frequency; }

    public boolean isAlwaysOnTop() { return always_on_top; }
    public void setAlwaysOnTop(boolean always_on_top) { this.always_on_top = always_on_top; }

    public boolean isWalkAnime() { return walk_anime; }
    public void setWalkAnime(boolean walk_anime) { this.walk_anime = walk_anime; }

    public int getMoveSpeed() { return move_speed; }
    public void setMoveSpeed(int move_speed) { this.move_speed = move_speed; }

    public boolean isStepAnime() { return step_anime; }
    public void setStepAnime(boolean step_anime) { this.step_anime = step_anime; }

    public boolean isDirectionFix() { return direction_fix; }
    public void setDirectionFix(boolean direction_fix) { this.direction_fix = direction_fix; }

    public JSONObject getMoveRoute() { return move_route; }
    public void setMoveRoute(JSONObject move_route) { 
        this.move_route = (move_route != null) ? move_route : new JSONObject(); 
    }

    // --- Basic Page Property Getters and Setters ---
    public int getPage_index() { return page_index; }
    public void setPage_index(int page_index) { this.page_index = page_index; }

    public int getMove_type() { return move_type; }
    public void setMove_type(int move_type) { this.move_type = move_type; }

    public int getTrigger() { return trigger; }
    public void setTrigger(int trigger) { this.trigger = trigger; }

    // --- Graphic Getters and Setters ---
    public JSONObject getGraphic() { return graphic; }
    public void setGraphic(JSONObject graphic) {
        this.graphic = (graphic != null) ? graphic : new JSONObject();
        // Update individual fields from the JSONObject
        this.characterName = this.graphic.optString("character_name", "");
        this.characterIndex = this.graphic.optInt("pattern", 0);
        this.direction = this.graphic.optInt("direction", 2);
        this.pattern = this.graphic.optInt("pattern", 0);
        this.opacity = this.graphic.optInt("opacity", 255);
        this.blendType = this.graphic.optInt("blend_type", 0);
    }

    // Individual graphic field getters and setters (for EventLoader direct calls)
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) {
        this.characterName = characterName;
        try { this.graphic.put("character_name", characterName); } catch (JSONException e) { /* handle */ }
    }

    public int getCharacterIndex() { return characterIndex; }
    public void setCharacterIndex(int characterIndex) {
        this.characterIndex = characterIndex;
        try { this.graphic.put("pattern", characterIndex); } catch (JSONException e) { /* handle */ }
    }

    public int getDirection() { return direction; }
    public void setDirection(int direction) {
        this.direction = direction;
        try { this.graphic.put("direction", direction); } catch (JSONException e) { /* handle */ }
    }

    public int getPattern() { return pattern; }
    public void setPattern(int pattern) {
        this.pattern = pattern;
        try { this.graphic.put("pattern", pattern); } catch (JSONException e) { /* handle */ }
    }

    public int getOpacity() { return opacity; }
    public void setOpacity(int opacity) {
        this.opacity = opacity;
        try { this.graphic.put("opacity", opacity); } catch (JSONException e) { /* handle */ }
    }

    public int getBlendType() { return blendType; }
    public void setBlendType(int blendType) {
        this.blendType = blendType;
        try { this.graphic.put("blend_type", blendType); } catch (JSONException e) { /* handle */ }
    }

    // --- Condition Getters and Setters ---
    public JSONObject getCondition() { return condition; }
    public void setCondition(JSONObject condition) {
        this.condition = (condition != null) ? condition : new JSONObject();
        // Update individual fields from the JSONObject for direct access (if needed)
        this.conditionSwitch1Valid = this.condition.optBoolean("switch1_valid", false);
        this.conditionSwitch1Id = this.condition.optInt("switch1_id", 0);
        this.conditionSwitch2Valid = this.condition.optBoolean("switch2_valid", false);
        this.conditionSwitch2Id = this.condition.optInt("switch2_id", 0);
        this.conditionSelfSwitchValid = this.condition.optBoolean("self_switch_valid", false);
        this.conditionSelfSwitchCh = this.condition.optString("self_switch_ch", "A");
        this.conditionVariableValid = this.condition.optBoolean("variable_valid", false);
        this.conditionVariableId = this.condition.optInt("variable_id", 0);
        this.conditionVariableValue = this.condition.optInt("variable_value", 0);
    }
    
    // Individual condition field getters and setters (for EventLoader direct calls)
    public boolean getConditionSwitch1Valid() { return conditionSwitch1Valid; }
    public void setConditionSwitch1Valid(boolean valid) {
        this.conditionSwitch1Valid = valid;
        try { this.condition.put("switch1_valid", valid); } catch (JSONException e) { /* handle */ }
    }
    public int getConditionSwitch1Id() { return conditionSwitch1Id; }
    public void setConditionSwitch1Id(int id) {
        this.conditionSwitch1Id = id;
        try { this.condition.put("switch1_id", id); } catch (JSONException e) { /* handle */ }
    }

    public List<EventCommand> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        return commands;
    }

    public void setCommands(List<EventCommand> commands) {
        this.commands = commands;
    }

    /**
     * Converts the internal List<EventCommand> to a JSONArray of raw command JSON objects.
     */
    public JSONArray toCommandsJsonArray() {
        JSONArray jsonArray = new JSONArray();
        if (commands != null) {
            for (EventCommand cmd : commands) {
                JSONObject cmdJson = new JSONObject();
                try {
                    cmdJson.put("code", cmd.getCode());
                    cmdJson.put("indent", cmd.getIndent()); 
                    cmdJson.put("parameters", cmd.getParameters());
                    jsonArray.put(cmdJson);
                } catch (JSONException e) {
                    System.err.println("Error converting EventCommand to JSON: " + e.getMessage());
                }
            }
        }
        return jsonArray;
    }

    /**
     * Populates the internal List<EventCommand> from a JSONArray of raw command JSON objects.
     */
    public void loadCommandsFromJsonArray(JSONArray commandsJson) {
        this.commands = new ArrayList<>();
        if (commandsJson != null) {
            for (int i = 0; i < commandsJson.length(); i++) {
                try {
                    JSONObject cmdJson = commandsJson.getJSONObject(i);
                    this.commands.add(new EventCommand(
                        cmdJson.getInt("code"),
                        cmdJson.getString("indent"), 
                        cmdJson.getJSONArray("parameters")
                    ));
                } catch (JSONException e) {
                    System.err.println("Error parsing command JSON into EventCommand: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public String toString() {
        return "EventPage [page_index=" + page_index + ", commands=" + commands.size() + ", graphic=" + graphic.optString("character_name") + "]";
    }
}