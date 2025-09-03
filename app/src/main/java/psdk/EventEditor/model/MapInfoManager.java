package psdk.EventEditor.model;

import libs.json.JSONObject;
import libs.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MapInfoManager {

    private Map<Integer, String> mapNamesById; // Stocke les noms de map par ID
    private String projectRootPath;

    public MapInfoManager(String projectRootPath) {
        this.projectRootPath = projectRootPath;
        this.mapNamesById = new HashMap<>();
        loadMapInfos();
    }

    private void loadMapInfos() {
        File mapInfosYmlFile = new File(projectRootPath + File.separator + "Data", "MapInfos.rxdata.yml");

        if (!mapInfosYmlFile.exists()) {
            System.err.println("[MapInfoManager] Fichier MapInfos.rxdata.yml non trouvé à : " + mapInfosYmlFile.getAbsolutePath());
            return;
        }

        File jsonOutputFile = new File(projectRootPath + File.separator + "Data", "temp_map_infos.json");
        Path jsonOutputPath = jsonOutputFile.toPath();

        try {
            // Convertir MapInfos.rxdata.yml en JSON
            ProcessBuilder pb = new ProcessBuilder("python", "yml_to_json.py", mapInfosYmlFile.getAbsolutePath(), jsonOutputFile.getAbsolutePath());
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("[MapInfoManager] Erreur lors de la conversion de MapInfos.rxdata.yml (code de sortie: " + exitCode + ").");
                return;
            }

            String jsonContent = new String(Files.readAllBytes(jsonOutputPath));
            JSONObject mapInfosJson = new JSONObject(jsonContent);

            // Parcourir les clés (qui sont les IDs de map) et extraire les noms
            for (String key : mapInfosJson.keySet()) {
                try {
                    int mapId = Integer.parseInt(key);
                    JSONObject mapInfo = mapInfosJson.getJSONObject(key);
                    if (mapInfo.has("name")) {
                        mapNamesById.put(mapId, mapInfo.getString("name"));
                    } else {
                        System.err.println("[MapInfoManager] Clé 'name' non trouvée pour la map ID: " + mapId + " dans MapInfos.rxdata.yml.");
                    }
                } catch (NumberFormatException e) {
                    System.err.println("[MapInfoManager] Clé non numérique trouvée dans MapInfos.rxdata.yml: " + key);
                } catch (JSONException e) {
                    System.err.println("[MapInfoManager] Erreur JSON pour la map ID: " + key + " : " + e.getMessage());
                }
            }
            System.out.println("[MapInfoManager] MapInfos.rxdata.yml chargé. " + mapNamesById.size() + " noms de maps trouvés.");

        } catch (IOException | InterruptedException e) {
            System.err.println("[MapInfoManager] Erreur de lecture/conversion de MapInfos.rxdata.yml : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Supprimer le fichier JSON temporaire
            if (jsonOutputFile.exists()) {
                try {
                    Files.delete(jsonOutputPath);
                } catch (IOException e) {
                    System.err.println("Impossible de supprimer le fichier temporaire " + jsonOutputFile.getName() + ". Supprimez-le manuellement.");
                }
            }
        }
    }

    public String getMapNameById(int mapId) {
        return mapNamesById.getOrDefault(mapId, "Map" + String.format("%03d", mapId)); // Retourne un nom par défaut si non trouvé
    }
}