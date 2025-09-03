package psdk.EventEditor.views;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventEditorDialog.CommandDisplayFormatter;
import psdk.EventEditor.model.EventEditorDialog.IndentCalculator;

public class EventCommandListCellRenderer extends DefaultListCellRenderer {

    private static final Color SELECTION_BG_COLOR = new Color(51, 153, 255);
    private static final Color SELECTION_FG_COLOR = Color.WHITE;
    private static final Color DEFAULT_FG_COLOR = Color.BLACK;
    private static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);

    private final CommandDisplayFormatter displayFormatter = new CommandDisplayFormatter();
    private final IndentCalculator indentCalculator = new IndentCalculator();

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        label.setFont(DEFAULT_FONT);
        if (isSelected) {
            label.setBackground(SELECTION_BG_COLOR);
            label.setForeground(SELECTION_FG_COLOR);
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(DEFAULT_FG_COLOR);
        }

        if (value instanceof EventCommand) {
            EventCommand command = (EventCommand) value;
            StringBuilder text = new StringBuilder();

            int indentLevel = indentCalculator.calculateIndent(command, list, index);

            for (int i = 0; i < indentLevel; i++) {
                text.append("  ");
            }

            text.append("◆ ");
            text.append(displayFormatter.getCommandDisplayName(command));

            label.setText(text.toString());
            label.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));
        } else {
            label.setText(value != null ? value.toString() : "");
        }
        return label;
    }
}