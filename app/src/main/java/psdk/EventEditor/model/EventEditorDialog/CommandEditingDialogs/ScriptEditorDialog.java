package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs;

import libs.json.JSONArray;
import libs.json.JSONException;
import psdk.EventEditor.model.EventCommand;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScriptEditorDialog extends JDialog {

    private List<EventCommand> scriptCommands; // Liste des commandes de script (355 + 655s)
    private JTextArea textArea;
    private boolean commandModified = false;

    /**
     * Constructor for the script editing dialog.
     * @param owner The parent window of this dialog.
     * @param scriptCommands List of script commands (355 followed by 655s) to edit.
     */
    public ScriptEditorDialog(Dialog owner, List<EventCommand> scriptCommands) {
        super(owner, "Edit Script", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(owner);

        // Creates a deep copy of the script commands to work on
        this.scriptCommands = new ArrayList<>();
        for (EventCommand cmd : scriptCommands) {
            try {
                EventCommand copy = new EventCommand(
                    cmd.getCode(),
                    cmd.getIndent(),
                    cmd.getParameters() != null ? new JSONArray(cmd.getParameters().toString()) : new JSONArray()
                );
                this.scriptCommands.add(copy);
            } catch (JSONException e) {
                System.err.println("Error deep copying EventCommand for ScriptEditorDialog: " + e.getMessage());
                // Fallback: create empty command with same code and indent
                this.scriptCommands.add(new EventCommand(cmd.getCode(), cmd.getIndent(), new JSONArray()));
            }
        }

        initComponents();
        loadScriptData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        textArea = new JTextArea();
        textArea.setLineWrap(false); // Don't wrap for code
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12)); // Use monospaced font for code
        textArea.setTabSize(2); // Smaller tab size for code
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
     * Loads the script commands' text data into the JTextArea.
     * Combines text from the 355 command and all following 655 commands.
     */
    private void loadScriptData() {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < scriptCommands.size(); i++) {
            EventCommand cmd = scriptCommands.get(i);
            JSONArray parameters = cmd.getParameters();
            
            if (parameters != null && parameters.length() > 0) {
                try {
                    String text = parameters.getString(0);
                    sb.append(text);
                    if (i < scriptCommands.size() - 1) {
                        sb.append("\n");
                    }
                } catch (JSONException e) {
                    System.err.println("Error reading script parameter from command " + cmd.getCode() + ": " + e.getMessage());
                }
            }
        }
        
        textArea.setText(sb.toString());
        textArea.setCaretPosition(0); // Move cursor to beginning
    }

    /**
     * Saves the JTextArea modifications back to the script commands.
     * Recreates the command structure: first line goes to 355, subsequent lines to 655 commands.
     */
    private void saveChanges() {
        String fullText = textArea.getText();
        String[] lines = fullText.split("\n", -1); // Keep empty lines
        
        // Clear the current command list
        scriptCommands.clear();
        
        if (lines.length > 0) {
            // First line goes to command 355
            String firstLine = lines[0];
            JSONArray firstParameters = new JSONArray();
            firstParameters.put(firstLine);
            
            // Use the indent from the original first command, or default to "0"
            String indent = "0"; // Default indent for scripts
            EventCommand firstCommand = new EventCommand(355, indent, firstParameters);
            scriptCommands.add(firstCommand);
            
            // Subsequent lines go to command 655
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                JSONArray parameters = new JSONArray();
                parameters.put(line);
                EventCommand continuationCommand = new EventCommand(655, indent, parameters);
                scriptCommands.add(continuationCommand);
            }
        } else {
            // Empty script: create a single 355 command with empty text
            JSONArray emptyParameters = new JSONArray();
            emptyParameters.put("");
            EventCommand emptyCommand = new EventCommand(355, "0", emptyParameters);
            scriptCommands.add(emptyCommand);
        }
        
        System.out.println("DEBUG: ScriptEditorDialog: Changes saved to script commands.");
        System.out.println("DEBUG: Number of commands created: " + scriptCommands.size());
        for (int i = 0; i < scriptCommands.size(); i++) {
            System.out.println("DEBUG: Command " + i + ": Code " + scriptCommands.get(i).getCode() + 
                             ", Parameters: " + scriptCommands.get(i).getParameters().toString());
        }
    }

    public boolean isCommandModified() {
        return commandModified;
    }

    public List<EventCommand> getModifiedScriptCommands() {
        return scriptCommands;
    }
}