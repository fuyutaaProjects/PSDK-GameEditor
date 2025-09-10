package psdk.EventEditor.model.EventEditorDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import psdk.EventEditor.model.Event;
import psdk.EventEditor.model.EventCommand;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage.CommandEditorManager;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage.CommandOperations;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage.EventPageManager;
import psdk.EventEditor.views.EventCommandListCellRenderer;

/**
 * Panel for displaying and managing event commands in a list format.
 * Supports adding, deleting, moving, and editing commands.
 */
public class CommandListPanel extends JPanel {
    
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    private static final Color LIST_BACKGROUND_COLOR = Color.WHITE;
    private static final Dimension PREFERRED_SIZE = new Dimension(0, 300);
    
    private final JList<EventCommand> commandList;
    private final DefaultListModel<EventCommand> commandListModel;
    private final CommandOperations commandOperations;
    private final CommandEditorManager editorManager;
    private final EventPageManager pageManager;
    
    private JTabbedPane pageTabbedPane;
    private PagePropertiesPanel pagePropertiesPanel;
    private Event event;
    private CommandListModificationListener modificationListener;
    private JDialog parentDialog;

    public CommandListPanel() {
        this.commandListModel = new DefaultListModel<>();
        this.commandList = createCommandList();
        this.commandOperations = new CommandOperations(this);
        this.editorManager = new CommandEditorManager(this);
        this.pageManager = new EventPageManager(this);
        
        initializeLayout();
    }

    private JList<EventCommand> createCommandList() {
        JList<EventCommand> list = new JList<>(commandListModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setBackground(LIST_BACKGROUND_COLOR);
        list.setCellRenderer(new EventCommandListCellRenderer());
        list.addMouseListener(new CommandListMouseListener());
        return list;
    }

    private void initializeLayout() {
        JPanel mainPanel = createMainPanel();
        mainPanel.add(createScrollPane(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), 
            "List of Event Commands:"
        );
        panel.setBorder(border);
        panel.setPreferredSize(PREFERRED_SIZE);
        
        return panel;
    }

    private JScrollPane createScrollPane() {
        JScrollPane scrollPane = new JScrollPane(commandList);
        scrollPane.setBackground(LIST_BACKGROUND_COLOR);
        return scrollPane;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(BACKGROUND_COLOR);
        
        ButtonConfig[] buttons = {
            new ButtonConfig("New...", this::addCommand),
            new ButtonConfig("Indent", this::indentCommand),
            new ButtonConfig("Outdent", this::outdentCommand),
            new ButtonConfig("Copy", this::copyCommand),
            new ButtonConfig("Paste", this::pasteCommand),
            new ButtonConfig("Delete", this::deleteCommand),
            new ButtonConfig("Move Up", this::moveCommandUp),
            new ButtonConfig("Move Down", this::moveCommandDown)
        };
        
        for (ButtonConfig config : buttons) {
            JButton button = createStyledButton(config.text, config.action);
            buttonPanel.add(button);
        }
        
        return buttonPanel;
    }

    private JButton createStyledButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setBackground(BACKGROUND_COLOR);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    public void updateCommandList(List<EventCommand> commands) {
        commandListModel.clear();
        if (commands != null) {
            commands.forEach(commandListModel::addElement);
        }
    }

    public void loadEventPages(Event event) {
        pageManager.loadEventPages(event);
    }

    // Command operation delegates
    private void addCommand() {
        commandOperations.addCommand();
    }

    private void deleteCommand() {
        commandOperations.deleteCommand();
    }

    private void moveCommandUp() {
        commandOperations.moveCommandUp();
    }

    private void moveCommandDown() {
        commandOperations.moveCommandDown();
    }

    private void indentCommand() {
        // TODO: Implement indent functionality
    }

    private void outdentCommand() {
        // TODO: Implement outdent functionality
    }

    private void copyCommand() {
        // TODO: Implement copy functionality
    }

    private void pasteCommand() {
        // TODO: Implement paste functionality
    }

    private class CommandListMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                EventCommand selectedCommand = commandList.getSelectedValue();
                if (selectedCommand != null) {
                    editorManager.openCommandEditor(selectedCommand);
                }
            }
        }
    }

    // Getters for internal components access
    public JList<EventCommand> getCommandList() {
        return commandList;
    }

    public DefaultListModel<EventCommand> getCommandListModel() {
        return commandListModel;
    }

    public Event getEvent() {
        return event;
    }

    public JTabbedPane getPageTabbedPane() {
        return pageTabbedPane;
    }

    public PagePropertiesPanel getPagePropertiesPanel() {
        return pagePropertiesPanel;
    }

    public JDialog getParentDialog() {
        return parentDialog;
    }

    public CommandListModificationListener getModificationListener() {
        return modificationListener;
    }

    // Setters
    public void setEvent(Event event) {
        this.event = event;
    }

    public void setPageTabbedPane(JTabbedPane pageTabbedPane) {
        this.pageTabbedPane = pageTabbedPane;
    }

    public void setPagePropertiesPanel(PagePropertiesPanel pagePropertiesPanel) {
        this.pagePropertiesPanel = pagePropertiesPanel;
    }

    public void setModificationListener(CommandListModificationListener listener) {
        this.modificationListener = listener;
    }

    public void setParentDialog(JDialog parentDialog) {
        this.parentDialog = parentDialog;
    }

    private static class ButtonConfig {
        final String text;
        final Runnable action;

        ButtonConfig(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }
    }
}