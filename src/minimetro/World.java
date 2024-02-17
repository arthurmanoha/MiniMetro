package minimetro;

import java.util.ArrayList;

/**
 * This class represents the terrain that contains the tracks, trains,
 * landscape, ...
 *
 * @author arthu
 */
public class World {

    private int nbRows, nbCols;
    private ArrayList<ArrayList<Cell>> cells;

    public World() {
        nbRows = 10;
        nbCols = 15;
        cells = new ArrayList<>();
        for (int row = 0; row < nbRows; row++) {
            cells.add(new ArrayList<>());
            for (int col = 0; col < nbCols; col++) {
                cells.get(row).add(new Cell());
            }
        }
    }

    public int getNbRows() {
        return nbRows;
    }

    public int getNbCols() {
        return nbCols;
    }

    public Cell getCell(int row, int col) {
        return cells.get(row).get(col);
    }
}
