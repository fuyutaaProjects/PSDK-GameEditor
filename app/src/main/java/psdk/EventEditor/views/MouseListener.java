package psdk.EventEditor.views;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import psdk.EventEditor.model.Event;

public class MouseListener extends MouseAdapter implements java.awt.event.MouseWheelListener {

    private TileVisualizer tileVisualizer;
    private GridOverlayVisualizer gridVisualizer;
    private EventVisualizer eventVisualizer;
    private EditorView editorView;

    private boolean isDragging = false;
    private int dragStartX = 0;
    private int dragStartY = 0;

    public MouseListener(TileVisualizer tileVisualizer, GridOverlayVisualizer gridVisualizer, EventVisualizer eventVisualizer, EditorView editorView) {
        this.tileVisualizer = tileVisualizer;
        this.gridVisualizer = gridVisualizer;
        this.eventVisualizer = eventVisualizer;
        this.editorView = editorView;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            isDragging = true;
            dragStartX = e.getX();
            dragStartY = e.getY();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON2) {
            isDragging = false;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isDragging) {
            int currentX = e.getX();
            int currentY = e.getY();

            int dx = currentX - dragStartX;
            int dy = currentY - dragStartY;

            tileVisualizer.scrollX += dx;
            tileVisualizer.scrollY += dy;
            gridVisualizer.scrollX += dx;
            gridVisualizer.scrollY += dy;
            eventVisualizer.scrollX += dx;
            eventVisualizer.scrollY += dy;

            dragStartX = currentX;
            dragStartY = currentY;

            tileVisualizer.repaint();
            gridVisualizer.repaint();
            eventVisualizer.repaint();
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        int scrollSpeed = 16;

        tileVisualizer.scrollY -= (notches * scrollSpeed);
        gridVisualizer.scrollY -= (notches * scrollSpeed);
        eventVisualizer.scrollY -= (notches * scrollSpeed);

        tileVisualizer.repaint();
        gridVisualizer.repaint();
        eventVisualizer.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            // Coordonnées du clic sur le panneau
            int clickX = e.getX();
            int clickY = e.getY();

            int tileSize = eventVisualizer.getTileSize();

            // Convert click coordinates to map coordinates (unscrolled)
            // The display uses `+ scrollX/Y` because scrollX/Y are negative offsets (content shift).
            // So to revert to raw map coordinates, we perform the inverse operation:
            // Map coordinate = Click coordinate on the panel - scrollX.
            int mapClickX = clickX - eventVisualizer.scrollX;
            int mapClickY = clickY - eventVisualizer.scrollY;

            int clickedTileCol = mapClickX / tileSize;
            int clickedTileRow = mapClickY / tileSize;

            System.out.println("Clic à la tuile : (" + clickedTileCol + ", " + clickedTileRow + ")");

            // Iterate through all events and check if the click is inside
            List<Event> events = eventVisualizer.getEvents();
            if (events != null) {
                for (Event event : events) {
                    if (event.getX() == clickedTileCol && event.getY() == clickedTileRow) {
                        System.out.println("Click corresponds to an event located at : (" + event.getX() + ", " + event.getY() + "):");
                        System.out.println(event.toString());


                        JFrame ownerFrame = (JFrame) SwingUtilities.getWindowAncestor(e.getComponent());
                        if (ownerFrame != null) {
                            // Build callback for that specific event that will be edited in the dialog window.
                            // The 'modifiedEvent' parameter in the lambda expression represents the Event object
                            // that will be returned by EventEditorDialog after the user has made modifications
                            // and closed the dialog. This callback ensures that the EditorView is notified
                            // to synchronize the updated event data back into the current map's JSON structure.
                            EventEditorDialog.EventModificationCallback callback = (modifiedEvent) -> {
                                System.out.println("Event modified, synchronizing to JSON...");
                                if (editorView != null) {
                                    editorView.syncEventsToJson();
                                }
                            };
                            
                            EventEditorDialog dialog = new EventEditorDialog(ownerFrame, event, callback);
                            dialog.setVisible(true);
                        } else {
                            System.err.println("Error: Could not find parent JFrame to open event editor.");
                        }

                        return;
                    }
                }
            }
            System.out.println("No event found at this click position.");
        }
    }
}