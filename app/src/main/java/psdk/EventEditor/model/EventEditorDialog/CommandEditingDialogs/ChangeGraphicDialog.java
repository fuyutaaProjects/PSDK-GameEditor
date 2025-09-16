package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import psdk.EventEditor.ConfigManager;
import psdk.EventEditor.Core;
import psdk.EventEditor.utils.DialogKeyBindingUtils;


public class ChangeGraphicDialog extends JDialog implements DialogKeyBindingUtils.ConfirmableDialog {

    private static final int GRID_COLS = 4;      // Columns in the spritesheet (0-3 for X)
    private static final int GRID_ROWS = 4;      // Rows in the spritesheet (0-3 for Y)
    
    private static final int DEFAULT_SPRITE_WIDTH = 32; 
    private static final int DEFAULT_SPRITE_HEIGHT = 48; 

    private JList<String> graphicFileList;
    private DefaultListModel<String> graphicFileListModel;
    private JPanel previewPanel; 
    private JSpinner colSpinner; // Represents the actual x-coordinate (0-3) for UI display
    private JSpinner rowSpinner; // Represents the actual y-coordinate (0-3) for UI display

    private String selectedGraphicName;
    private int selectedCol;
    private int selectedRow;
    private int hueValue = 0; // This tool doesn't allow hue changes.

    private boolean okPressed = false;
    private BufferedImage currentFullSpritesheet;

