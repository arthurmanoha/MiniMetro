package minimetro;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class behaves like a 2d matrix where a row or column may be added before
 * or after existing elements.
 *
 * @author arthu
 */
public class ExpandableMatrix<T> implements Iterable {

    private int firstRowIndex;
    private int firstColIndex;
    private int lastRowIndex;
    private int lastColIndex;

    private ArrayList<ArrayList<T>> elements;

    public ExpandableMatrix() {
        firstRowIndex = 0;
        firstColIndex = 0;
        elements = new ArrayList<>();
        elements.add(new ArrayList<>());
    }

    public void set(T newElem, int row, int col) {

    }

    private void ensureCapacity(int neededRow, int neededCol) {
        while (neededRow > lastRowIndex) {
            addRow(false);
        }
        while (neededRow < firstRowIndex) {
            addRow(true);
        }
        while (neededCol > lastColIndex) {
            addCol(false);
        }
        while (neededCol < firstColIndex) {
            addCol(true);
        }
    }

    public T get(int row, int col) {
        ensureCapacity(row, col);
        return elements.get(row).get(col);
    }

    public int getRow(T elem) {
        return getRowOrCol(elem, true);
    }

    public int getCol(T elem) {
        return getRowOrCol(elem, false);
    }

    private int getRowOrCol(T elem, boolean rowRequested) {
        for (int row = firstRowIndex; row < lastRowIndex; row++) {
            int rowInTab = row - firstRowIndex;

            for (int col = firstColIndex; col < lastColIndex; col++) {
                int colInTab = col - firstColIndex;

                if (elements.get(rowInTab).get(colInTab).equals(elem)) {
                    if (rowRequested) {
                        return row;
                    } else {
                        return col;
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    private void addRow(boolean addAtBeginning) {
        ArrayList<T> newRow = new ArrayList<>(getWidth());
        if (addAtBeginning) {
            elements.add(0, newRow);
            firstRowIndex--;
        } else {
            elements.add(newRow);
        }
    }

    private void addCol(boolean addAtBeginning) {
        int index;
        // 0 for beginning, width for end.
        if (addAtBeginning) {
            index = 0;
            firstColIndex--;
        } else {
            index = getWidth();
        }

        for (ArrayList row : elements) {
            row.add(index, null);
        }
    }

    private int getWidth() {
        return lastColIndex - firstColIndex + 1;
    }

    private int getHeight() {
        return lastRowIndex - firstRowIndex + 1;
    }

    @Override
    public Iterator iterator() {
        return new ExpandableMatrixIterator<>();

    }

    public void remove(T elem) {
        ArrayList<T> selectedRow = null;
        int selectedCol = -1;
        for (int row = 0; row < getHeight(); row++) {
            for (int col = 0; col < getWidth(); col++) {
                if (elements.get(row).get(col).equals(elem)) {
                    selectedRow = elements.get(row);
                    selectedCol = col;
                }
            }
        }
        if (selectedRow != null) {
            selectedRow.remove(selectedCol);
        }
    }

    private class ExpandableMatrixIterator<T> implements Iterator<T> {

        private int row, col;

        public ExpandableMatrixIterator() {
            row = 0;
            col = 0;
        }

        @Override
        public boolean hasNext() {
            return row < elements.size() && col < elements.get(0).size();
        }

        @Override
        public T next() {
            if (row == elements.size() - 1) {
                if (col == elements.get(0).size() - 1) {
                    // Already at last position
                    return null;
                }
                // Last element of a row, need to go to the next row
                row++;
                col = 0;
            }
            // Go to the next element of the same line.
            col++;
            return (T) elements.get(row).get(col);
        }
    }
}
