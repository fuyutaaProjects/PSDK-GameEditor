package psdk.EventEditor;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import psdk.EventEditor.views.EditorView;
import psdk.EventEditor.views.MainMenu;

public class Core {
    // Constantes pour les noms des views
    private static final String MAIN_MENU_VIEW = "MainMenu";
    private static final String EDITOR_VIEW = "Editor";
    
    private static JFrame frame;
    private static JPanel cardPanel;
    private static CardLayout cardLayout;
    private static String currentProjectPath;
    
    // Références aux views principales
    private static MainMenu mainMenu;
    private static EditorView editorView;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            initializeUI();
            loadInitialProject();
            frame.setVisible(true);
            showView(MAIN_MENU_VIEW);
        });
    }
    
    private static void initializeUI() {
        frame = new JFrame("PSDK RMXP Project Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        frame.setContentPane(cardPanel);
    }
    
    private static void loadInitialProject() {
        try {
            currentProjectPath = ConfigManager.loadProjectPath();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du projet : " + e.getMessage());
            currentProjectPath = null;
        }
        
        // Créer le menu principal
        mainMenu = new MainMenu(currentProjectPath);
        cardPanel.add(mainMenu, MAIN_MENU_VIEW);
    }

    public static void showView(String viewName) {
        cardLayout.show(cardPanel, viewName);
    }

    public static void showEditor() {
        try {
            // Recharger le path depuis la config
            currentProjectPath = ConfigManager.loadProjectPath();
            
            if (currentProjectPath == null || currentProjectPath.trim().isEmpty()) {
                System.err.println("Aucun projet configuré");
                return;
            }
            
            // Créer ou recréer l'editor view si nécessaire
            if (editorView != null) {
                cardPanel.remove(editorView);
            }
            
            editorView = new EditorView(currentProjectPath);
            cardPanel.add(editorView, EDITOR_VIEW);
            
            // Afficher l'editor
            showView(EDITOR_VIEW);
            
            // Rafraîchir l'UI
            frame.revalidate();
            frame.repaint();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ouverture de l'editor : " + e.getMessage());
            // Retourner au menu principal en cas d'erreur
            showView(MAIN_MENU_VIEW);
        }
    }
    
    public static void showMainMenu() {
        showView(MAIN_MENU_VIEW);
    }

    public static String getCurrentProjectPath() {
        return currentProjectPath;
    }
    
    public static JFrame getMainFrame() {
        return frame;
    }
}