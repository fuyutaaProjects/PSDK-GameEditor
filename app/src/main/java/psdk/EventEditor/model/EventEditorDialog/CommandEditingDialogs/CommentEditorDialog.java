package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs;

import libs.json.JSONArray;
import libs.json.JSONException;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.utils.DialogKeyBindingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CommentEditorDialog extends JDialog implements DialogKeyBindingUtils.SaveableDialog {

    private List<EventCommand> commentCommands; // Liste des commandes de commentaire (108 + 408s)
    private JTextArea textArea;
    private boolean commandModified = false;

    /**
     * Constructor for the comment editing dialog.
     * @param owner The parent window of this dialog.
     * @param commentCommands List of comment commands (108 followed by 408s) to edit.
     */
    public CommentEditorDialog(Dialog owner, List<EventCommand> commentCommands) {
        super(owner, "Edit Comment", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(owner);

        // Creates a deep copy of the comment commands to work on
        this.commentCommands = new ArrayList<>();
        for (EventCommand cmd : commentCommands) {
            try {
                EventCommand copy = new EventCommand(
                    cmd.getCode(),
                    cmd.getIndent(),
                    cmd.getParameters() != null ? new JSONArray(cmd.getParameters().toString()) : new JSONArray()
                );
                this.commentCommands.add(copy);
            } catch (JSONException e) {
                System.err.println("Error deep copying EventCommand for CommentEditorDialog: " + e.getMessage());
                // Fallback: create empty command with same code and indent
                this.commentCommands.add(new EventCommand(cmd.getCode(), cmd.getIndent(), new JSONArray()));
            }
        }

        initComponents();
        loadCommentData();
        setupKeyBindings();
    }

    private void setupKeyBindings() {
        DialogKeyBindingUtils.setupStandardKeyBindings(this, this);
    }

    @Override
    public void saveAndClose() {
        saveChanges();
        commandModified = true;
        dispose();
    }

    @Override
    public void cancelAndClose() {
        commandModified = false;
        dispose();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

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
     * Loads the comment commands' text data into the JTextArea.
     * Combines text from the 108 command and all following 408 commands.
     */
    private void loadCommentData() {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < commentCommands.size(); i++) {
            EventCommand cmd = commentCommands.get(i);
            JSONArray parameters = cmd.getParameters();
            
            if (parameters != null && parameters.length() > 0) {
                try {
                    String text = parameters.getString(0);
                    sb.append(text);
                    if (i < commentCommands.size() - 1) {
                        sb.append("\n");
                    }
                } catch (JSONException e) {
                    System.err.println("Error reading comment parameter from command " + cmd.getCode() + ": " + e.getMessage());
                }
            }
        }
        
        textArea.setText(sb.toString());
    }

    /**
     * Saves the JTextArea modifications back to the comment commands.
     * Recreates the command structure: first line goes to 108, subsequent lines to 408 commands.
     */
    private void saveChanges() {
        String fullText = textArea.getText();
        String[] lines = fullText.split("\n", -1); // Keep empty lines
        
        // Clear the current command list
        commentCommands.clear();
        
        if (lines.length > 0) {
            // First line goes to command 108
            String firstLine = lines[0];
            JSONArray firstParameters = new JSONArray();
            firstParameters.put(firstLine);
            
            // Use the indent from the original first command, or default to "0"
            String indent = "0"; // Default indent for comments
            EventCommand firstCommand = new EventCommand(108, indent, firstParameters);
            commentCommands.add(firstCommand);
            
            // Subsequent lines go to command 408
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                JSONArray parameters = new JSONArray();
                parameters.put(line);
                EventCommand continuationCommand = new EventCommand(408, indent, parameters);
                commentCommands.add(continuationCommand);
            }
        } else {
            // Empty comment: create a single 108 command with empty text
            JSONArray emptyParameters = new JSONArray();
            emptyParameters.put("");
            EventCommand emptyCommand = new EventCommand(108, "0", emptyParameters);
            commentCommands.add(emptyCommand);
        }
        
        System.out.println("DEBUG: CommentEditorDialog: Changes saved to comment commands.");
        System.out.println("DEBUG: Number of commands created: " + commentCommands.size());
        for (int i = 0; i < commentCommands.size(); i++) {
            System.out.println("DEBUG: Command " + i + ": Code " + commentCommands.get(i).getCode() + 
                             ", Parameters: " + commentCommands.get(i).getParameters().toString());
        }
    }

    public boolean isCommandModified() {
        return commandModified;
    }

    public List<EventCommand> getModifiedCommentCommands() {
        return commentCommands;
    }
}