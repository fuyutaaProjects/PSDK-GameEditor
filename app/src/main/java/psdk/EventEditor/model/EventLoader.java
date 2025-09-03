package psdk.EventEditor.model;

import java.util.ArrayList;
import java.util.List;

import libs.json.JSONArray;
import libs.json.JSONException;
import libs.json.JSONObject;

public class EventLoader {

    public static List<Event> loadEventsFromArray(JSONArray eventsJsonArray) throws JSONException {
        List<Event> events = new ArrayList<>();
        if (eventsJsonArray == null) {
            return events;
        }

        for (int i = 0; i < eventsJsonArray.length(); i++) {
            JSONObject eventJson = eventsJsonArray.getJSONObject(i);
            
            int id = eventJson.optInt("id", 0);
            String name = eventJson.optString("name", "New Event");
            int x = eventJson.optInt("x", 0);
            int y = eventJson.optInt("y", 0);

            Event event = new Event(id, name, x, y);

            JSONArray pagesJson = eventJson.optJSONArray("pages");
            if (pagesJson != null) {
                List<EventPage> pages = new ArrayList<>();
                for (int j = 0; j < pagesJson.length(); j++) {
                    JSONObject pageJson = pagesJson.getJSONObject(j);
                    EventPage page = loadEventPageFromJson(pageJson, j + 1);
                    pages.add(page);
                }
                event.setPages(pages);
            } else {
                event.setPages(new ArrayList<>());
            }
            events.add(event);
        }
        return events;
    }

    private static EventPage loadEventPageFromJson(JSONObject pageJson, int defaultPageIndex) {
        EventPage page = new EventPage();
        
        try {
            page.setPage_index(pageJson.optInt("page_index", defaultPageIndex));
            page.setMove_type(pageJson.optInt("move_type", 0));
            page.setTrigger(pageJson.optInt("trigger", 0));
            
            page.setThrough(pageJson.optBoolean("through", false));
            page.setMoveFrequency(pageJson.optInt("move_frequency", 3));
            page.setAlwaysOnTop(pageJson.optBoolean("always_on_top", false));
            page.setWalkAnime(pageJson.optBoolean("walk_anime", true));
            page.setMoveSpeed(pageJson.optInt("move_speed", 3));
            page.setStepAnime(pageJson.optBoolean("step_anime", false));
            page.setDirectionFix(pageJson.optBoolean("direction_fix", false));

            JSONObject graphicJson = pageJson.optJSONObject("graphic");
            if (graphicJson == null) {
                graphicJson = new JSONObject();
            }
            page.setGraphic(graphicJson);

            JSONObject conditionJson = pageJson.optJSONObject("condition");
            if (conditionJson == null) {
                conditionJson = new JSONObject();
            }
            page.setCondition(conditionJson);

            // Move route
            JSONObject moveRouteJson = pageJson.optJSONObject("move_route");
            if (moveRouteJson == null) {
                // Create one
                moveRouteJson = new JSONObject();
                moveRouteJson.put("repeat", true);
                moveRouteJson.put("skippable", false);
                JSONArray moveList = new JSONArray();
                JSONObject moveCmd = new JSONObject();
                moveCmd.put("code", 0);
                moveCmd.put("parameters", new JSONArray());
                moveList.put(moveCmd);
                moveRouteJson.put("list", moveList);
            }
            page.setMoveRoute(moveRouteJson);

            JSONArray commandsJsonArray = pageJson.optJSONArray("list");
            if (commandsJsonArray == null) {
                commandsJsonArray = pageJson.optJSONArray("commands");
            }
            
            if (commandsJsonArray != null) {
                List<EventCommand> commandsList = new ArrayList<>();
                for (int k = 0; k < commandsJsonArray.length(); k++) {
                    JSONObject cmdJson = commandsJsonArray.getJSONObject(k);
                    int code = cmdJson.optInt("code", 0);
                    String indent = String.valueOf(cmdJson.optInt("indent", 0));
                    JSONArray parameters = cmdJson.optJSONArray("parameters");
                    if (parameters == null) {
                        parameters = new JSONArray();
                    }
                    
                    EventCommand command = new EventCommand(code, indent, parameters);
                    commandsList.add(command);
                }
                page.setCommands(commandsList);
            } else {
                page.setCommands(new ArrayList<>());
            }
            
        } catch (JSONException e) {
            System.err.println("Error loading EventPage from JSON: " + e.getMessage());
            e.printStackTrace();
        }
        
        return page;
    }

    public static List<Event> loadEvents(JSONObject mapJsonData) throws JSONException {
        List<Event> events = new ArrayList<>();
        if (mapJsonData.has("events") && mapJsonData.get("events") instanceof JSONObject) {
            JSONObject eventsObject = mapJsonData.getJSONObject("events");
            for (String key : eventsObject.keySet()) {
                JSONObject eventJson = eventsObject.getJSONObject(key);
                try {
                    int id = Integer.parseInt(key);
                    String name = eventJson.optString("name", "Event " + id);
                    int x = eventJson.optInt("x", 0);
                    int y = eventJson.optInt("y", 0);
                    Event event = new Event(id, name, x, y);

                    // Load pages using the same method
                    JSONArray pagesJsonArray = eventJson.optJSONArray("pages");
                    if (pagesJsonArray != null) {
                        List<EventPage> pages = new ArrayList<>();
                        for (int j = 0; j < pagesJsonArray.length(); j++) {
                            JSONObject pageJson = pagesJsonArray.getJSONObject(j);
                            EventPage page = loadEventPageFromJson(pageJson, j + 1);
                            pages.add(page);
                        }
                        event.setPages(pages);
                    } else {
                        event.setPages(new ArrayList<>());
                    }
                    events.add(event);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing event ID from key: " + key + ". " + e.getMessage());
                }
            }
        }
        return events;
    }
}