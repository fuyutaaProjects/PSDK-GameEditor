package psdk.EventEditor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ConfigManager {
    
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String PROJECT_PATH_KEY = "rpg_maker_project_path";
    private static final String LAST_OPENED_MAP_KEY = "last_opened_map";
    private static final Path CONFIG_PATH = Paths.get(CONFIG_FILE_NAME);
    
    public static void saveProjectPath(String path) {
        if (path == null) {
            System.err.println("Impossible de sauvegarder un chemin null");
            return;
        }
        
        // Use Properties to preserve all existing keys
        Properties properties = loadProperties();
        properties.setProperty(PROJECT_PATH_KEY, path);
        
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(output, "PSDK Event Editor Configuration");
            System.out.println("Chemin du projet sauvegardé : " + path);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde : " + e.getMessage());
        }
    }
    
    public static String loadProjectPath() {
        Properties properties = loadProperties();
        String path = properties.getProperty(PROJECT_PATH_KEY);
        
        if (path != null && !path.trim().isEmpty()) {
            System.out.println("Chemin du projet chargé : " + path);
            return path.trim();
        }
        
        return null;
    }
    
    /**
     * Save the path of the last opened map file
     * @param mapFilePath The absolute path to the map file
     */
    public static void saveLastOpenedMap(String mapFilePath) {
        if (mapFilePath == null || mapFilePath.trim().isEmpty()) {
            System.err.println("Cannot save null or empty map file path");
            return;
        }
        
        Properties properties = loadProperties();
        properties.setProperty(LAST_OPENED_MAP_KEY, mapFilePath);
        
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(output, "PSDK Event Editor Configuration");
            System.out.println("Last opened map saved: " + mapFilePath);
        } catch (IOException e) {
            System.err.println("Error saving last opened map: " + e.getMessage());
        }
    }
    
    /**
     * Load the path of the last opened map file
     * @return The path to the last opened map file, or null if not found
     */
    public static String loadLastOpenedMap() {
        Properties properties = loadProperties();
        String mapPath = properties.getProperty(LAST_OPENED_MAP_KEY);
        
        if (mapPath != null && !mapPath.trim().isEmpty()) {
            // Verify that the file still exists
            Path mapFile = Paths.get(mapPath);
            if (Files.exists(mapFile)) {
                System.out.println("Last opened map loaded: " + mapPath);
                return mapPath.trim();
            } else {
                System.out.println("Last opened map file no longer exists: " + mapPath);
                // Clear the invalid path
                clearLastOpenedMap();
            }
        }
        
        return null;
    }
    
    /**
     * Clear the last opened map from configuration
     */
    public static void clearLastOpenedMap() {
        Properties properties = loadProperties();
        properties.remove(LAST_OPENED_MAP_KEY);
        
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(output, "PSDK Event Editor Configuration");
            System.out.println("Last opened map cleared");
        } catch (IOException e) {
            System.err.println("Error clearing last opened map: " + e.getMessage());
        }
    }
    
    private static Properties loadProperties() {
        Properties properties = new Properties();
        
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la configuration : " + e.getMessage());
            }
        }
        
        return properties;
    }
    
    public static boolean hasValidProject() {
        String path = loadProjectPath();
        return path != null && !path.trim().isEmpty();
    }
    
    public static void clearProjectPath() {
        Properties properties = loadProperties();
        properties.remove(PROJECT_PATH_KEY);
        properties.remove(LAST_OPENED_MAP_KEY); // Also clear last opened map when clearing project
        
        try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(output, "PSDK Event Editor Configuration");
            System.out.println("Chemin du projet supprimé");
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}