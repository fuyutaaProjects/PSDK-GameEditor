package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import psdk.EventEditor.model.EventPage;

/**
 * Version améliorée avec système de notifications pour les changements
 */
public class PagePropertiesPanel extends JPanel {
    
    // Constants
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    // Model
    private EventPage currentEventPage;
    
    // UI Components - organized by section
    private ConditionComponents conditionComponents;
    private GraphicComponents graphicComponents;
    private MovementComponents movementComponents;
    private OptionComponents optionComponents;
    private TriggerComponents triggerComponents;
    
    // Panel creators
    private PanelCreator panelCreator;
    
    // Listeners pour les changements
    private List<OptionChangeListener> optionChangeListeners = new ArrayList<>();
    
    // Flag pour éviter les sauvegardes pendant les mises à jour programmatiques
    private boolean isUpdatingFromModel = false;

    public PagePropertiesPanel() {
        initializePanel();
        createComponents();
        layoutComponents();
        setupPagePropertiesListeners();
    }

    // ========== INITIALIZATION ==========
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
    }
    
    private void createComponents() {
        conditionComponents = new ConditionComponents();
        graphicComponents = new GraphicComponents();
        movementComponents = new MovementComponents();
        optionComponents = new OptionComponents();
        triggerComponents = new TriggerComponents();
        panelCreator = new PanelCreator(BACKGROUND_COLOR);
    }
    
    private void layoutComponents() {
        JPanel contentPanel = createMainContentPanel();
        add(new JScrollPane(contentPanel), BorderLayout.CENTER);
    }
    
    private JPanel createMainContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add sections with spacing
        contentPanel.add(panelCreator.createConditionsPanel(conditionComponents));
        contentPanel.add(Box.createVerticalStrut(5));
        
        contentPanel.add(createGraphicMovementPanel());
        contentPanel.add(Box.createVerticalStrut(5));
        
        contentPanel.add(createOptionsTriggersPanel());
        contentPanel.add(Box.createVerticalGlue());
        
        return contentPanel;
    }
    
    private JPanel createGraphicMovementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(panelCreator.createGraphicPanel(graphicComponents), BorderLayout.WEST);
        panel.add(panelCreator.createMovementPanel(movementComponents), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createOptionsTriggersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.add(panelCreator.createOptionsPanel(optionComponents), BorderLayout.WEST);
        panel.add(panelCreator.createTriggerPanel(triggerComponents), BorderLayout.CENTER);
        return panel;
    }

    // ========== LISTENERS ==========
    
    private void setupPagePropertiesListeners() {
        PagePropertiesListeners.setupConditionListeners(conditionComponents);
        PagePropertiesListeners.setupGraphicListeners(graphicComponents, this);
        PagePropertiesListeners.setupMovementListeners(movementComponents);
        PagePropertiesListeners.setupOptionListeners(optionComponents, this);
    }

    // ========== PUBLIC API METHODS ==========
    
    public void updatePageProperties(EventPage page) {
        isUpdatingFromModel = true; // disalle saves during update
        
        try {
            if (page == null) {
                PagePropertiesUpdater.clearAllFields(conditionComponents, graphicComponents, 
                    movementComponents, optionComponents, triggerComponents);
                return;
            }
            
            currentEventPage = page;
            PagePropertiesUpdater.updateAllSections(page, conditionComponents, graphicComponents,
                movementComponents, optionComponents, triggerComponents);
                
        } finally {
            isUpdatingFromModel = false; // re-enable saves after update
        }
    }
    
    // ========== GETTERS ==========
    
    public EventPage getCurrentEventPage() {
        return currentEventPage;
    }
    
    // ========== SAVE METHODS ==========
    
    /**
     * Sauvegarde un changement d'option dans l'EventPage courante
     */
    public void saveOptionChange(String optionName, boolean value) {
        if (currentEventPage == null || isUpdatingFromModel) {
            return; // Ne pas sauvegarder si pas de page courante ou si on est en train de mettre à jour
        }
        
        try {
            boolean oldValue = getCurrentOptionValue(optionName);
            
            // Ne sauvegarder que si la valeur a vraiment changé
            if (oldValue == value) {
                return;
            }
            
            switch (optionName) {
                case "walkAnime":
                    currentEventPage.setWalkAnime(value);
                    break;
                case "stepAnime":
                    currentEventPage.setStepAnime(value);
                    break;
                case "directionFix":
                    currentEventPage.setDirectionFix(value);
                    break;
                case "through":
                    currentEventPage.setThrough(value);
                    break;
                case "alwaysOnTop":
                    currentEventPage.setAlwaysOnTop(value);
                    break;
                default:
                    System.err.println("Option inconnue : " + optionName);
                    return;
            }
            
            // Notifier les listeners
            notifyOptionChanged(optionName, value);
            
            // Déclencher un événement PropertyChange pour notifier que les données ont changé
            firePropertyChange("pageModified", false, true);
            
            System.out.println("Option " + optionName + " sauvegardée avec la valeur : " + value);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde de l'option " + optionName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Récupère la valeur actuelle d'une option depuis l'EventPage
     */
    private boolean getCurrentOptionValue(String optionName) {
        if (currentEventPage == null) return false;
        
        switch (optionName) {
            case "walkAnime": return currentEventPage.isWalkAnime();
            case "stepAnime": return currentEventPage.isStepAnime();
            case "directionFix": return currentEventPage.isDirectionFix();
            case "through": return currentEventPage.isThrough();
            case "alwaysOnTop": return currentEventPage.isAlwaysOnTop();
            default: return false;
        }
    }
    
    // ========== LISTENER MANAGEMENT ==========
    
    /**
     * Ajoute un listener pour les changements d'options
     */
    public void addOptionChangeListener(OptionChangeListener listener) {
        if (!optionChangeListeners.contains(listener)) {
            optionChangeListeners.add(listener);
        }
    }
    
    /**
     * Supprime un listener pour les changements d'options
     */
    public void removeOptionChangeListener(OptionChangeListener listener) {
        optionChangeListeners.remove(listener);
    }
    
    /**
     * Notifie tous les listeners d'un changement d'option
     */
    private void notifyOptionChanged(String optionName, boolean newValue) {
        for (OptionChangeListener listener : optionChangeListeners) {
            try {
                listener.onOptionChanged(optionName, newValue, currentEventPage);
            } catch (Exception e) {
                System.err.println("Erreur lors de la notification du listener: " + e.getMessage());
            }
        }
    }
}