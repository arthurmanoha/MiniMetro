package minimetro;

import java.util.ArrayList;

/**
 * @author arthu
 * This class represents the same data as a 2d m*n matrix, but in a way that
 * makes it efficient for matrices with a low density.
 * @param <T> The type of elements stored in this matrix
 */
public class SparseMatrix<T> {

    /**
     * This class encapsulates the values stored in the sparse matrix with data
     * used to locate the elements.
     *
     * @param <T>
     */
    private class SparseMatrixContainer<T> {

        public T element;
        public int row;
        public int col;
    }

    private ArrayList<ArrayList<SparseMatrixContainer>> rows;

    public SparseMatrix() {
        this.rows = new ArrayList<>();
    }

    /**
     * Return the element at specified row and column if it exists, or null
     * otherwise.
     *
     * @param rowNumber
     * @param colNumber
     * @return the element positioned at specified location if it exists,
     * otherwise null;
     */
    public T get(int rowNumber, int colNumber) {

        if (rows == null || rows.isEmpty()) {
            return null;
        }

        int physicalRowNumber = 0;
        ArrayList<SparseMatrixContainer> selectedRow = null;

        // Find the physical row containing our element.
        while (physicalRowNumber < rows.size() && selectedRow == null) {
            SparseMatrixContainer firstElemOfCurrentRow = rows.get(physicalRowNumber).get(0);
            if (firstElemOfCurrentRow.row > rowNumber) {
                // Requested row does not exist.
                return null;
            } else if (firstElemOfCurrentRow.row == rowNumber) {
                selectedRow = rows.get(physicalRowNumber);
            }
            physicalRowNumber++;
        }

        if (selectedRow == null) {
            return null;
        }

        // Search for the requested element in this row.
        int physicalColNumber = 0;
        SparseMatrixContainer selectedContainer = null;
        while (physicalColNumber < selectedRow.size() && selectedContainer == null) {
            if (selectedRow.get(physicalColNumber).col == colNumber) {
                // Successfully found our element
                return (T) selectedRow.get(physicalColNumber).element;
            }
            physicalColNumber++;
        }
        // At this step the element does not exist in the selected row.
        return null;
    }

    public void set(T newElem, int newRowNumber, int newColNumber) {
        System.out.println("Set(" + newElem + " at (" + newRowNumber + ", " + newColNumber + ");");
        // Step one: find the row that will contain the new element.
        ArrayList<SparseMatrixContainer> selectedRow = null;
        int physicalRowNumber = 0;
        for (ArrayList<SparseMatrixContainer> scannedRow : rows) {
            // Scan all the stored (non-empty) rows and find @newRowNumber or higher
            int currentRowNumber = scannedRow.get(0).row;
            if (currentRowNumber == newRowNumber) {
                // We found the row where we shall insert the new element.
                selectedRow = scannedRow;
            } else if (currentRowNumber > newRowNumber) {
                // We found the next row, our row does not exist yet, we need to create it.
                selectedRow = new ArrayList<>();
                rows.add(physicalRowNumber, selectedRow);
            }
            physicalRowNumber++;
        }
        if (selectedRow == null) {
            // We must add our row at the end.
            selectedRow = new ArrayList<>();
            rows.add(selectedRow);
        }

        // Step two: find the right spot in the selected row.
        SparseMatrixContainer selectedContainer = null;
        int physicalColNumber = 0;
        for (SparseMatrixContainer scannedContainer : selectedRow) {
            int currentColNumber = scannedContainer.col;
            if (currentColNumber == newColNumber) {
                // We found an element at the same column we want to add the new one: we need to replace it.
                selectedContainer = scannedContainer;
            } else if (currentColNumber > newColNumber) {
                // We found the next column, our column does not exist yet, we need to create it.
                selectedContainer = new SparseMatrixContainer();
                selectedContainer.col = newColNumber;
                selectedContainer.row = newRowNumber;
                selectedRow.add(physicalColNumber, selectedContainer);
            }
            physicalColNumber++;
        }
        if (selectedContainer == null) {
            // We must add our new container at the end of the row.
            selectedContainer = new SparseMatrixContainer();
            selectedContainer.col = newColNumber;
            selectedContainer.row = newRowNumber;
            selectedRow.add(selectedContainer);
        }

        // Set the actual value of our container
        selectedContainer.element = newElem;
    }

    public int getMaxRow() {
        try {
            ArrayList<SparseMatrixContainer> lastRow = rows.get(rows.size() - 1);
            SparseMatrixContainer firstContainer = lastRow.get(0);
            return firstContainer.row;
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    public int getMaxCol() {
        int maxCol = -1;
        for (ArrayList<SparseMatrixContainer> row : rows) {
            SparseMatrixContainer lastContainer = row.get(row.size() - 1);
            maxCol = Math.max(maxCol, lastContainer.col);
        }
        return maxCol;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean expand) {
        String text = "M{ max row: " + getMaxRow() + ", max col: " + getMaxCol() + "\n";
        if (expand) {

            for (int row = 0; row <= getMaxRow(); row++) {
                for (int col = 0; col <= getMaxCol(); col++) {
                    text += "(" + row + ", " + col + "): " + get(row, col) + " ";
                }
                text += "\n";
            }

        } else {
            text = "M{ max row: " + getMaxRow() + ", max col: " + getMaxCol() + "\n";
            for (ArrayList<SparseMatrixContainer> row : rows) {
                for (SparseMatrixContainer container : row) {
                    text += "(" + container.row + ", " + container.col
                            + "): " + container.element + "\n";
                }
            }
            text += "}";
        }
        return text;
    }
}
