package psdk.EventEditor.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

import libs.json.JSONArray;
import libs.json.JSONObject;
import libs.json.JSONException;

public class Editor {

    private String rpgMakerProjectRootPath;
    private List<File> ymlFiles;
    private JSONObject currentMapDataJson;


    public Editor(String projectRootPath) {
        this.rpgMakerProjectRootPath = projectRootPath;
        ymlFiles = new ArrayList<>();
        loadYmlFiles();
    }

    public void setCurrentMapDataJson(JSONObject mapData) {
        this.currentMapDataJson = mapData;
    }

    public JSONObject getCurrentMapDataJson() {
        return this.currentMapDataJson;
    }

    private void loadYmlFiles() {
        if (rpgMakerProjectRootPath == null || rpgMakerProjectRootPath.isEmpty()) {
            System.err.println("RPG Maker project path not set. Cannot load map files.");
            return;
        }

        File dataFolder = new File(rpgMakerProjectRootPath, "Data");
        System.out.println("Absolute path of the RPG Maker project Data folder: " + dataFolder.getAbsolutePath());

        if (dataFolder.exists() && dataFolder.isDirectory()) {
            File[] files = dataFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".yml") && name.startsWith("Map")
            );
            
            if (files != null && files.length > 0) {
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        try {
                            int id1 = Integer.parseInt(f1.getName().substring(3, 6));
                            int id2 = Integer.parseInt(f2.getName().substring(3, 6));
                            return Integer.compare(id1, id2);
                        } catch (NumberFormatException e) {
                            System.err.println("Warning: Could not parse ID for sorting " + f1.getName() + " or " + f2.getName());
                            return f1.getName().compareTo(f2.getName()); 
                        }
                    }
                });

                for (File file : files) {
                    ymlFiles.add(file);
                    System.out.println("YAML/Map file found: " + file.getName());
                }
            } else {
                System.out.println("No map file (.yml) found in the Data folder: " + dataFolder.getAbsolutePath());
            }
        } else {
            System.err.println("The Data folder does not exist or is not a directory at the location: " + dataFolder.getAbsolutePath());
        }
    }

    public List<File> getYmlFiles() {
        return ymlFiles;
    }

    public JSONObject loadMapFromJson(File ymlFile) {
        File jsonOutputFile = new File(rpgMakerProjectRootPath, "temp_map_data.json"); 
        String pathToPythonScript = "yml_to_json.py"; 

        try {
            ProcessBuilder pb = new ProcessBuilder("python", pathToPythonScript, ymlFile.getAbsolutePath(), jsonOutputFile.getAbsolutePath());
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("Error converting YAML to JSON (exit code: " + exitCode + "). Check Python logs.");
                return null;
            }
            System.out.println("YAML to JSON conversion successful via yml_to_json.py.");

            String jsonContent = new String(Files.readAllBytes(jsonOutputFile.toPath()));
            JSONObject jsonData = new JSONObject(jsonContent);

            int eventCount = 0;
            if (jsonData.has("events")) {
                Object eventsObj = jsonData.get("events");
                if (eventsObj instanceof JSONArray) {
                    JSONArray eventsArray = (JSONArray) eventsObj;
                    eventCount = eventsArray.length();
                } else if (eventsObj instanceof JSONObject) {
                    JSONObject eventsMap = (JSONObject) eventsObj;
                    eventCount = eventsMap.length();
                }
            } else if (jsonData.has("map_data") && jsonData.getJSONObject("map_data").has("events")) {
                JSONObject mapData = jsonData.getJSONObject("map_data");
                Object eventsObj = mapData.get("events");
                if (eventsObj instanceof JSONArray) {
                    JSONArray eventsArray = (JSONArray) eventsObj;
                    eventCount = eventsArray.length();
                } else if (eventsObj instanceof JSONObject) {
                    JSONObject eventsMap = (JSONObject) eventsObj;
                    eventCount = eventsMap.length();
                }
            }
            System.out.println("DEBUG: Number of events detected on the map: " + eventCount);
            this.currentMapDataJson = jsonData;

            return jsonData;

        } catch (IOException | InterruptedException | JSONException e) {
            System.err.println("Error loading map via Python/JSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            /*
            try {
                Files.deleteIfExists(jsonOutputFile.toPath());
                System.out.println("Fichier temporaire " + jsonOutputFile.getName() + " supprimÃ©.");
            } catch (IOException e) {
                System.err.println("Erreur lors de la suppression du fichier temporaire : " + e.getMessage());
            }
                 */
        }
    }

    public boolean saveMapDataToYml(int mapId, JSONObject mapDataJson) {
        String tempJsonFilePath = rpgMakerProjectRootPath + File.separator + "temp_map_data_to_save.json";
        
        String outputYmlFilePath = rpgMakerProjectRootPath + File.separator + "Data" + File.separator + String.format("Map%03d.rxdata.yml", mapId); 

        String pathToPythonScript = "json_to_yml.py"; 

        try {
            try (FileWriter file = new FileWriter(tempJsonFilePath)) {
                file.write(mapDataJson.toString(2));
                System.out.println("Map data temporarily saved to: " + tempJsonFilePath);
            } catch (IOException e) {
                System.err.println("Error saving temporary JSON file: " + e.getMessage());
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder("python", pathToPythonScript, tempJsonFilePath, outputYmlFilePath);
            pb.inheritIO();
            Process p = pb.start();
            int exitCode = p.waitFor();

            if (exitCode == 0) {
                System.out.println("Successfully converted JSON to YAML: " + outputYmlFilePath);
                return true;
            } else {
                System.err.println("Python script exited with error code: " + exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing Python script: " + e.getMessage());
            return false;
        } finally {
            /* 
            try {
                Files.deleteIfExists(Paths.get(tempJsonFilePath));
                System.out.println("Temporary JSON file for YAML conversion deleted: " + tempJsonFilePath);
            } catch (IOException e) {
                System.err.println("Error deleting temporary JSON file: " + e.getMessage());
            }
            */
        }
        
    }


    public boolean exportMapDataToYml(int mapId, JSONObject mapDataJson) {
        String tempJsonFilePath = rpgMakerProjectRootPath + File.separator + "temp_map_data_to_export.json";
        
        String exportYmlFilePath = rpgMakerProjectRootPath + File.separator + "Data" + File.separator + mapId + "_export_test.yml"; 

        String pathToPythonScript = "json_to_yml.py"; 

        try {
            // Ã‰crire le JSON temporaire
            try (FileWriter file = new FileWriter(tempJsonFilePath)) {
                file.write(mapDataJson.toString(2));
                System.out.println("Map data temporarily saved for export to: " + tempJsonFilePath);
            } catch (IOException e) {
                System.err.println("Error saving temporary JSON file for export: " + e.getMessage());
                return false;
            }

            // ExÃ©cuter le script Python pour convertir JSON vers YAML
            ProcessBuilder pb = new ProcessBuilder("python", pathToPythonScript, tempJsonFilePath, exportYmlFilePath);
            pb.inheritIO();
            Process p = pb.start();
            int exitCode = p.waitFor();

            if (exitCode == 0) {
                System.out.println("Successfully exported JSON to YAML: " + exportYmlFilePath);
                return true;
            } else {
                System.err.println("Python script exited with error code during export: " + exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error executing Python script for export: " + e.getMessage());
            return false;
        } finally {
            // Nettoyer le fichier temporaire
            try {
                Files.deleteIfExists(new File(tempJsonFilePath).toPath());
                System.out.println("Temporary JSON file for export deleted: " + tempJsonFilePath);
            } catch (IOException e) {
                System.err.println("Error deleting temporary JSON file for export: " + e.getMessage());
            }
        }
    }

    /**
     * Exporte le JSON en mémoire de manière brute pour débogage
     * @param mapDataJson Le JSON à exporter
     * @return true si l'export a réussi, false sinon
     */
    public boolean exportRawJson(JSONObject mapDataJson) {
        if (mapDataJson == null) {
            System.err.println("No JSON data to export - mapDataJson is null");
            return false;
        }

        String rawJsonFilePath = rpgMakerProjectRootPath + File.separator + "debug_raw_json.json";

        try {
            try (FileWriter file = new FileWriter(rawJsonFilePath)) {
                file.write(mapDataJson.toString(2)); // Indentation de 2 pour lisibilité
                System.out.println("Raw JSON data exported to: " + rawJsonFilePath);
                System.out.println("JSON size: " + mapDataJson.toString().length() + " characters");
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error exporting raw JSON file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}