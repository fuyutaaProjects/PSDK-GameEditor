package psdk.EventEditor.model.EventEditorDialog;

import javax.swing.*;
import java.awt.*;

public class GraphicComponents {
    
    // Constants
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    private static final Dimension CHARACTER_PREVIEW_SIZE = new Dimension(96, 96);
    private static final Dimension BUTTON_SIZE = new Dimension(30, 25);
    private static final String[] DIRECTION_ARROWS = {"←", "→", "↓", "↑"};
    
    public JLabel characterPreview;
    public JButton characterButton;
    public JComboBox<String> directionCombo;
    
    public GraphicComponents() {
        initializeGraphicComponents();
    }
    
    private void initializeGraphicComponents() {
        characterPreview = createCharacterPreview();
        characterButton = createStyledButton("Change graphic", BUTTON_SIZE);
        directionCombo = new JComboBox<>(DIRECTION_ARROWS);
    }
    
    private JLabel createCharacterPreview() {
        JLabel preview = new JLabel();
        preview.setPreferredSize(CHARACTER_PREVIEW_SIZE);
        preview.setBorder(BorderFactory.createLoweredBevelBorder());
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.setVerticalAlignment(SwingConstants.CENTER);
        preview.setText("<html><center>No Character<br>Selected</center></html>");
        preview.setBackground(Color.WHITE);
        preview.setOpaque(true);
        return preview;
    }
    
    private JButton createStyledButton(String text, Dimension size) {
        JButton button = new JButton(text);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        if (size != null) {
            button.setPreferredSize(size);
        }
        return button;
    }
}