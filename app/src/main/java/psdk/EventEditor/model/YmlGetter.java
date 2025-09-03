package psdk.EventEditor.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import libs.json.JSONObject;

public class YmlGetter {

    // --- VOTRE ANCIENNE LOGIQUE getMapName QUI MARCHE POUR MapInfos.rxdata.yml ---
    // (Pas de changement ici, elle fonctionne comme voulu)
    public static String getMapName(File mapFile, String rpgMakerProjectRootPath) {
        String fileName = mapFile.getName();
        if (fileName.length() < 6 || !fileName.startsWith("Map") || !fileName.substring(3, 6).matches("\\d{3}")) {
            System.err.println("[YmlGetter] Nom de fichier de map invalide : " + fileName + ". Attendu format 'MapXXX.yml'.");
            return null;
        }
        
        String mapNumberStr = fileName.substring(3, 6);
        int mapNumber;
        try {
            mapNumber = Integer.parseInt(mapNumberStr);
        } catch (NumberFormatException e) {
            System.err.println("[YmlGetter] Numéro de map invalide dans le fichier " + fileName + " : " + e.getMessage());
            return null;
        }

        if (rpgMakerProjectRootPath == null || rpgMakerProjectRootPath.isEmpty()) {
            System.err.println("[YmlGetter] Chemin racine du projet RPG Maker non défini. Impossible de trouver MapInfos.rxdata.yml.");
            return null;
        }

        File mapInfosFile = new File(rpgMakerProjectRootPath + File.separator + "Data", "MapInfos.rxdata.yml");
        
        if (!mapInfosFile.exists()) {
            System.err.println("[YmlGetter] Fichier MapInfos.rxdata.yml non trouvé à : " + mapInfosFile.getAbsolutePath());
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(mapInfosFile))) {
            String line;
            boolean foundMapIdSection = false;
            String potentialBase64Content = null;

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();

                if (potentialBase64Content != null) {
                    try {
                        byte[] decodedBytes = Base64.getDecoder().decode(trimmedLine);
                        return new String(decodedBytes, StandardCharsets.UTF_8);
                    } catch (IllegalArgumentException decodeError) {
                        System.err.println("[YmlGetter] Erreur de décodage Base64 pour le nom de map (ligne: " + trimmedLine + ") : " + decodeError.getMessage());
                        return trimmedLine;
                    } finally {
                        potentialBase64Content = null;
                    }
                }
                
                if (!foundMapIdSection) {
                    if (trimmedLine.startsWith(String.valueOf(mapNumber) + ":")) {
                        foundMapIdSection = true;
                    }
                } else {
                    if (trimmedLine.startsWith("name:")) {
                        String[] parts = trimmedLine.split(":", 2);
                        if (parts.length >= 2) {
                            String rawName = parts[1].trim();
                            if (rawName.startsWith("!binary |-")) {
                                String base64Encoded = rawName.substring("!binary |-".length()).trim();
                                if (!base64Encoded.isEmpty()) {
                                    try {
                                        byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
                                        return new String(decodedBytes, StandardCharsets.UTF_8);
                                    } catch (IllegalArgumentException decodeError) {
                                        System.err.println("[YmlGetter] Erreur de décodage Base64 pour le nom de map (same line): " + decodeError.getMessage());
                                        return rawName;
                                    }
                                } else {
                                    potentialBase64Content = "!binary"; 
                                }
                            } else {
                                return rawName; 
                            }
                        }
                    }
                    if (potentialBase64Content == null && (trimmedLine.matches("^\\d+:") || (!line.startsWith(" ") && !trimmedLine.isEmpty()))) {
                       break; 
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[YmlGetter] Erreur de lecture du fichier MapInfos.rxdata.yml : " + e.getMessage());
        }
        System.err.println("[YmlGetter] Nom de map non trouvé pour le numéro " + mapNumber + " dans MapInfos.rxdata.yml.");
        return null;
    }
    // --- FIN VOTRE ANCIENNE LOGIQUE getMapName ---


    // --- MÉTHODES BASÉES SUR LE JSON (CORRIGÉES : accès direct aux clés) ---
    public static int getTilesetIdFromJson(JSONObject mapJsonData) {
        if (mapJsonData == null) {
            System.err.println("[YmlGetter] getTilesetIdFromJson: mapJsonData est null.");
            return 0;
        }
        // ACCÈS DIRECT À 'tileset_id'
        if (!mapJsonData.has("tileset_id")) {
            System.err.println("[YmlGetter] getTilesetIdFromJson: JSON ne contient pas la clé 'tileset_id'. Contenu: " + mapJsonData.toString());
            return 0;
        }

        try {
            return mapJsonData.optInt("tileset_id", 0); // Utilise optInt pour la robustesse
        } catch (Exception e) {
            System.err.println("[YmlGetter] Erreur lors de la lecture de 'tileset_id' en tant qu'entier : " + e.getMessage());
            return 0;
        }
    }

    public static int getMapWidthFromJson(JSONObject mapJsonData) {
        if (mapJsonData == null) {
            System.err.println("[YmlGetter] getMapWidthFromJson: mapJsonData est null.");
            return 0;
        }
        // ACCÈS DIRECT À 'width'
        if (!mapJsonData.has("width")) {
            System.err.println("[YmlGetter] getMapWidthFromJson: JSON ne contient pas la clé 'width'.");
            return 0;
        }
        return mapJsonData.optInt("width", 0);
    }

    public static int getMapHeightFromJson(JSONObject mapJsonData) {
        if (mapJsonData == null) {
            System.err.println("[YmlGetter] getMapHeightFromJson: mapJsonData est null.");
            return 0;
        }
        // ACCÈS DIRECT À 'height'
        if (!mapJsonData.has("height")) {
            System.err.println("[YmlGetter] getMapHeightFromJson: JSON ne contient pas la clé 'height'.");
            return 0;
        }
        return mapJsonData.optInt("height", 0);
    }
}