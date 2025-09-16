package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.OverlayLayout;
import javax.swing.SwingUtilities;

import libs.json.JSONArray;
import libs.json.JSONObject;
import psdk.EventEditor.ConfigManager;
import psdk.EventEditor.model.Editor;
import psdk.EventEditor.model.Event;
import psdk.EventEditor.model.EventLoader;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.GridDataConverter;
import psdk.EventEditor.model.YmlGetter;

public class EditorView extends JPanel {

    // Constants
    private static final int DEFAULT_SCROLL_UNIT = 16;
    private static final int DEFAULT_MAP_ID = 0;
    private static final String MAP_FILENAME_PATTERN = "Map\\d{3}\\..*";
    
    // Core components
    private final Editor editor;
    private final String rpgMakerProjectRootPath;
    
    // Current state
    private File currentYmlFile;
    private int[][][] currentTileGrid;
    private int currentTilesetId;
    private JSONObject currentMapJsonData;
    private List<Event> currentMapEvents;
    private int currentMapId;
    
    // UI components
    private MapListPanel mapListPanel;
    private TileVisualizer tileVisualizer;
    private GridOverlayVisualizer gridVisualizer;
    private EventVisualizer eventVisualizer;
    private TileGridToolbar tileGridToolbar;
    private JScrollPane scrollPane;
    private JLayeredPane layeredPane;

    public EditorView(String projectRootPath) {
        if (projectRootPath == null || projectRootPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Project root path cannot be null or empty");
        }
        
        this.rpgMakerProjectRootPath = projectRootPath;
        this.editor = new Editor(projectRootPath);
        
        initializeState();
        initializeUI();
        loadLastOpenedMapIfExists();
    }
    
    private void initializeState() {
        currentMapId = DEFAULT_MAP_ID;
        currentTilesetId = DEFAULT_MAP_ID;
        currentMapEvents = new ArrayList<>();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Create UI components
        createMapListPanel();
        createVisualizerComponents();
        createLayeredPane();
        createScrollPane();
        createToolbar();
        
        // Layout components
        layoutComponents();
    }
    
    /**
     * Load the last opened map automatically when the editor starts
     */
    private void loadLastOpenedMapIfExists() {
        SwingUtilities.invokeLater(() -> {
            String lastMapPath = ConfigManager.loadLastOpenedMap();
            if (lastMapPath != null) {
                File lastMapFile = new File(lastMapPath);
                if (lastMapFile.exists() && lastMapFile.isFile()) {
                    System.out.println("Loading last opened map: " + lastMapPath);
                    onMapSelected(lastMapFile);
                } else {
                    System.out.println("Last opened map file not found, cleared from config");
                }
            } else {
                System.out.println("No last opened map found");
            }
        });
    }
    
    private void createMapListPanel() {
        mapListPanel = new MapListPanel(editor, this::onMapSelected, rpgMakerProjectRootPath);
        add(mapListPanel, BorderLayout.WEST);
    }
    
    private void createVisualizerComponents() {
        tileVisualizer = new TileVisualizer();
        gridVisualizer = new GridOverlayVisualizer();
        eventVisualizer = new EventVisualizer();
        
        // Set alignment for overlay layout
        setVisualizerAlignment();
    }
    
    private void setVisualizerAlignment() {
        tileVisualizer.setAlignmentX(0.0f);
        tileVisualizer.setAlignmentY(0.0f);
        gridVisualizer.setAlignmentX(0.0f);
        gridVisualizer.setAlignmentY(0.0f);
        eventVisualizer.setAlignmentX(0.0f);
        eventVisualizer.setAlignmentY(0.0f);
    }
    
