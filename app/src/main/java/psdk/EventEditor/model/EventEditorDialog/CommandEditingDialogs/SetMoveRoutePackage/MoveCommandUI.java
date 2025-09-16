package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRoutePackage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import libs.json.JSONArray;

/**
 * UI component builder for move command buttons
 */
public class MoveCommandUI {

    private final MoveCommandInsertCallback insertCallback;
    private final MoveCommandGraphicCallback graphicCallback;

    public interface MoveCommandInsertCallback {
        void insertCommand(int moveCode, String commandName, JSONArray parameters);
    }

    public interface MoveCommandGraphicCallback {
        void openGraphicDialog(int moveCode, String commandName);
    }

    public MoveCommandUI(MoveCommandInsertCallback insertCallback, MoveCommandGraphicCallback graphicCallback) {
        this.insertCallback = insertCallback;
        this.graphicCallback = graphicCallback;
    }

    /**
     * Create move command buttons grid
     */
    public void createMoveCommandButtons(JPanel panel, MoveCommandHandler commandHandler) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        int col = 0;
        int row = 0;
        final int COLUMNS_PER_ROW = 3;

        for (Map.Entry<String, Integer> entry : MoveCommandRegistry.MOVE_COMMAND_CODES.entrySet()) {
            String commandName = entry.getKey();
            int currentMoveCode = entry.getValue();

            JButton button = new JButton(commandName);
            button.addActionListener(e -> {
                if (currentMoveCode == 41) {
                    // Special handling for Change Graphic command
                    graphicCallback.openGraphicDialog(currentMoveCode, commandName);
                } else {
                    // Regular command handling
                    JSONArray parameters = commandHandler.insertCommandFromButton(currentMoveCode, commandName);
                    insertCallback.insertCommand(currentMoveCode, commandName, parameters);
                }
            });

            gbc.gridx = col;
            gbc.gridy = row;
            panel.add(button, gbc);

            col++;
            if (col >= COLUMNS_PER_ROW) {
                col = 0;
                row++;
            }
        }
    }
}