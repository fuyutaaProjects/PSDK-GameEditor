## PSDK RMXP Project Editor User Manual 🛠️

---

### 1. Introduction 👋

The PSDK RMXP Project Editor is a Java Swing application designed for external editing of RPG Maker XP (RMXP) projects. 
**This tool was created to address the limitations and outdated nature of RMXP's native editor**, providing a modern, external alternative while maintaining compatibility with RMXP project structures (as PSDK itself builds upon this foundation). 
By **converting `.rxdata` files to `.yml` using PSDK's convert utility**, the editor can then **read these plain-text `.yml` map data files and allow you to edit their events**.

**Key Features:**
* **Overhauled User Interface (UI):** A modern and intuitive design.
* **Improved Map List Panel:** Features a search bar for quick map lookup.
* **Enhanced Command List Panel:** Improved coloring for better readability.
* **Intelligent Command Insertion:** Use `Shift+/` to search for commands by keywords and quickly add them to an `EventPage`.
* **Dark Mode:** A comfortable viewing option for prolonged use.
* **PSDK Command Integration:** New buttons in the "adding command dialog" specifically for PSDK commands.
* **Streamlined Event Editing Dialog:** `Shift+Enter` shortcut to exit and save.
* **Simplified Pathfinding:** Click starting and final locations to automatically generate the pathfinding command with correct coordinates.

This tool provides an intuitive interface for streamlined event editing outside the standard RMXP environment.


---

### 2. Getting Started ▶️

The application's entry point is `Core.java`. To launch the editor, open the project in VS Code. As it is set up as a Gradle project, VS Code should automatically detect `Core.java` as the main launch file.

---

### 3. Project Variables Management 📂

`ConfigManager.java` handles project path management. The editor uses `config.properties` to store the RPG Maker XP project path.

#### 3.1. Initial Project Loading
`Core.java` initiates project loading. `ConfigManager.java` loads the project path from `config.properties` using the variable `rpg_maker_project_path`.
* If a path is found in `rpg_maker_project_path`: `Core.java` will utilize this path.
* If `rpg_maker_project_path` is not found or `config.properties` does not exist: You will be prompted to configure a path, via the `MainMenu`.

#### 3.2. Saving Project Path 💾
`ConfigManager.java` saves the project path to `config.properties`. The `saveProjectPath(String path)` method writes the provided `path` string to the `rpg_maker_project_path` variable within the `config.properties` file.

#### 3.3. Clearing Project Path 🗑️
`ConfigManager.java` clears the stored project path. The `clearProjectPath()` method removes the `rpg_maker_project_path` variable from `config.properties`, effectively deleting the file.

#### 3.4. Getting the Project Path 📁
To retrieve the configured project path, use the method:
`ConfigManager.getProjectPath()`
* **Note:** While `ConfigManager.loadProjectPath()` technically returns the path, it also performs additional actions like path validation and writing to the config file. For simply *getting* the currently stored path, `getProjectPath()` is the appropriate getter.


---

### 4. Main User Interface 🖥️

`Core.java` initializes the main `JFrame` and uses a `CardLayout` to switch between views.

#### 4.1. Main Menu (`MainMenu`)
`MainMenu.java` is the view that's shown when booting up the app. It allows you to configure your RPG Maker XP project path before diving in the editor itself.

#### 4.2. Editor View (`EditorView`)
`EditorView.java` is displayed after exiting the `MainMenu`. It features:
* **Map List Panel (`MapListPanel`):** `MapListPanel.java` is located on the left side of the screen. It enumerates all map files (`MapXXX.yml`) found within the `[Project Root Folder]/Data` directory that are registered as actual in-game maps via the `MapInfos.rxdata.yml` file. This `MapInfos.rxdata.yml` file specifically lists the maps present in the game and provides their unique numerical index (ID).

