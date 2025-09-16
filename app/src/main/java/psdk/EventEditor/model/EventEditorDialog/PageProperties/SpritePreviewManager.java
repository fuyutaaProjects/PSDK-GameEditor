package psdk.EventEditor.model.EventEditorDialog.PageProperties;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import psdk.EventEditor.ConfigManager;

/**
 * Gestionnaire pour l'affichage des sprites de personnages dans les previews
 * Supporte l'extraction de frames spécifiques depuis les spritesheets RPG Maker XP
 */
public class SpritePreviewManager {
    
    private static final int DEFAULT_PREVIEW_WIDTH = 96;
    private static final int DEFAULT_PREVIEW_HEIGHT = 96;
    private static final String CHARACTERS_FOLDER = "Graphics" + File.separator + "Characters";
    
    // Constantes pour les spritesheets RPG Maker XP
    private static final int SPRITE_WIDTH = 32;   // Largeur d'une frame de sprite
    private static final int SPRITE_HEIGHT = 32;  // Hauteur d'une frame de sprite
    private static final int FRAMES_PER_CHARACTER = 3;  // 3 frames d'animation par direction
    private static final int DIRECTIONS_PER_CHARACTER = 4;  // 4 directions par personnage
    private static final int CHARACTERS_PER_ROW = 4;  // 4 personnages par ligne dans la spritesheet
    
