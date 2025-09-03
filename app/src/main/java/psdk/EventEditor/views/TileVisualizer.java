package psdk.EventEditor.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TileVisualizer extends JPanel {

    private int[][][] tileGrid = null;
    private BufferedImage tilesetImage = null;
    private int tileSize = 32;

    private BufferedImage[] tileCache;

    public int scrollX = 0;
    public int scrollY = 0;

    public TileVisualizer() {
        setBackground(new Color(0x3a3843)); // background color for empty cells on the grid.
        setOpaque(true); 
    }

    public void updateGrid(int[][][] newGrid, BufferedImage newTileset) {
        boolean tilesetChanged = (this.tilesetImage != newTileset);

        this.tileGrid = newGrid;
        this.tilesetImage = newTileset;

        if (tilesetChanged && tilesetImage != null) {
            int tilesetCols = tilesetImage.getWidth() / tileSize;
            int tilesetRows = tilesetImage.getHeight() / tileSize;
            int totalTilesInTileset = tilesetCols * tilesetRows;

            tileCache = new BufferedImage[totalTilesInTileset]; 

            for (int i = 0; i < totalTilesInTileset; i++) {
                int tileX = i % tilesetCols;
                int tileY = i / tilesetCols;
                tileCache[i] = tilesetImage.getSubimage(tileX * tileSize, tileY * tileSize, tileSize, tileSize);
            }
        }

        if (tileGrid != null && tileGrid.length > 0 && tileGrid[0].length > 0) {
            int mapWidthPixels = tileGrid.length * tileSize;
            int mapHeightPixels = tileGrid[0].length * tileSize;
            setPreferredSize(new Dimension(mapWidthPixels, mapHeightPixels));
        } else {
            setPreferredSize(new Dimension(0, 0));
        }

        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    public void resetScroll() {
        this.scrollX = 0;
        this.scrollY = 0;
        SwingUtilities.invokeLater(this::repaint);
    }

    public int getTileSize() {
        return tileSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (tileGrid == null || tilesetImage == null) {
            g.setColor(Color.WHITE);
            g.drawString("Pas de map chargée", 10, 20);
            return;
        }

        Graphics2D g2d = (Graphics2D) g;

        int mapGridWidth = tileGrid.length;
        int mapGridHeight = tileGrid[0].length;
        
        // Calculate the range of tiles to draw taking into account scrolling        
        int startTileX = Math.max(0, -scrollX / tileSize);
        int endTileX = Math.min(mapGridWidth, (-scrollX + getWidth() + tileSize - 1) / tileSize);
        
        int startTileY = Math.max(0, -scrollY / tileSize);
        int endTileY = Math.min(mapGridHeight, (-scrollY + getHeight() + tileSize - 1) / tileSize);

        for (int z = 0; z < tileGrid[0][0].length; z++) {
            for (int y = startTileY; y < endTileY; y++) {
                for (int x = startTileX; x < endTileX; x++) {
                    int tileValue = tileGrid[x][y][z];

                    if (tileValue >= 384) { 
                        int tileIndexInCache = tileValue - 384; 
                        
                        if (tileIndexInCache >= 0 && tileIndexInCache < tileCache.length) {
                            BufferedImage tile = tileCache[tileIndexInCache];
                            
                            int destX = x * tileSize + scrollX;
                            int destY = y * tileSize + scrollY;

                            g2d.drawImage(tile, destX, destY, tileSize, tileSize, null);
                        } else {
                            g2d.setColor(Color.MAGENTA);
                            g2d.fillRect(x * tileSize + scrollX, y * tileSize + scrollY, tileSize, tileSize);
                        }
                    } else if (tileValue != 0) {
                        g2d.setColor(new Color(0, 0, 150));
                        g2d.fillRect(x * tileSize + scrollX, y * tileSize + scrollY, tileSize, tileSize);
                    }
                }
            }
        }
    }
}