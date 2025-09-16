package psdk.EventEditor.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * Utility class for setting up common key bindings in dialog windows.
 * Provides standardized shortcuts for dialog operations like save and close.
 */
public class DialogKeyBindingUtils {
    
    /**
     * Interface for dialogs that support save and close operations.
     */
    public interface SaveableDialog {
        /**
         * Called when the dialog should save changes and close.
         * This method should handle the save operation and dispose of the dialog.
         */
        void saveAndClose();
        
        /**
         * Called when the dialog should close without saving.
         * This method should dispose of the dialog without saving changes.
         */
        default void cancelAndClose() {
            if (this instanceof JDialog) {
                ((JDialog) this).dispose();
            }
        }
    }
    
    /**
     * Sets up standard key bindings for a dialog.
     * - Shift+Enter: Save and close
     * - Escape: Cancel and close
     * 
     * @param dialog The dialog to set up key bindings for. Must implement SaveableDialog.
     */
    public static void setupStandardKeyBindings(JDialog dialog, SaveableDialog saveableDialog) {
        JRootPane rootPane = dialog.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        // Shift+Enter: Save and close
        KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(shiftEnter, "saveAndClose");
        actionMap.put("saveAndClose", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveableDialog.saveAndClose();
            }
        });
        
        // Escape: Cancel and close
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        inputMap.put(escape, "cancelAndClose");
        actionMap.put("cancelAndClose", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveableDialog.cancelAndClose();
            }
        });
    }
    
    /**
     * Sets up only the save and close key binding (Shift+Enter).
     * Use this when you don't want the Escape key binding or want to handle it separately.
     * 
     * @param dialog The dialog to set up key bindings for. Must implement SaveableDialog.
     */
    public static void setupSaveKeyBinding(JDialog dialog, SaveableDialog saveableDialog) {
        JRootPane rootPane = dialog.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        // Shift+Enter: Save and close
        KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        inputMap.put(shiftEnter, "saveAndClose");
        actionMap.put("saveAndClose", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveableDialog.saveAndClose();
            }
        });
    }
    
    /**
     * Sets up custom key bindings for a dialog.
     * 
     * @param dialog The dialog to set up key bindings for
     * @param keyStroke The key combination to bind
     * @param actionName A unique name for this action
     * @param action The action to perform when the key combination is pressed
     */
    public static void setupCustomKeyBinding(JDialog dialog, KeyStroke keyStroke, String actionName, AbstractAction action) {
        JRootPane rootPane = dialog.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        inputMap.put(keyStroke, actionName);
        actionMap.put(actionName, action);
    }
    
    /**
     * Convenience method to create a KeyStroke from key code and modifiers.
     * 
     * @param keyCode The key code (e.g., KeyEvent.VK_ENTER)
     * @param modifiers The modifier mask (e.g., KeyEvent.SHIFT_DOWN_MASK)
     * @return The KeyStroke object
     */
    public static KeyStroke createKeyStroke(int keyCode, int modifiers) {
        return KeyStroke.getKeyStroke(keyCode, modifiers);
    }
    
    /**
     * Common key combinations as constants for convenience.
     */
    public static final class CommonKeyStrokes {
        public static final KeyStroke SHIFT_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK);
        public static final KeyStroke CTRL_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        public static final KeyStroke ESCAPE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        public static final KeyStroke CTRL_S = KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
        public static final KeyStroke F1 = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
    }
}