    /**
     * Constructor for the Change Graphic dialog.
     * @param owner The parent Dialog for modality.
     * @param initialGraphicName The initial graphic file name (e.g., "amanda_walk").
     * @param initialEncodedX The initial X-coordinate from RPG Maker's encoded format (e.g., 2, 4, 6, 8).
     * @param initialY The initial Y-coordinate from RPG Maker's direct format (e.g., 0, 1, 2, 3).
     * @param initialHue The initial Hue value (ignored, forced to 0).
     */
    public ChangeGraphicDialog(Dialog owner, String initialGraphicName, int initialEncodedX, int initialY, int initialHue) {
        super(owner, "Change Graphic", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setResizable(true);
        setLocationRelativeTo(owner);

        System.out.println("\nDEBUG: Constructor called with:");
        System.out.println("  initialGraphicName: " + initialGraphicName);
        System.out.println("  initialEncodedX (from RMXP JSON): " + initialEncodedX);
        System.out.println("  initialY (from RMXP JSON): " + initialY);
        System.out.println("  initialHue: " + initialHue);

        this.selectedGraphicName = initialGraphicName;
        
        // DECODE initialEncodedX to get the actual column (x) (0-3) for our UI
        // From RPG Maker: encodedX = 2 * x + 2  =>  x = (encodedX - 2) / 2
        this.selectedCol = (initialEncodedX - 2) / 2;
        // initialY is already the correct row (y) (0-3) for our UI
        this.selectedRow = initialY;

        System.out.println("DEBUG: Decoded coordinates for UI (selectedCol, selectedRow):");
        System.out.println("  selectedCol (actual X for display): " + selectedCol);
        System.out.println("  selectedRow (actual Y for display): " + selectedRow);

        // Ensure decoded col/row are within bounds (0 to GRID_COLS-1 / GRID_ROWS-1)
        this.selectedCol = Math.max(0, Math.min(selectedCol, GRID_COLS - 1));
        this.selectedRow = Math.max(0, Math.min(selectedRow, GRID_ROWS - 1));
        
        System.out.println("DEBUG: Clamped coordinates for UI (after bounds check):");
        System.out.println("  selectedCol (clamped): " + selectedCol);
        System.out.println("  selectedRow (clamped): " + selectedRow);

        this.hueValue = 0; // Always force to 0, the tool doesn't support hue value changes.

        initComponents();
        loadGraphicFiles();
        selectInitialGraphic();
        updatePreview();
        setupKeyBindings(); 
    }

    private void setupKeyBindings() {
        DialogKeyBindingUtils.setupConfirmableKeyBindings(this, this);
    }

    @Override
    public void acceptAndClose() {
        onOk();
    }

    @Override
    public void cancelAndClose() {
        onCancel();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left Panel: Graphic File List
        graphicFileListModel = new DefaultListModel<>();
        graphicFileList = new JList<>(graphicFileListModel);
        graphicFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Listener for list selection changes (mouse or keyboard navigation)
        graphicFileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && graphicFileList.getSelectedValue() != null) {
                String newSelection = graphicFileList.getSelectedValue();
                if (!newSelection.equals(selectedGraphicName)) {
                    selectedGraphicName = newSelection;

                    selectedCol = 0;
                    selectedRow = 0;
                    colSpinner.setValue(selectedCol);
                    rowSpinner.setValue(selectedRow);
                    System.out.println("DEBUG: Graphic file changed to " + selectedGraphicName + ", resetting UI selection to (0,0).");
                    updatePreview();
                }
            }
        });

        // Key Listener for JList (Arrows, Enter, Space)
        graphicFileList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
                    onOk();
                    e.consume(); // Consume the event to prevent default JList behavior
                }
            }
        });

        // Double-click on a sprite in the spritesheet to select and close
        graphicFileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    onOk();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(graphicFileList);
        scrollPane.setPreferredSize(new Dimension(180, 0));
        mainPanel.add(scrollPane, BorderLayout.WEST);

        // Center Panel: Preview and Spinners
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEtchedBorder());

        // Custom JPanel to display the spritesheet preview from the file we selected, with a rectangle that highlights which frame we chose.
        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (currentFullSpritesheet != null) {
                    double scaleX = (double) getWidth() / currentFullSpritesheet.getWidth();
                    double scaleY = (double) getHeight() / currentFullSpritesheet.getHeight();
                    double scale = Math.min(scaleX, scaleY);

                    int imgWidth = (int) (currentFullSpritesheet.getWidth() * scale);
                    int imgHeight = (int) (currentFullSpritesheet.getHeight() * scale);

                    int xOffset = (getWidth() - imgWidth) / 2;
                    int yOffset = (getHeight() - imgHeight) / 2;

                    g.drawImage(currentFullSpritesheet, xOffset, yOffset, imgWidth, imgHeight, this);

                    if (selectedGraphicName != null && !selectedGraphicName.isEmpty() && !"(None)".equals(selectedGraphicName)) {
                        int spriteDisplayWidth = imgWidth / GRID_COLS;
                        int spriteDisplayHeight = imgHeight / GRID_ROWS;

                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setColor(Color.RED);
                        g2d.setStroke(new BasicStroke(2));

                        g2d.drawRect(xOffset + selectedCol * spriteDisplayWidth, 
                                     yOffset + selectedRow * spriteDisplayHeight,
                                     spriteDisplayWidth, spriteDisplayHeight);
                        g2d.dispose();
                        System.out.println("DEBUG: Drawing selection rectangle at UI coords: (" + selectedCol + "," + selectedRow + ")");
                    }
                } else {
                    String message = "No Graphic Selected or Error Loading Image";
                    FontMetrics fm = g.getFontMetrics();
                    int textWidth = fm.stringWidth(message);
                    int textHeight = fm.getHeight();
                    g.drawString(message, (getWidth() - textWidth) / 2, (getHeight() - textHeight) / 2 + fm.getAscent());
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(DEFAULT_SPRITE_WIDTH * GRID_COLS * 3, DEFAULT_SPRITE_HEIGHT * GRID_ROWS * 3); 
            }
        };

        // Mouse listener for selecting sprites on the preview panel
        previewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentFullSpritesheet == null || "(None)".equals(selectedGraphicName)) {
                    return; // No image or "(None)" selected, no selection possible
                }

                double scaleX = (double) previewPanel.getWidth() / currentFullSpritesheet.getWidth();
                double scaleY = (double) previewPanel.getHeight() / currentFullSpritesheet.getHeight();
                double scale = Math.min(scaleX, scaleY);

                int imgWidth = (int) (currentFullSpritesheet.getWidth() * scale);
                int imgHeight = (int) (currentFullSpritesheet.getHeight() * scale);

                int xOffset = (previewPanel.getWidth() - imgWidth) / 2;
                int yOffset = (previewPanel.getHeight() - imgHeight) / 2;

                // Check if the click is within the image bounds
                if (e.getX() >= xOffset && e.getX() < xOffset + imgWidth &&
                    e.getY() >= yOffset && e.getY() < yOffset + imgHeight) {

                    int spriteDisplayWidth = imgWidth / GRID_COLS;
                    int spriteDisplayHeight = imgHeight / GRID_ROWS;

                    // Calculate the clicked column and row (0-3)
                    int clickedCol = (e.getX() - xOffset) / spriteDisplayWidth;
                    int clickedRow = (e.getY() - yOffset) / spriteDisplayHeight;

                    // Ensure values are within bounds (0-3)
                    clickedCol = Math.max(0, Math.min(clickedCol, GRID_COLS - 1));
                    clickedRow = Math.max(0, Math.min(clickedRow, GRID_ROWS - 1));

                    // Update selected values and spinners if they changed
                    if (selectedCol != clickedCol || selectedRow != clickedRow) {
                        selectedCol = clickedCol;
                        selectedRow = clickedRow;
                        colSpinner.setValue(selectedCol);
                        rowSpinner.setValue(selectedRow);
                        System.out.println("DEBUG: Mouse clicked, new UI selection: (" + selectedCol + "," + selectedRow + ")");
                        previewPanel.repaint();
                    }
                }
            }
        });


        centerPanel.add(previewPanel, BorderLayout.CENTER);

        JPanel spinnerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        spinnerPanel.add(new JLabel("Column (0-3):"));
        colSpinner = new JSpinner(new SpinnerNumberModel(selectedCol, 0, GRID_COLS - 1, 1));
        colSpinner.addChangeListener(e -> {
            selectedCol = (int) colSpinner.getValue();
            System.out.println("DEBUG: Spinner Col changed to: " + selectedCol);
            previewPanel.repaint();
        });
        spinnerPanel.add(colSpinner);

        spinnerPanel.add(new JLabel("Row (0-3):"));
        rowSpinner = new JSpinner(new SpinnerNumberModel(selectedRow, 0, GRID_ROWS - 1, 1));
        rowSpinner.addChangeListener(e -> {
            selectedRow = (int) rowSpinner.getValue();
            System.out.println("DEBUG: Spinner Row changed to: " + selectedRow);
            previewPanel.repaint();
        });
        spinnerPanel.add(rowSpinner);

        centerPanel.add(spinnerPanel, BorderLayout.SOUTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel: OK/Cancel Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        
        // Request focus on the list initially
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                graphicFileList.requestFocusInWindow();
            }
        });
    }

    /**
     * Constructs the full path to the Graphics/Characters directory within the RPG Maker project.
     * Uses Core.getRpgMakerProjectPath() to get the project's root directory.
     * @return The absolute path to the Characters graphics directory.
     */
    private String getGraphicsCharactersPath() {
        String projectPath = ConfigManager.loadProjectPath();
        if (projectPath == null || projectPath.isEmpty()) {
            System.err.println("Warning: RPG Maker Project Path is not set in Core. Falling back to local 'graphics/characters'.");
            return "graphics" + File.separator + "characters"; 
        }
        // RPG Maker XP uses "Graphics/Characters"
        return projectPath + File.separator + "Graphics" + File.separator + "Characters";
    }

    private void loadGraphicFiles() {
        File graphicsDir = new File(getGraphicsCharactersPath()); // Use the new method
        System.out.println("DEBUG: Loading graphic files from: " + graphicsDir.getAbsolutePath());
        if (graphicsDir.exists() && graphicsDir.isDirectory()) {
            List<String> fileNames = new ArrayList<>();
            // Add "(None)" option at the top
            fileNames.add("(None)");
            File[] files = graphicsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName)); // Sort alphabetically
                for (File file : files) {
                    fileNames.add(file.getName().substring(0, file.getName().length() - 4)); // Remove .png extension
                }
            }
            graphicFileListModel.clear();
            for (String name : fileNames) {
                graphicFileListModel.addElement(name);
            }
            System.out.println("DEBUG: Loaded " + (fileNames.size() - 1) + " graphic files.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Graphics directory not found: " + graphicsDir.getAbsolutePath() + "\n" +
                    "Please ensure '" + getGraphicsCharactersPath() + "' exists relative to your application's working directory or the specified RPG Maker project path.",
                    "Error Loading Graphics", JOptionPane.ERROR_MESSAGE);
            graphicFileListModel.addElement("(Error: Directory Not Found)");
            graphicFileList.setEnabled(false); // Disable list if directory not found
            System.err.println("ERROR: Graphics directory not found: " + graphicsDir.getAbsolutePath());
        }
    }

    private void selectInitialGraphic() {
        if (selectedGraphicName != null && !selectedGraphicName.isEmpty()) {
            graphicFileList.setSelectedValue(selectedGraphicName, true);
            System.out.println("DEBUG: Attempting to select initial graphic: " + selectedGraphicName);
        } else {
            graphicFileList.setSelectedValue("(None)", true);
            System.out.println("DEBUG: Initial graphic name was null/empty, selecting (None).");
        }
        // Ensure spinners reflect the loaded or default values
        colSpinner.setValue(selectedCol);
        rowSpinner.setValue(selectedRow);
        System.out.println("DEBUG: Spinners set to initial UI coords: (" + selectedCol + "," + selectedRow + ")");
    }

    private BufferedImage loadImage(String graphicName) {
        if ("(None)".equals(graphicName) || graphicName == null || graphicName.isEmpty()) {
            System.out.println("DEBUG: Not loading image for (None) or empty graphic name.");
            return null;
        }
        try {
            File imageFile = new File(getGraphicsCharactersPath(), graphicName + ".png"); // Use the new method
            System.out.println("DEBUG: Loading image: " + imageFile.getAbsolutePath());
            if (imageFile.exists()) {
                BufferedImage image = ImageIO.read(imageFile);
                System.out.println("DEBUG: Image loaded successfully.");
                return image;
            } else {
                System.err.println("ERROR: Image file not found: " + imageFile.getAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            System.err.println("ERROR: Error loading image " + graphicName + ": " + e.getMessage());
            e.printStackTrace(); // Print stack trace for more details
            return null;
        }
    }

    private void updatePreview() {
        currentFullSpritesheet = loadImage(selectedGraphicName);
        if (currentFullSpritesheet != null) {
            colSpinner.setEnabled(true);
            rowSpinner.setEnabled(true);
            System.out.println("DEBUG: Spritesheet loaded, spinners enabled.");
        } else {
            colSpinner.setEnabled(false);
            rowSpinner.setEnabled(false);
            System.out.println("DEBUG: No spritesheet, spinners disabled.");
        }
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    private void onOk() {
        // Handle (None) selection explicitly
        if ("(None)".equals(graphicFileList.getSelectedValue())) {
            selectedGraphicName = "";
            selectedCol = 0;
            selectedRow = 0;
            System.out.println("DEBUG: OK Pressed - Selected (None). Storing as empty string, coords (0,0).");
        } else {
            selectedGraphicName = graphicFileList.getSelectedValue();
            // selectedCol and selectedRow are already updated by spinner listeners or mouse click
            System.out.println("DEBUG: OK Pressed - Selected Graphic: " + selectedGraphicName + 
                               ", UI Coords: (" + selectedCol + "," + selectedRow + ")");
        }
        okPressed = true;
        dispose();
        System.out.println("DEBUG: Dialog disposed after OK.");
    }

    private void onCancel() {
        okPressed = false;
        dispose();
        System.out.println("DEBUG: Dialog disposed after Cancel.");
    }

    public boolean isOkPressed() {
        return okPressed;
    }

    public String getSelectedGraphicName() {
        return selectedGraphicName;
    }

    /**
     * Returns the selected column (x) in RPG Maker's encoded format.
     * Use this when saving to RPG Maker's event data.
     * @return The selected column (x) in RPG Maker's encoded format (2 * x + 2).
     */
    public int getSelectedColForRpgMaker() {
        // Encode the actual column (selectedCol, which is 0-3) back to RPG Maker's format
        int encodedCol = (selectedCol * 2) + 2;
        System.out.println("DEBUG: getSelectedColForRpgMaker() called. UI Col: " + selectedCol + " -> RMXP EncodedX: " + encodedCol);
        return encodedCol;
    }

    /**
     * Returns the selected row (y) directly, as RPG Maker stores it as is.
     * Use this when saving to RPG Maker's event data.
     * @return The selected row (y).
     */
    public int getSelectedRow() {
        System.out.println("DEBUG: getSelectedRow() called. UI Row: " + selectedRow);
        return selectedRow;
    }

    public int getHueValue() {
        return hueValue; // Always 0
    }
}