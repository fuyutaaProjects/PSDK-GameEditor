package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs;

import libs.json.JSONArray;
import libs.json.JSONException;
import psdk.EventEditor.model.EventCommand;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor dialog for comment commands (108 + 408 continuation commands).
 * Handles multi-line comments that span across multiple EventCommand objects.
 */
public class CommentEditorDialog extends JDialog {

    private static final int COMMENT_START = 108;
    private static final int COMMENT_CONTINUATION = 408;

    private List<EventCommand> allCommands;
    private int startCommandIndex;
    private JTextArea textArea;
    private boolean commandModified = false;
    private String originalIndent;

    /**
     * Constructor for the comment editing dialog.
     * @param owner The parent window of this dialog.
     * @param commands The complete list of commands from the event page.
     * @param commentStartIndex The index of the 108 command in the command list.
     */
    public CommentEditorDialog(Dialog owner, List<EventCommand> commands, int commentStartIndex) {
        super(owner, "Edit Comment", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(owner);

        this.allCommands = commands;
        this.startCommandIndex = commentStartIndex;
        
        if (commentStartIndex < 0 || commentStartIndex >= commands.size() || 
            commands.get(commentStartIndex).getCode() != COMMENT_START) {
            throw new IllegalArgumentException("Invalid comment start index or command is not a comment (108)");
        }

        this.originalIndent = commands.get(commentStartIndex).getIndent();

        initComponents();
        loadCommentData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create text area with label
        JLabel label = new JLabel("Comment text:");
        mainPanel.add(label, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(480, 200));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            saveChanges();
            commandModified = true;
            dispose();
        });

        cancelButton.addActionListener(e -> {
            commandModified = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Loads the comment data from the 108 and subsequent 408 commands into the text area.
     */
    private void loadCommentData() {
        StringBuilder sb = new StringBuilder();
        
        // Load the main comment (108)
        EventCommand startCommand = allCommands.get(startCommandIndex);
        try {
            JSONArray parameters = startCommand.getParameters();
            if (parameters != null && parameters.length() > 0) {
                sb.append(parameters.getString(0));
            }
        } catch (JSONException e) {
            System.err.println("Error reading comment start parameter: " + e.getMessage());
        }

        // Load continuation comments (408)
        int currentIndex = startCommandIndex + 1;
        while (currentIndex < allCommands.size() && 
               allCommands.get(currentIndex).getCode() == COMMENT_CONTINUATION) {
            
            EventCommand contCommand = allCommands.get(currentIndex);
            try {
                JSONArray parameters = contCommand.getParameters();
                if (parameters != null && parameters.length() > 0) {
                    sb.append("\n");
                    sb.append(parameters.getString(0));
                }
            } catch (JSONException e) {
                System.err.println("Error reading comment continuation parameter at index " + currentIndex + ": " + e.getMessage());
            }
            currentIndex++;
        }

        textArea.setText(sb.toString());
    }

    /**
     * Saves the changes by removing old comment commands and creating new ones.
     */
    private void saveChanges() {
        String fullText = textArea.getText();
        String[] lines = fullText.split("\n", -1); // -1 to keep empty strings

        // Remove old comment commands (108 + all following 408s)
        removeOldCommentCommands();

        // Create new comment commands
        createNewCommentCommands(lines);
    }

    /**
     * Removes the original 108 command and all following 408 commands.
     */
    private void removeOldCommentCommands() {
        // Remove from the start index, going backwards to maintain indices
        List<Integer> indicesToRemove = new ArrayList<>();
        
        // Add the main comment index
        indicesToRemove.add(startCommandIndex);
        
        // Add all continuation comment indices
        int currentIndex = startCommandIndex + 1;
        while (currentIndex < allCommands.size() && 
               allCommands.get(currentIndex).getCode() == COMMENT_CONTINUATION) {
            indicesToRemove.add(currentIndex);
            currentIndex++;
        }

        // Remove commands in reverse order to maintain correct indices
        for (int i = indicesToRemove.size() - 1; i >= 0; i--) {
            allCommands.remove(indicesToRemove.get(i).intValue());
        }
    }

    /**
     * Creates new comment commands based on the text lines.
     */
    private void createNewCommentCommands(String[] lines) {
        if (lines.length == 0) {
            // Create empty comment if no text
            EventCommand emptyComment = new EventCommand(COMMENT_START, originalIndent, 
                new JSONArray().put(""));
            allCommands.add(startCommandIndex, emptyComment);
            return;
        }

        // Create the main comment (108) with the first line
        JSONArray firstLineParams = new JSONArray().put(lines[0]);
        EventCommand mainComment = new EventCommand(COMMENT_START, originalIndent, firstLineParams);
        allCommands.add(startCommandIndex, mainComment);

        // Create continuation comments (408) for remaining lines
        for (int i = 1; i < lines.length; i++) {
            JSONArray contParams = new JSONArray().put(lines[i]);
            EventCommand contComment = new EventCommand(COMMENT_CONTINUATION, originalIndent, contParams);
            allCommands.add(startCommandIndex + i, contComment);
        }
    }

    public boolean isCommandModified() {
        return commandModified;
    }
}