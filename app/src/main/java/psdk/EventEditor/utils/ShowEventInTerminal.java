package psdk.EventEditor.utils;

import java.util.List;

import psdk.EventEditor.model.Event;
import psdk.EventEditor.model.EventPage;
import psdk.EventEditor.views.ShowEventCommand;
import psdk.EventEditor.views.ShowEventPage;

public class ShowEventInTerminal {
    
    /**
     * Static method to display event details in terminal
     * Now uses ShowEventPage for displaying page details
     */
    public static void displayEventDetails(Event event) {
        System.out.println("================================================================================");
        System.out.println("EVENT DEBUG DISPLAY");
        System.out.println("================================================================================");
        System.out.println("Event ID: " + event.getId());
        System.out.println("Event Name: " + event.getName());
        System.out.println("Position: (" + event.getX() + ", " + event.getY() + ")");
        System.out.println("Total Pages: " + event.getPages().size());
        System.out.println("================================================================================");

        List<EventPage> pages = event.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            EventPage page = pages.get(pageIndex);
            
            System.out.println();
            ShowEventPage.displayEventPageDetails(page, pageIndex);
            
            if (pageIndex < pages.size() - 1) {
                System.out.println("................................................................................");
            }
        }
        
        System.out.println("================================================================================");
        System.out.println("END EVENT DEBUG DISPLAY");
        System.out.println("================================================================================");
    }
    
    /**
     * Display event details with enhanced command interpretation
     */
    public static void displayEventDetailsWithInterpretation(Event event) {
        System.out.println("================================================================================");
        System.out.println("EVENT DEBUG DISPLAY (WITH INTERPRETATION)");
        System.out.println("================================================================================");
        System.out.println("Event ID: " + event.getId());
        System.out.println("Event Name: " + event.getName());
        System.out.println("Position: (" + event.getX() + ", " + event.getY() + ")");
        System.out.println("Total Pages: " + event.getPages().size());
        System.out.println("================================================================================");

        List<EventPage> pages = event.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            EventPage page = pages.get(pageIndex);
            
            System.out.println();
            displayEventPageWithInterpretation(page, pageIndex);
            
            if (pageIndex < pages.size() - 1) {
                System.out.println("................................................................................");
            }
        }
        
        System.out.println("================================================================================");
        System.out.println("END EVENT DEBUG DISPLAY (WITH INTERPRETATION)");
        System.out.println("================================================================================");
    }
    
    /**
     * Display event page with command interpretation
     */
    private static void displayEventPageWithInterpretation(EventPage page, int pageIndex) {
        System.out.println("--- PAGE " + pageIndex + " ---");
        System.out.println("Page Index: " + page.getPage_index());
        
        // Basic page info (similar to ShowEventPage but simplified)
        System.out.println("Move Type: " + page.getMove_type());
        System.out.println("Trigger: " + page.getTrigger());
        System.out.println("Character: " + (page.getCharacterName().isEmpty() ? "(none)" : page.getCharacterName()));
        
        // Display commands with interpretation
        System.out.println("Commands: " + page.getCommands().size());
        System.out.println("................................................................................");
        
        for (int cmdIndex = 0; cmdIndex < page.getCommands().size(); cmdIndex++) {
            ShowEventCommand.displayEventCommandDetailsWithInterpretation(
                page.getCommands().get(cmdIndex), 
                cmdIndex + 1
            );
            
            if (cmdIndex < page.getCommands().size() - 1) {
                System.out.println("  ................................................................");
            }
        }
    }
    
    /**
     * Display only basic event information (summary)
     */
    public static void displayEventSummary(Event event) {
        System.out.println("================================================================================");
        System.out.println("EVENT SUMMARY");
        System.out.println("================================================================================");
        System.out.println("Event ID: " + event.getId());
        System.out.println("Event Name: " + event.getName());
        System.out.println("Position: (" + event.getX() + ", " + event.getY() + ")");
        System.out.println("Total Pages: " + event.getPages().size());
        
        List<EventPage> pages = event.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            EventPage page = pages.get(pageIndex);
            System.out.println("  Page " + pageIndex + ": " + page.getCommands().size() + " commands, " +
                             "Character: " + (page.getCharacterName().isEmpty() ? "(none)" : page.getCharacterName()) +
                             ", Trigger: " + page.getTrigger());
        }
        
        System.out.println("================================================================================");
    }
    
    /**
     * Display only commands for all pages
     */
    public static void displayEventCommands(Event event) {
        System.out.println("================================================================================");
        System.out.println("EVENT COMMANDS ONLY - " + event.getName() + " (ID: " + event.getId() + ")");
        System.out.println("================================================================================");
        
        List<EventPage> pages = event.getPages();
        for (int pageIndex = 0; pageIndex < pages.size(); pageIndex++) {
            EventPage page = pages.get(pageIndex);
            
            System.out.println("--- PAGE " + pageIndex + " COMMANDS ---");
            
            for (int cmdIndex = 0; cmdIndex < page.getCommands().size(); cmdIndex++) {
                ShowEventCommand.displayEventCommandDetailsWithInterpretation(
                    page.getCommands().get(cmdIndex), 
                    cmdIndex + 1
                );
                
                if (cmdIndex < page.getCommands().size() - 1) {
                    System.out.println("  ................................................................");
                }
            }
            
            if (pageIndex < pages.size() - 1) {
                System.out.println("................................................................................");
            }
        }
        
        System.out.println("================================================================================");
    }
}