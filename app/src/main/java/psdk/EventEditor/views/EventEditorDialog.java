package psdk.EventEditor.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import psdk.EventEditor.model.Event;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandListModificationListener;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanel;
import psdk.EventEditor.model.EventEditorDialog.PagePropertiesPanel;
import psdk.EventEditor.utils.ShowEventInTerminal;
import psdk.EventEditor.ConfigManager;

public class EventEditorDialog extends JDialog implements CommandListModificationListener {
    private Event event;
    private EventModificationCallback modificationCallback;
    private boolean eventWasModified = false;
    private PagePropertiesPanel pagePropertiesPanel;
    private CommandListPanel commandListPanel;
    private JTabbedPane pageTabbedPane; 
    
    private String projectRootPath; 

    public EventEditorDialog(JFrame owner, Event event, EventModificationCallback callback) {
        super(owner, "Edit Event - ID:" + event.getId(), true);
        this.event = event;
        this.modificationCallback = callback;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(owner);

        ShowEventInTerminal.displayEventDetails(event);

        projectRootPath = ConfigManager.loadProjectPath();
        if (projectRootPath != null) {
            System.out.println("DEBUG: RPG Maker Project Path loaded: " + projectRootPath);
        } else {
            System.out.println("DEBUG: RPG Maker Project Path not found in config.");
        }

        initComponents(); 

        this.commandListPanel.setEvent(event);
        this.commandListPanel.setPagePropertiesPanel(this.pagePropertiesPanel);
        this.commandListPanel.setPageTabbedPane(this.pageTabbedPane);
        this.commandListPanel.setModificationListener(this); 
        this.commandListPanel.setParentDialog(this);

        this.commandListPanel.loadEventPages(event);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (eventWasModified && modificationCallback != null) {
                    modificationCallback.onEventModified(event);
                }
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(236, 233, 216));
        
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.setBackground(new Color(236, 233, 216));
        namePanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(event.getName(), 20);
        namePanel.add(nameField);
        topPanel.add(namePanel, BorderLayout.NORTH);
        
        JPanel pageButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pageButtonsPanel.setBackground(new Color(236, 233, 216));
        
        JButton newPageButton = new JButton("New Event Page");
        JButton copyPageButton = new JButton("Copy Event Page");
        JButton pastePageButton = new JButton("Paste Event Page");
        JButton deletePageButton = new JButton("Delete Event Page");
        JButton clearPageButton = new JButton("Clear Event Page");
        
        styleButton(newPageButton);
        styleButton(copyPageButton);
        styleButton(pastePageButton);
        styleButton(deletePageButton);
        styleButton(clearPageButton);
        
        pageButtonsPanel.add(newPageButton);
        pageButtonsPanel.add(copyPageButton);
        pageButtonsPanel.add(pastePageButton);
        pageButtonsPanel.add(deletePageButton);
        pageButtonsPanel.add(clearPageButton);
        
        topPanel.add(pageButtonsPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setBackground(new Color(236, 233, 216));

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(236, 233, 216));
        
        pageTabbedPane = new JTabbedPane();
        pageTabbedPane.setBackground(new Color(236, 233, 216));
        pageTabbedPane.setPreferredSize(new Dimension(0, 60));
        leftPanel.add(pageTabbedPane, BorderLayout.NORTH);
        
        commandListPanel = new CommandListPanel();
        leftPanel.add(commandListPanel, BorderLayout.CENTER);
        
        mainSplitPane.setLeftComponent(leftPanel);

        pagePropertiesPanel = new PagePropertiesPanel();
        mainSplitPane.setRightComponent(pagePropertiesPanel);

        add(mainSplitPane, BorderLayout.CENTER);

        pageTabbedPane.addChangeListener(e -> {
            int selectedIndex = pageTabbedPane.getSelectedIndex();
            if (selectedIndex != -1 && selectedIndex < event.getPages().size()) {
                EventPage selectedPage = event.getPages().get(selectedIndex);
                commandListPanel.updateCommandList(selectedPage.getCommands());
                pagePropertiesPanel.updatePageProperties(selectedPage);
            } else {
                commandListPanel.updateCommandList(new ArrayList<>());
                pagePropertiesPanel.updatePageProperties(new EventPage());
            }
        });
    }

    private void styleButton(JButton button) {
        button.setBackground(new Color(236, 233, 216));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
    }
    
    // Implémentation de l'interface CommandListModificationListener
    @Override
    public void onCommandsModified() {
        this.eventWasModified = true;
        System.out.println("DEBUG: EventEditorDialog received notification: commands modified.");
    }

    public Event getModifiedEvent() {
        return this.event;
    }

    public interface EventModificationCallback {
        void onEventModified(Event modifiedEvent);
    }
}