    /**
     * Met à jour le preview d'un sprite dans un JLabel
     * @param previewLabel Le JLabel qui affichera le sprite
     * @param characterFileName Le nom du fichier du sprite (avec ou sans extension)
     */
    public static void updateSpritePreview(JLabel previewLabel, String characterFileName) {
        updateSpritePreview(previewLabel, characterFileName, 0, 2, 1, DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
    }
    
    /**
     * Met à jour le preview d'un sprite dans un JLabel avec paramètres spécifiques
     * @param previewLabel Le JLabel qui affichera le sprite
     * @param characterFileName Le nom du fichier du sprite (avec ou sans extension)
     * @param characterIndex Index du personnage dans la spritesheet (0-3 pour la première ligne, 4-7 pour la seconde, etc.)
     * @param direction Direction du personnage (2=Down, 4=Left, 6=Right, 8=Up)
     * @param pattern Pattern d'animation (0, 1, 2)
     */
    public static void updateSpritePreview(JLabel previewLabel, String characterFileName, 
                                         int characterIndex, int direction, int pattern) {
        updateSpritePreview(previewLabel, characterFileName, characterIndex, direction, pattern, 
                           DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
    }
    
    /**
     * Met à jour le preview d'un sprite dans un JLabel avec taille personnalisée
     * @param previewLabel Le JLabel qui affichera le sprite
     * @param characterFileName Le nom du fichier du sprite (avec ou sans extension)
     * @param characterIndex Index du personnage dans la spritesheet
     * @param direction Direction du personnage (2=Down, 4=Left, 6=Right, 8=Up)
     * @param pattern Pattern d'animation (0, 1, 2)
     * @param width Largeur du preview
     * @param height Hauteur du preview
     */
    public static void updateSpritePreview(JLabel previewLabel, String characterFileName, 
                                         int characterIndex, int direction, int pattern,
                                         int width, int height) {
        // Vérifier les paramètres
        if (previewLabel == null) {
            System.err.println("ERROR: Preview label is null");
            return;
        }
        
        // Handle empty or null character name
        if (characterFileName == null || characterFileName.isEmpty()) {
            setNoCharacterSelected(previewLabel);
            return;
        }

        String projectRootPath = ConfigManager.loadProjectPath();
        if (projectRootPath == null || projectRootPath.isEmpty()) {
            setNoProjectPath(previewLabel);
            return;
        }

        // Construire le chemin vers l'image
        String imagePath = buildImagePath(projectRootPath, characterFileName);
        System.out.println("DEBUG: Attempting to load character image from: " + imagePath);
        System.out.println("DEBUG: Character parameters - Index: " + characterIndex + ", Direction: " + direction + ", Pattern: " + pattern);

        // Vérifier si le fichier existe
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            setFileNotFound(previewLabel, characterFileName);
            return;
        }

        // Charger et extraire la frame spécifique
        extractAndDisplayFrame(previewLabel, imagePath, characterFileName, characterIndex, direction, pattern, width, height);
    }
    
    /**
     * Extrait une frame spécifique de la spritesheet et l'affiche
     */
    private static void extractAndDisplayFrame(JLabel previewLabel, String imagePath, String characterFileName,
                                             int characterIndex, int direction, int pattern, int width, int height) {
        try {
            // Charger l'image complète
            BufferedImage fullImage = ImageIO.read(new File(imagePath));
            
            if (fullImage == null) {
                setLoadError(previewLabel, characterFileName);
                System.err.println("ERROR: Could not load image from: " + imagePath);
                return;
            }
            
            // Calculer les coordonnées de la frame
            FrameCoordinates coords = calculateFrameCoordinates(characterIndex, direction, pattern, fullImage);
            
            if (coords == null) {
                setInvalidParameters(previewLabel, characterFileName);
                return;
            }
            
            // Extraire la frame
            BufferedImage frameImage = fullImage.getSubimage(coords.x, coords.y, coords.width, coords.height);
            
            // Redimensionner pour le preview
            Image scaledImage = frameImage.getScaledInstance(width, height, Image.SCALE_FAST);
            
            // Créer l'icône et l'assigner
            ImageIcon scaledIcon = new ImageIcon(scaledImage);
            previewLabel.setIcon(scaledIcon);
            previewLabel.setText(""); // Enlever le texte quand on a une image
            
            System.out.println("DEBUG: Frame extracted successfully - Position: (" + coords.x + "," + coords.y + "), Size: " + coords.width + "x" + coords.height);
            
        } catch (IOException e) {
            setLoadError(previewLabel, characterFileName);
            System.err.println("ERROR: IOException while loading character image: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            setLoadError(previewLabel, characterFileName);
            System.err.println("ERROR: Exception while processing character image: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
/**
 * Calcule les coordonnées d'une frame dans la spritesheet
 * Structure RPG Maker XP : 4x4 grid, Pattern détermine la colonne, Direction détermine la ligne
 */
private static FrameCoordinates calculateFrameCoordinates(int characterIndex, int direction, int pattern, BufferedImage fullImage) {
    // Convertir la direction RPG Maker XP en index de ligne (0-3)
    int directionIndex;
    switch (direction) {
        case 2: directionIndex = 0; break; // Down (ligne 1)
        case 4: directionIndex = 1; break; // Left (ligne 2)
        case 6: directionIndex = 2; break; // Right (ligne 3)
        case 8: directionIndex = 3; break; // Up (ligne 4)
        default: 
            System.err.println("ERROR: Invalid direction: " + direction);
            return null;
    }
    
    // Vérifier les paramètres - pattern peut aller de 0 à 3 pour une grille 4x4
    if (pattern < 0 || pattern >= 4) {
        System.err.println("ERROR: Invalid pattern: " + pattern + " (must be 0-3 for 4x4 grid)");
        return null;
    }
    
    // Dans une spritesheet 4x4 de RPG Maker XP :
    // - Pattern détermine la colonne (0-3)
    // - Direction détermine la ligne (0-3)
    // - Character Index n'affecte PAS la position dans cette sheet spécifique
    int frameX = pattern * SPRITE_WIDTH;
    int frameY = directionIndex * SPRITE_HEIGHT;
    
    // Vérifier que les coordonnées sont dans les limites de l'image
    if (frameX + SPRITE_WIDTH > fullImage.getWidth() || frameY + SPRITE_HEIGHT > fullImage.getHeight()) {
        System.err.println("ERROR: Frame coordinates out of bounds - Frame: (" + frameX + "," + frameY + "), Image size: " + fullImage.getWidth() + "x" + fullImage.getHeight());
        return null;
    }
    
    System.out.println("DEBUG: Calculated coordinates - Direction: " + direction + " (row " + directionIndex + "), Pattern: " + pattern + " (col " + pattern + ") → (" + frameX + "," + frameY + ")");
    
    return new FrameCoordinates(frameX, frameY, SPRITE_WIDTH, SPRITE_HEIGHT);
}
    
    /**
     * Classe pour stocker les coordonnées d'une frame
     */
    private static class FrameCoordinates {
        final int x, y, width, height;
        
        FrameCoordinates(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    /**
     * Construit le chemin complet vers l'image du sprite
     */
    private static String buildImagePath(String projectRootPath, String characterFileName) {
        // Ne pas ajouter .png si le fichier a déjà une extension
        if (characterFileName.toLowerCase().endsWith(".png")) {
            return new File(projectRootPath, CHARACTERS_FOLDER + File.separator + characterFileName).getAbsolutePath();
        } else {
            return new File(projectRootPath, CHARACTERS_FOLDER + File.separator + characterFileName + ".png").getAbsolutePath();
        }
    }
    
    // Méthodes pour les différents états d'affichage
    private static void setNoCharacterSelected(JLabel previewLabel) {
        previewLabel.setText("<html><center>No Character<br>Selected</center></html>");
        previewLabel.setIcon(null);
    }
    
    private static void setNoProjectPath(JLabel previewLabel) {
        previewLabel.setText("<html><center>No Project Path<br>Set</center></html>");
        previewLabel.setIcon(null);
        System.out.println("DEBUG: Cannot load character graphic, project path is not set.");
    }
    
    private static void setFileNotFound(JLabel previewLabel, String characterFileName) {
        previewLabel.setText("<html><center>File not found<br>" + characterFileName + "</center></html>");
        previewLabel.setIcon(null);
        System.err.println("ERROR: Character image file not found: " + characterFileName);
    }
    
    private static void setLoadError(JLabel previewLabel, String characterFileName) {
        previewLabel.setText("<html><center>Error loading<br>" + characterFileName + "</center></html>");
        previewLabel.setIcon(null);
    }
    
    private static void setInvalidParameters(JLabel previewLabel, String characterFileName) {
        previewLabel.setText("<html><center>Invalid parameters<br>" + characterFileName + "</center></html>");
        previewLabel.setIcon(null);
    }
    
    /**
     * Efface le preview (remet à l'état "No Character Selected")
     */
    public static void clearPreview(JLabel previewLabel) {
        if (previewLabel != null) {
            setNoCharacterSelected(previewLabel);
        }
    }
}