package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import libs.json.JSONArray;
import libs.json.JSONException;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.SetMoveRouteEditorDialog;
import psdk.EventEditor.model.EventEditorDialog.CommandEditingDialogs.ShowTextEditorDialog;

public class EventCommandEditorDialog extends JDialog {

    private EventCommand originalCommand;
    private EventCommand modifiedCommand;
    private boolean commandModified = false;

    private JLabel codeLabel;
    private JLabel indentLabel;
    private JTextArea parametersTextArea;
    private JButton editSpecificButton;

    public EventCommandEditorDialog(Dialog owner, EventCommand command) {
        super(owner, "Edit Command", true);
        this.originalCommand = command;
        // Create a modifiable copy
        try {
            // Perform a deep copy of the original command to allow non-destructive editing, in case we don't save the edits.
            this.modifiedCommand = new EventCommand(
                command.getCode(),
                command.getIndent(),
                command.getParameters() != null ? new JSONArray(command.getParameters().toString()) : new JSONArray()
            );
        } catch (JSONException e) {
            System.err.println("Error deep copying EventCommand parameters: " + e.getMessage());
            this.modifiedCommand = new EventCommand(command.getCode(), command.getIndent(), new JSONArray()); // Fallback to empty params
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(owner);
        initComponents();
        populateFields();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        infoPanel.add(new JLabel("Code:"));
        codeLabel = new JLabel();
        infoPanel.add(codeLabel);
        infoPanel.add(new JLabel("Indent:"));
        indentLabel = new JLabel();
        infoPanel.add(indentLabel);

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        JPanel parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.setBorder(BorderFactory.createTitledBorder("Parameters (Raw JSON)"));
        parametersTextArea = new JTextArea(5, 30);
        parametersTextArea.setLineWrap(true);
        parametersTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(parametersTextArea);
        parametersPanel.add(scrollPane, BorderLayout.CENTER);

        editSpecificButton = new JButton("Edit Specific Parameters");
        editSpecificButton.setEnabled(false);
        parametersPanel.add(editSpecificButton, BorderLayout.SOUTH);

        mainPanel.add(parametersPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton okButton = new JButton("OK");
        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            applyChangesToModifiedCommand();
            commandModified = true;
            dispose();
        });

        applyButton.addActionListener(e -> {
            applyChangesToModifiedCommand();
            commandModified = true;
            // Dialog remains open
        });

        cancelButton.addActionListener(e -> {
            commandModified = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFields() {
        codeLabel.setText(String.valueOf(modifiedCommand.getCode()));
        indentLabel.setText(String.valueOf(modifiedCommand.getIndent()));
        if (modifiedCommand.getParameters() != null) {
            parametersTextArea.setText(modifiedCommand.getParameters().toString(2));
        } else {
            parametersTextArea.setText("[]");
        }
        
        // Remove existing listeners to prevent duplicates
        for (java.awt.event.ActionListener al : editSpecificButton.getActionListeners()) {
            editSpecificButton.removeActionListener(al);
        }

        if (modifiedCommand.getCode() == 209) { // Set Move Route
            editSpecificButton.setEnabled(true);
            editSpecificButton.setText("Edit Move Route...");
            editSpecificButton.addActionListener(e -> openSpecificCommandEditor(modifiedCommand.getCode()));
        } else if (modifiedCommand.getCode() == 101) { // Show Text
            editSpecificButton.setEnabled(true);
            editSpecificButton.setText("Edit Text Content...");
            editSpecificButton.addActionListener(e -> openSpecificCommandEditor(modifiedCommand.getCode()));
        }
        else {
            editSpecificButton.setEnabled(false);
            editSpecificButton.setText("Edit Specific Parameters (N/A)");
        }
    }

    private void applyChangesToModifiedCommand() {
        try {
            String rawParams = parametersTextArea.getText();
            if (rawParams.trim().startsWith("[") && rawParams.trim().endsWith("]")) {
                modifiedCommand.setParameters(new JSONArray(rawParams));
            } else if (rawParams.trim().isEmpty()) {
                modifiedCommand.setParameters(new JSONArray());
            } else {
                System.err.println("Warning: Parameters text is not a valid JSON array. Attempting to wrap in array.");
                modifiedCommand.setParameters(new JSONArray().put(rawParams));
            }

        } catch (JSONException e) {
            JOptionPane.showMessageDialog(this, "Error parsing parameters JSON: " + e.getMessage(), "JSON Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error parsing parameters JSON: " + e.getMessage());
            commandModified = false;
        }
    }

    private void openSpecificCommandEditor(int commandCode) {
        if (commandCode == 209) { // Set Move Route
            System.out.println("DEBUG: Opening SetMoveRouteEditorDialog for Command Code 209.");
            SetMoveRouteEditorDialog moveRouteDialog = new SetMoveRouteEditorDialog(this, modifiedCommand);
            moveRouteDialog.setVisible(true);

            if (moveRouteDialog.isCommandModified()) {
                // IMPORTANT: Get the modified parameters from the specific dialog
                JSONArray newParameters = moveRouteDialog.getModified209Parameters();
                modifiedCommand.setParameters(newParameters); // Update this dialog's modifiedCommand
                parametersTextArea.setText(modifiedCommand.getParameters().toString(2));
                commandModified = true;
                System.out.println("DEBUG: SetMoveRouteEditorDialog returned modified parameters.");
            } else {
                System.out.println("DEBUG: SetMoveRouteEditorDialog was cancelled or no changes made.");
            }
        } else if (commandCode == 101) { // Show Text
            System.out.println("DEBUG: Opening ShowTextEditorDialog for Command Code 101.");
            // Pass this dialog's 'modifiedCommand' directly to ShowTextEditorDialog
            // ShowTextEditorDialog's internal 'modifiedCommand' will be a deep copy
            ShowTextEditorDialog textEditorDialog = new ShowTextEditorDialog(this, modifiedCommand);
            textEditorDialog.setVisible(true);

            if (textEditorDialog.isCommandModified()) {
                // IMPORTANT: Retrieve the modified command from ShowTextEditorDialog
                this.modifiedCommand = textEditorDialog.getModifiedCommand(); // Replace this dialog's modifiedCommand with the one that was actually modified.
                parametersTextArea.setText(modifiedCommand.getParameters().toString(2));
                commandModified = true;
                System.out.println("DEBUG: ShowTextEditorDialog returned modified text.");
            } else {
                System.out.println("DEBUG: ShowTextEditorDialog was cancelled or no changes made.");
            }
        }
        else {
            JOptionPane.showMessageDialog(this, "No specific editor available for this command code: " + commandCode, "Specific Editor", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public EventCommand getModifiedCommand() {
        return modifiedCommand;
    }

    public boolean isCommandModified() {
        return commandModified;
    }
}