* **Map Visualization Area:** A `JLayeredPane` instance named `layeredPane` in `EditorView.java` overlays different visualizers:
    * `TileVisualizer.java`: Displays map tiles. <br><br>

    * `GridOverlayVisualizer.java`: This is a `JPanel` placed on top of the `TileVisualizer.java` panel via `layeredPane.add(gridVisualizer, JLayeredPane.PALETTE_LAYER)` within `EditorView.java`. The script itself draws a grid, mimicking RMXP's grid display. It overlaps over the `TileVisualizer`, and is declared in `EditorView.java`. The overlay's visibility can be toggled on/off via the "Toggle Grid" button found in `TileGridToolbar.java`. The button itself calls the `toggleGridVisibility()` function in `GridOverlayVisualizer.java`. <br><br>

    * `EventVisualizer.java`: Displays clickable map events. Events are loaded by `EventLoader.java` from the "events" section of the map's JSON data (`currentMapJsonData`) as a `JSONArray` (the way we translate the map yml into a json is explained in section 5.1). `EventLoader.loadEventsFromArray()` converts this `JSONArray` into a `List<Event>` of objects. This `currentMapEvents` list is then passed by `EditorView.java` to `EventVisualizer.java` via the `updateEvents(currentMapEvents, mapDimensions)` method, which is responsible for drawing the events on the map. <br><br>

* **Tile Grid Toolbar (`TileGridToolbar`):** `TileGridToolbar.java` is located at the top of the visualization area, encapsulated within a wrapper panel in `EditorView.java`. It stores buttons such as the "Show Grid", explained previously.

---

### 5. Map and Event Editing 🗺️

#### 5.1. Map Selection
When a user selects a map (`.yml`) file from the `MapListPanel`, the following detailed process is initiated to load and display the map data:

1.  **Callback Activation:** The selection of a map file in `MapListPanel.java` triggers a callback (`onMapSelected`) within `EditorView.java`.
2.  **Python Script Invocation:** `onMapSelected` calls `loadMapData()`.
The function contains this line :
currentMapJsonData = editor.loadMapFromJson(currentYmlFile);
As you can see, it calls for `loadMapFromJson` in `Editor.java`, which will then execute a separate **Python script** `yml_to_json.py`, taking the selected plain-text `.yml` map file as input.
3.  **YML to JSON Conversion:** This Python script processes the `.yml` content and converts it into a standardized JSON format.
4.  **JSON Data Loading into Memory:** The resulting JSON data is returned to `EditorView.java`, and stored in `currentMapJsonData`.
5.  **Data Extraction:** From the `map_data` key in the `JSONObject`, `EditorView.java` extracts crucial information such as the Map ID and the associated tileset ID.
6.  **Tile Grid Conversion and Display:** `GridDataConverter.java` processes the 3D tile grid data found within the `map_data` JSON. This converted grid is then loaded and displayed by `TileVisualizer.java` for visual representation of the map's layout.
7.  **Event Loading:** `EventLoader.java` loads all event data from the "events" section of the `map_data` JSON. This data is then used to populate the `EventVisualizer.java`. The `List` that contains these events is stored in the `currentMapEvents` variable in `EditorView.java`.


#### 5.2. Visualization
`EditorView.java` updates `TileVisualizer.java`, `GridOverlayVisualizer.java`, and `EventVisualizer.java` to display the map, grid, and events with the correct tileset. Map dimensions are dynamically calculated by `EditorView.java` for display adjustment.

#### 5.3. Map Interaction : how clicks are handled
`MouseListener.java` handles mouse interactions, attached to the `JLayeredPane` in `EditorView.java`. When a user clicks on the map visualization area, `MouseListener.java` calculates the corresponding tile coordinates. It then checks if an event is present at these coordinates. If an event is found, the `EventEditorDialog` is opened to allow for event modification.

#### 5.4. Event Synchronization 🔄

`EditorView.java` manages the synchronization of event modifications with the map's underlying JSON data.

**Event Editing Flow and Data Management:**

When an event is clicked on the map, `MouseListener.java` initiates the process that leads to the opening of the `EventEditorDialog` for that specific event.

1.  **Opening the `EventEditorDialog`**: `EditorView.java` creates an instance of `EventEditorDialog`, passing it a direct reference to the `Event` object that was clicked (from its `currentMapEvents` list). It also passes a callback interface, `EventModificationCallback`, interface declared in EventEditorDialog and instance created in MouseListener after clicking on the event.

