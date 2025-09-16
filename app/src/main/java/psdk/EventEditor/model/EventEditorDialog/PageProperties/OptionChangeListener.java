package psdk.EventEditor.model.EventEditorDialog.PageProperties;

/**
 * Interface pour écouter les changements d'options
 * Peut être utilisée si vous voulez notifier d'autres composants des changements
 */
public interface OptionChangeListener {
    
    /**
     * Appelée quand une option est modifiée
     * @param optionName le nom de l'option modifiée
     * @param newValue la nouvelle valeur
     * @param eventPage la page d'événement concernée
     */
    void onOptionChanged(String optionName, boolean newValue, psdk.EventEditor.model.EventPage eventPage);
}