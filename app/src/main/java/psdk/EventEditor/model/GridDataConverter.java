package psdk.EventEditor.model;

import java.util.Iterator;

import libs.json.JSONArray;
import libs.json.JSONObject;

public class GridDataConverter {

    public static int[][][] convertJsonGridTo3DArray(JSONObject gridInfoJson) {
        if (gridInfoJson == null || !gridInfoJson.has("width") || !gridInfoJson.has("height") ||
            !gridInfoJson.has("layers") || !gridInfoJson.has("grids")) {
            System.err.println("ERROR: Invalid or incomplete grid_info JSONObject from JSON.");
            return null;
        }

        int width = gridInfoJson.getInt("width");
        int height = gridInfoJson.getInt("height");
        int depth = gridInfoJson.getInt("layers");

        JSONObject layersData = gridInfoJson.getJSONObject("grids");

        if (width == 0 || height == 0 || depth == 0) {
            System.err.println("ERROR: Invalid grid dimensions parsed from JSON.");
            return null;
        }

        int[][][] grid = new int[width][height][depth];

        Iterator<String> layerKeys = layersData.keys();
        while (layerKeys.hasNext()) {
            String zKey = layerKeys.next();
            int z = Integer.parseInt(zKey);

            if (z < 0 || z >= depth) {
                System.err.println("WARNING: Layer key " + zKey + " is out of bounds for defined depth " + depth + ". Skipping.");
                continue;
            }

            JSONArray rows = layersData.getJSONArray(zKey);
            int rowsToRead = Math.min(rows.length(), height);

            for (int y = 0; y < rowsToRead; y++) {
                JSONArray rowNums = rows.getJSONArray(y);
                int colsToRead = Math.min(rowNums.length(), width);

                for (int x = 0; x < colsToRead; x++) {
                    grid[x][y][z] = rowNums.getInt(x);
                }
            }
        }
        return grid;
    }
}