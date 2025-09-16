package psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs;

import libs.json.JSONArray;
import libs.json.JSONException;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.utils.DialogKeyBindingUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.stream.IntStream;

public class ShowTextEditorDialog extends JDialog implements DialogKeyBindingUtils.SaveableDialog {

    private EventCommand modifiedCommand; // La commande Show Text que nous allons modifier
    private JTextArea textArea;
    private boolean commandModified = false;

    /**
    * Constructor for the text editing dialog.
    * @param owner The parent window of this dialog.
    * @param command The "Show Text" type EventCommand (code 101) to edit.
    */
    public ShowTextEditorDialog(Dialog owner, EventCommand command) {
        super(owner, "Edit Show Text", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(owner);

        // Creates a deep copy of the command to work on
        try {
            this.modifiedCommand = new EventCommand(
                command.getCode(),
                command.getIndent(),
                command.getParameters() != null ? new JSONArray(command.getParameters().toString()) : new JSONArray()
            );
        } catch (JSONException e) {
            System.err.println("Error deep copying EventCommand for ShowTextEditorDialog: " + e.getMessage());
            this.modifiedCommand = new EventCommand(command.getCode(), command.getIndent(), new JSONArray()); // Fallback
        }

        initComponents();
        loadTextData();
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
     * Loads the command's text data into the JTextArea.
     * For a "Show Text" command (code 101), the parameters are a JSONArray of strings.
     */
    private void loadTextData() {
        JSONArray parameters = modifiedCommand.getParameters();
        if (parameters != null && parameters.length() > 0) {
            StringBuilder sb = new StringBuilder();
            IntStream.range(0, parameters.length()).forEach(i -> {
                try {
                    sb.append(parameters.getString(i));
                    if (i < parameters.length() - 1) {
                        sb.append("\n");
                    }
                } catch (JSONException e) {
                    System.err.println("Error reading text parameter at index " + i + ": " + e.getMessage());
                }
            });
            textArea.setText(sb.toString());
        } else {
            textArea.setText("");
        }
    }

    /**
    * Saves the JTextArea modifications to the command's parameters.
    * Each line in the JTextArea becomes a string element in the parameters' JSONArray.
    */
    private void saveChanges() {
        String fullText = textArea.getText();
        String[] lines = fullText.split("\n");
        JSONArray newParameters = new JSONArray();
        for (String line : lines) {
            newParameters.put(line);
        }
        modifiedCommand.setParameters(newParameters);
        System.out.println("DEBUG: ShowTextEditorDialog: Changes saved to modifiedCommand parameters.");
        System.out.println("DEBUG: New parameters: " + modifiedCommand.getParameters().toString(2));
    }


    public boolean isCommandModified() {
        return commandModified;
    }

    public EventCommand getModifiedCommand() {
        return modifiedCommand;
    }
}