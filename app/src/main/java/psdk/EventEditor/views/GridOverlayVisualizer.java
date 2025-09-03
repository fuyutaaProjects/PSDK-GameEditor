package psdk.EventEditor.views;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

public class GridOverlayVisualizer extends JPanel {

    private boolean showGrid = false;
    private int tileSize = 32; // Be sure that it's the same in TileVisualizer

    public int scrollX = 0; // Synchronised with TileVisualizer
    public int scrollY = 0; // Synchronised with TileVisualizer

    public GridOverlayVisualizer() {
        setOpaque(false);
        setLayout(null);
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    public void updateMapDimensions(Dimension mapDimension) {
        setPreferredSize(mapDimension);
        setMaximumSize(mapDimension);
        setMinimumSize(mapDimension);
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    public void toggleGridVisibility() {
        this.showGrid = !this.showGrid;
        SwingUtilities.invokeLater(this::repaint);
    }

    public boolean isGridVisible() {
        return showGrid;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (!showGrid) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);

        // Draw the cell grid
        // vertical lines
        for (int x = 0; x < getWidth() + tileSize; x += tileSize) {
            int lineX = x + scrollX;
            g2d.drawLine(lineX, 0, lineX, getHeight());
        }
        // horizontal lines
        for (int y = 0; y < getHeight() + tileSize; y += tileSize) {
            int lineY = y + scrollY;
            g2d.drawLine(0, lineY, getWidth(), lineY);
        }
    }
}