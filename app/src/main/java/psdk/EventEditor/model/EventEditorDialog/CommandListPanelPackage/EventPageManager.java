package psdk.EventEditor.model.EventEditorDialog.CommandListPanelPackage;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import psdk.EventEditor.model.Event;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.model.EventEditorDialog.CommandListPanel;
import psdk.EventEditor.model.EventEditorDialog.PagePropertiesPanel;

/**
 * Manages event page loading and UI updates for the CommandListPanel.
 */
public class EventPageManager {
    
    private static final Color BACKGROUND_COLOR = new Color(236, 233, 216);
    
    private final CommandListPanel panel;

    public EventPageManager(CommandListPanel panel) {
        this.panel = panel;
    }

    public void loadEventPages(Event event) {
        JTabbedPane pageTabbedPane = panel.getPageTabbedPane();
        if (!validatePageTabbedPane(pageTabbedPane)) return;
        
        pageTabbedPane.removeAll();
        
        if (event.getPages() == null || event.getPages().isEmpty()) {
            createDefaultPage(event, pageTabbedPane);
        } else {
            loadExistingPages(event, pageTabbedPane);
        }
        
        selectFirstPageAndUpdateUI(event, pageTabbedPane);
    }

    private boolean validatePageTabbedPane(JTabbedPane pageTabbedPane) {
        if (pageTabbedPane == null) {
            System.err.println("ERROR: pageTabbedPane is null in CommandListPanel. Cannot load event pages.");
            return false;
        }
        return true;
    }

    private void createDefaultPage(Event event, JTabbedPane pageTabbedPane) {
        EventPage defaultPage = new EventPage();
        event.getPages().add(defaultPage);
        JPanel pagePanel = createPagePanel(defaultPage, 1);
        pageTabbedPane.addTab("1", pagePanel);
    }

    private void loadExistingPages(Event event, JTabbedPane pageTabbedPane) {
        for (int i = 0; i < event.getPages().size(); i++) {
            EventPage page = event.getPages().get(i);
            JPanel pagePanel = createPagePanel(page, i + 1);
            pageTabbedPane.addTab(String.valueOf(i + 1), pagePanel);
        }
    }

    private void selectFirstPageAndUpdateUI(Event event, JTabbedPane pageTabbedPane) {
        if (pageTabbedPane.getTabCount() > 0) {
            pageTabbedPane.setSelectedIndex(0);
            EventPage firstPage = event.getPages().get(0);
            panel.updateCommandList(firstPage.getCommands());
            updatePagePropertiesPanel(firstPage);
        }
    }

    private void updatePagePropertiesPanel(EventPage page) {
        PagePropertiesPanel pagePropertiesPanel = panel.getPagePropertiesPanel();
        if (pagePropertiesPanel != null) {
            pagePropertiesPanel.updatePageProperties(page);
        }
    }

    private JPanel createPagePanel(EventPage page, int pageNumber) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        return panel;
    }
}