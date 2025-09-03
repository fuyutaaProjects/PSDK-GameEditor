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
    private static final Path CONFIG_PATH = Paths.get(CONFIG_FILE_NAME);
    
    public static void saveProjectPath(String path) {
        if (path == null) {
            System.err.println("Impossible de sauvegarder un chemin null");
            return;
        }
        
        // raw saving
        try {
            String content = PROJECT_PATH_KEY + "=" + path;
            Files.write(CONFIG_PATH, content.getBytes(StandardCharsets.UTF_8));
            System.out.println("Chemin du projet sauvegardé (raw) : " + path);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde raw : " + e.getMessage());
            
            Properties properties = loadProperties();
            properties.setProperty(PROJECT_PATH_KEY, path);
            
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "PSDK Event Editor Configuration");
                System.out.println("Chemin du projet sauvegardé (fallback) : " + path);
            } catch (IOException e2) {
                System.err.println("Erreur lors de la sauvegarde fallback : " + e2.getMessage());
            }
        }
    }
    
    public static String loadProjectPath() {
        if (!Files.exists(CONFIG_PATH)) {
            System.out.println("Fichier de configuration non trouvé");
            return null;
        }
        
        // reading raw
        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8);
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }
                
                if (line.startsWith(PROJECT_PATH_KEY + "=")) {
                    String path = line.substring((PROJECT_PATH_KEY + "=").length());
                    
                    if (path != null && !path.trim().isEmpty()) {
                        System.out.println("Chemin du projet chargé (raw) : " + path);
                        return path.trim();
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement raw : " + e.getMessage());
            
            // Fallback
            Properties properties = loadProperties();
            String path = properties.getProperty(PROJECT_PATH_KEY);
            
            if (path != null && !path.trim().isEmpty()) {
                System.out.println("Chemin du projet chargé (fallback) : " + path);
                return path.trim();
            }
        }
        
        return null;
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
        try {
            Files.deleteIfExists(CONFIG_PATH);
            System.out.println("Chemin du projet supprimé");
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }
}