2.  **Editing Command Details via `CommandListPanel`**: The core of command-specific editing happens within the `CommandListPanel` (a component of `EventEditorDialog`).
    * When an `EventCommand` in the `CommandListPanel` is double-clicked, its `CommandListMouseListener` handles the interaction.
    * This listener calls `editorManager.openSpecificCommandEditor(selectedCommand)`. Inside `CommandEditorManager`, a **deep copy of the `EventCommand` is made** (`EventCommand commandCopy = createCommandCopy(commandToEdit);`). This ensures that initial modifications are performed on a temporary object, preventing direct, unintended changes to the original `EventCommand` while the editor dialog is open.
    * Based on the command's code, a specific editor dialog (e.g., `ShowTextEditorDialog`, `SetMoveRouteEditorDialog`) is opened, operating on this `commandCopy`.

3.  **Returning Changes to `CommandListPanel`**:
    * **For "Show Text" (Code 101)**: The `ShowTextEditorDialog` works on the `commandCopy`. If changes are made and confirmed, `CommandEditorManager` then **updates the original `EventCommand`'s properties** (code, indent, and parameters) by assigning the values from the `modified` `EventCommand` copy (`original.setParameters(modified.getParameters());`). This effectively "replaces" the content of the original object with the modified one.
    * **For "Set Move Route" (Code 209)**: Similarly, the `SetMoveRouteEditorDialog` works on the `commandCopy`'s parameters. If changes are made and confirmed, `CommandEditorManager` **updates the original `EventCommand`'s parameters** by assigning the `JSONArray` returned by `dialog.getModified209Parameters()` (`original.setParameters(dialog.getModified209Parameters());`).
    * In both cases, after the original `EventCommand` has been updated in memory, the `CommandListPanel` updates its `JList` model (`commandListModel.set(selectedIndex, commandToEdit);`) and notifies its listeners (`notifyModification()`) to reflect the changes.

4.  **"Separation" Clarification**: The edits are **not** stored in a separate, temporary copy of the overall `Event`. Instead, the `EventEditorDialog` works directly on the original `Event` object that is already part of `EditorView.java`'s `currentMapEvents` list. For individual commands, sub-dialogs like `ShowTextEditorDialog` and `SetMoveRouteEditorDialog` operate on a deep copy of the `EventCommand`'s parameters (or the command itself for `ShowTextEditorDialog`), which are then explicitly propagated back to the *original* `EventCommand` instance within the `CommandListPanel`.

5.  **Visual vs. Data Persistence for "Set Move Route"**:
    * It's important to note that the changes made to "Set Move Route" commands **do persist in the underlying `EventCommand` object's data**. The `CommandListPanel`'s update mechanism ensures this.
    * However, the *visual representation* in the `JList` for "Set Move Route" commands (Code 209) typically does not show the detailed list of individual move commands. This is because the `EventCommandListCellRenderer` (which renders the text for each list item) often provides only a generic label like "Set Move Route" rather than parsing and displaying the complex `JSONArray` of its parameters. If you don't see visual changes in the list after editing a "Set Move Route" command, it's a limitation of its renderer, not a data persistence issue.

6.  **Returning Changes via Callback**:
    * When the `EventEditorDialog` is closed (e.g., by the user closing the window), a `WindowAdapter` attached to the dialog is triggered.
    * If the `eventWasModified` flag is `true`, the `modificationCallback.onEventModified(event)` method is invoked.
    * Since `EditorView.java` is the implementing class for `EventModificationCallback`, its `onEventModified` method is called. This signals to `EditorView.java` that modifications have been made to an event.
    * Upon receiving this notification, `EditorView.java` can then trigger necessary updates, such as redrawing the `EventVisualizer`, to reflect any visual changes and, crucially, calling its `syncEventsToJson()` method to prepare the data for saving.

* **`syncEventsToJson()` Method**: This method in `EditorView.java` converts the current in-memory `List<Event>` (which now contains the modified event) back into a `JSONArray` that matches the structure expected by the `.yml` map file.
    * **Process**: It iterates through each `Event` object in the `currentMapEvents` list. For each `Event` and its `EventPage`s, `EditorView.java` converts these Java objects into their corresponding `JSONObject` representations.
    * **Data Update**: This newly generated `JSONArray` is then used to update the "events" key within the `currentMapJsonData` `JSONObject`, ensuring the in-memory JSON accurately reflects all current event states. This updated `JSONObject` is what will eventually be saved back to the `.yml` file.