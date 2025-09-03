package psdk.EventEditor.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import psdk.EventEditor.model.Event;

public class EventVisualizer extends JPanel {

    private List<Event> events;
    private int tileSize = 32; // be sure it's the same in TileVisualizer

    public int scrollX = 0; // Synchronised with TileVisualizer
    public int scrollY = 0; // Synchronised with TileVisualizer
    private EditorView editorView; 

    public EventVisualizer() {
        setOpaque(false);
        setLayout(null);
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public int getTileSize() {
        return tileSize;
    }

    public void updateEvents(List<Event> newEvents, Dimension mapDimension) {
        this.events = newEvents;
        
        setPreferredSize(mapDimension);
        setMaximumSize(mapDimension);
        setMinimumSize(mapDimension);

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    public List<Event> getEvents() {
        return events;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (events == null || events.isEmpty()) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        Rectangle clipRect = g.getClipBounds(); 

        for (Event event : events) {
            int x = event.getX() * tileSize + scrollX; 
            int y = event.getY() * tileSize + scrollY;

            if (clipRect.intersects(x, y, tileSize, tileSize)) {
                g2d.setColor(Color.RED);
                g2d.fillRect(x, y, tileSize, tileSize);
                g2d.setColor(Color.WHITE);
                g2d.drawString("E" + event.getId(), x + 5, y + tileSize / 2 + 5);
            }
        }
    }

    public void setEditorView(EditorView editorView) {
        this.editorView = editorView;
    }
}