    private void createLayeredPane() {
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        
        // Add visualizers to appropriate layers
        layeredPane.add(tileVisualizer, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(gridVisualizer, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(eventVisualizer, JLayeredPane.MODAL_LAYER);
        
        // Add mouse listeners
        MouseListener mouseListener = new MouseListener(tileVisualizer, gridVisualizer, eventVisualizer, this);
        layeredPane.addMouseListener(mouseListener);
        layeredPane.addMouseMotionListener(mouseListener);
        layeredPane.addMouseWheelListener(mouseListener);
    }
    
    private void createScrollPane() {
        scrollPane = new JScrollPane(layeredPane);
        scrollPane.getVerticalScrollBar().setUnitIncrement(DEFAULT_SCROLL_UNIT);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(DEFAULT_SCROLL_UNIT);
    }
    
    private void createToolbar() {
        tileGridToolbar = new TileGridToolbar(editor, currentMapId, currentMapJsonData, gridVisualizer);
    }
    
    private void layoutComponents() {
        JPanel mapPanelWrapper = new JPanel(new BorderLayout());
        mapPanelWrapper.add(tileGridToolbar, BorderLayout.NORTH);
        mapPanelWrapper.add(scrollPane, BorderLayout.CENTER);
        
        add(mapPanelWrapper, BorderLayout.CENTER);
    }

    public void onMapSelected(File ymlFile) {
        if (ymlFile == null) {
            System.err.println("[EditorView] Cannot select null YML file");
            return;
        }
        
        currentYmlFile = ymlFile;
        
        // Save the selected map as the last opened map
        ConfigManager.saveLastOpenedMap(ymlFile.getAbsolutePath());
        
        try {
            resetVisualizerScrolls();
            
            // Load map data
            if (!loadMapData()) {
                handleMapLoadFailure();
                return;
            }
            
            // Extract and validate map ID
            extractMapId();
            
            // Update editor state
            updateEditorState();
            
            // Load map components
            loadMapComponents();
            
            // Update visualizers
            updateVisualizers();
            
        } catch (Exception e) {
            System.err.println("[EditorView] Unexpected error loading map: " + e.getMessage());
            e.printStackTrace();
            handleMapLoadFailure();
        }
    }
    
    private void resetVisualizerScrolls() {
        tileVisualizer.resetScroll();
        gridVisualizer.scrollX = 0;
        gridVisualizer.scrollY = 0;
        eventVisualizer.scrollX = 0;
        eventVisualizer.scrollY = 0;
    }
    
    private boolean loadMapData() {
        currentMapJsonData = editor.loadMapFromJson(currentYmlFile);
        
        if (currentMapJsonData == null) {
            System.err.println("[EditorView] Could not load map JSON data");
            return false;
        }
        
        return true;
    }
    
    private void extractMapId() {
        String fileName = currentYmlFile.getName();
        
        if (fileName.matches(MAP_FILENAME_PATTERN)) {
            try {
                currentMapId = Integer.parseInt(fileName.substring(3, 6));
            } catch (NumberFormatException e) {
                System.err.println("[EditorView] Could not parse map ID from filename: " + fileName);
                currentMapId = DEFAULT_MAP_ID;
            }
        } else {
            System.err.println("[EditorView] Unexpected filename format: " + fileName);
            currentMapId = DEFAULT_MAP_ID;
        }
    }
    
    private void updateEditorState() {
        editor.setCurrentMapDataJson(currentMapJsonData);
        tileGridToolbar.setCurrentMapData(currentMapId, currentMapJsonData);
    }
    
    private void loadMapComponents() {
        loadEvents();
        loadTilesetAndGrid();
    }
    
    private void loadEvents() {
        currentMapEvents = new ArrayList<>();
        
        if (currentMapJsonData.has("events")) {
            Object eventsObject = currentMapJsonData.get("events");
            if (eventsObject instanceof JSONArray) {
                currentMapEvents = EventLoader.loadEventsFromArray((JSONArray) eventsObject);
            } else {
                System.out.println("[EditorView] Events key found but not a JSONArray");
            }
        } else {
            System.out.println("[EditorView] No events found in map data");
        }
    }
    
    private void loadTilesetAndGrid() {
        Optional<JSONObject> mapDataOpt = getMapDataObject();
        
        if (!mapDataOpt.isPresent()) {
            System.err.println("[EditorView] Could not load map_data object");
            return;
        }
        
        JSONObject mapDataJson = mapDataOpt.get();
        
        // Load tileset ID
        currentTilesetId = YmlGetter.getTilesetIdFromJson(mapDataJson);
        
        // Load tile grid
        currentTileGrid = loadTileGrid(mapDataJson);
    }
    
    private Optional<JSONObject> getMapDataObject() {
        if (!currentMapJsonData.has("map_data")) {
            return Optional.empty();
        }
        
        try {
            return Optional.of(currentMapJsonData.getJSONObject("map_data"));
        } catch (Exception e) {
            System.err.println("[EditorView] Error accessing map_data: " + e.getMessage());
            return Optional.empty();
        }
    }
    
    private int[][][] loadTileGrid(JSONObject mapDataJson) {
        if (!mapDataJson.has("grid_info")) {
            System.err.println("[EditorView] No grid_info found in map_data");
            return null;
        }
        
        try {
            JSONObject gridInfo = mapDataJson.getJSONObject("grid_info");
            return GridDataConverter.convertJsonGridTo3DArray(gridInfo);
        } catch (Exception e) {
            System.err.println("[EditorView] Error loading tile grid: " + e.getMessage());
            return null;
        }
    }
    
    private void updateVisualizers() {
        try {
            BufferedImage tilesetImage = loadTilesetImage();
            
            // Update tile visualizer
            tileVisualizer.updateGrid(currentTileGrid, tilesetImage);
            
            // Calculate and set map dimensions
            Dimension mapDimensions = calculateMapDimensions();
            updateVisualizersWithDimensions(mapDimensions);
            
        } catch (IOException e) {
            System.err.println("[EditorView] Error updating visualizers: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private BufferedImage loadTilesetImage() throws IOException {
        if (currentTilesetId == 0) {
            System.out.println("[EditorView] No tileset to load (ID is 0)");
            return null;
        }
        
        File tilesetFile = new File(rpgMakerProjectRootPath, 
            "Graphics" + File.separator + "Tilesets" + File.separator + "_" + currentTilesetId + ".png");
        
        if (!tilesetFile.exists()) {
            System.err.println("[EditorView] Tileset file not found: " + tilesetFile.getAbsolutePath());
            return null;
        }
        
        return ImageIO.read(tilesetFile);
    }
    
    private Dimension calculateMapDimensions() {
        if (currentTileGrid == null || currentTileGrid.length == 0 || currentTileGrid[0].length == 0) {
            return new Dimension(0, 0);
        }
        
        int tileSize = tileVisualizer.getTileSize();
        return new Dimension(
            currentTileGrid.length * tileSize,
            currentTileGrid[0].length * tileSize
        );
    }
    
    private void updateVisualizersWithDimensions(Dimension mapDimensions) {
        // Update tile visualizer
        tileVisualizer.setPreferredSize(mapDimensions);
        tileVisualizer.revalidate();
        
        // Update grid visualizer
        gridVisualizer.setTileSize(tileVisualizer.getTileSize());
        gridVisualizer.updateMapDimensions(mapDimensions);
        
        // Update event visualizer
        eventVisualizer.setTileSize(tileVisualizer.getTileSize());
        eventVisualizer.updateEvents(currentMapEvents, mapDimensions);
    }
    
    private void handleMapLoadFailure() {
        resetVisualizers();
        currentMapId = DEFAULT_MAP_ID;
        currentMapJsonData = null;
        tileGridToolbar.setCurrentMapData(currentMapId, currentMapJsonData);
    }

    private void resetVisualizers() {
        currentTileGrid = null;
        currentTilesetId = DEFAULT_MAP_ID;
        currentMapEvents = new ArrayList<>();
        
        tileVisualizer.updateGrid(null, null);
        gridVisualizer.updateMapDimensions(new Dimension(0, 0));
        eventVisualizer.updateEvents(new ArrayList<>(), new Dimension(0, 0));
    }

    public void syncEventsToJson() {
        if (currentMapEvents == null || currentMapJsonData == null) {
            System.err.println("[EditorView] Cannot sync events: missing required data");
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                JSONArray eventsJsonArray = convertEventsToJsonArray();
                currentMapJsonData.put("events", eventsJsonArray);
                
                editor.setCurrentMapDataJson(currentMapJsonData);
                tileGridToolbar.setCurrentMapData(currentMapId, currentMapJsonData);
                
                System.out.println("[EditorView] Events synchronized successfully");
                
            } catch (Exception e) {
                System.err.println("[EditorView] Error synchronizing events: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    private JSONArray convertEventsToJsonArray() {
        JSONArray eventsArray = new JSONArray();
        
        for (Event event : currentMapEvents) {
            try {
                JSONObject eventJson = convertEventToJson(event);
                eventsArray.put(eventJson);
            } catch (Exception e) {
                System.err.println("[EditorView] Error converting event " + event.getId() + ": " + e.getMessage());
            }
        }
        
        return eventsArray;
    }

    private JSONObject convertEventToJson(Event event) {
        JSONObject eventJson = new JSONObject();
        
        eventJson.put("id", event.getId());
        eventJson.put("name", event.getName());
        eventJson.put("x", event.getX());
        eventJson.put("y", event.getY());
        
        JSONArray pagesArray = new JSONArray();
        if (event.getPages() != null) {
            for (EventPage page : event.getPages()) {
                JSONObject pageJson = convertEventPageToJson(page);
                pagesArray.put(pageJson);
            }
        }
        eventJson.put("pages", pagesArray);
        
        return eventJson;
    }

    private JSONObject convertEventPageToJson(EventPage page) {
        JSONObject pageJson = new JSONObject();
        
        pageJson.put("page_index", page.getPage_index());
        pageJson.put("move_type", page.getMove_type());
        pageJson.put("trigger", page.getTrigger());
        pageJson.put("through", page.isThrough());
        pageJson.put("move_frequency", page.getMoveFrequency());
        pageJson.put("always_on_top", page.isAlwaysOnTop());
        pageJson.put("walk_anime", page.isWalkAnime());
        pageJson.put("move_speed", page.getMoveSpeed());
        pageJson.put("step_anime", page.isStepAnime());
        pageJson.put("direction_fix", page.isDirectionFix());
        pageJson.put("graphic", page.getGraphic());
        pageJson.put("condition", page.getCondition());
        pageJson.put("move_route", page.getMoveRoute());
        
        JSONArray commandsArray = page.toCommandsJsonArray();
        pageJson.put("list", commandsArray);
        
        return pageJson;
    }
    
    // Getters for other components that might need access
    public Editor getEditor() { return editor; }
    public File getCurrentYmlFile() { return currentYmlFile; }
    public int[][][] getCurrentTileGrid() { return currentTileGrid; }
    public int getCurrentTilesetId() { return currentTilesetId; }
    public JSONObject getCurrentMapJsonData() { return currentMapJsonData; }
    public List<Event> getCurrentMapEvents() { return currentMapEvents; }
    public int getCurrentMapId() { return currentMapId; }
}