package minimetro;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author arthu
 * This class represents the same data as a 2d m*n matrix, but in a way that
 * makes it efficient for matrices with a low density.
 * @param <T> The type of elements stored in this matrix
 */
public class SparseMatrix<T> implements Iterable<T> {

    private ArrayList<SparseMatrixContainer> containers;

    public SparseMatrix() {
        this.containers = new ArrayList<>();
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

        if (containers == null || containers.isEmpty()) {
            return null;
        }

        for (SparseMatrixContainer container : containers) {
            if (container.row == rowNumber && container.col == colNumber) {
                return (T) container.element;
            }
        }

        // At this step the element does not exist in the selected row.
        return null;
    }

    public void set(T newElem, int newRowNumber, int newColNumber) {

        int rank = 0;
        while (rank < containers.size() && (containers.get(rank).row < newRowNumber
                || (containers.get(rank).row == newRowNumber && containers.get(rank).col < newColNumber))) {
            rank++;
        }

        SparseMatrixContainer containerForNewElem;
        if (rank < containers.size() && containers.get(rank).row == newRowNumber && containers.get(rank).col == newColNumber) {
            // We must replace this container
            containerForNewElem = containers.get(rank);
        } else {
            // We must add this container.
            containerForNewElem = new SparseMatrixContainer();
            containers.add(rank, containerForNewElem);
            containerForNewElem.row = newRowNumber;
            containerForNewElem.col = newColNumber;
        }
        containerForNewElem.element = newElem;
    }

    public int getMaxRow() {
        int max = Integer.MIN_VALUE;
        for (SparseMatrixContainer container : containers) {
            max = Math.max(max, container.row);
        }
        return max;
    }

    public int getMaxCol() {
        int max = Integer.MIN_VALUE;
        for (SparseMatrixContainer container : containers) {
            max = Math.max(max, container.col);
        }
        return max;
    }

    /**
     * Return the index of the row that contains the specified element, or
     * Integer.MIN_VALUE if the element is not found.
     *
     * @param elem
     * @return
     */
    public int getRow(T elem) {
        for (SparseMatrixContainer container : containers) {
            if (container.element.equals(elem)) {
                return container.row;
            }
        }
        return Integer.MIN_VALUE;
    }

    /**
     * Return the index of the column that contains the specified element, or
     * Integer.MIN_VALUE if the element is not found.
     *
     * @param elem
     * @return
     */
    public int getCol(T elem) {
        for (SparseMatrixContainer container : containers) {
            if (container.element.equals(elem)) {
                return container.col;
            }
        }
        return Integer.MIN_VALUE;
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
            for (SparseMatrixContainer container : containers) {
                text += "(" + container.row + ", " + container.col
                        + "): " + container.element + "\n";
            }
            text += "}";
        }
        return text;
    }

    private class SparseMatrixIterator<T> implements Iterator<T> {

        private int rank;

        public SparseMatrixIterator() {
            rank = 0;
        }

        /**
         * This iterator has a next element if it is not yet at the last row,
         * or not yet at the last element of the last row.
         *
         * @return
         */
        @Override
        public boolean hasNext() {
            return rank < containers.size() - 1;
        }

        @Override
        public T next() {
            // Next element is on the same line, increase physicalRow and return the element.
            return (T) containers.get(rank++).element;
        }
    }

    protected Collection<T> toList() {
        ArrayList<T> result = new ArrayList<>();
        for (SparseMatrixContainer container : containers) {
            result.add((T) container.element);
        }
        return result;
    }

    public Iterator<T> iterator() {
        return new SparseMatrixIterator<>();
    }

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
}
