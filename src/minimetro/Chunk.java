package minimetro;

import java.util.ArrayList;
import java.util.Collection;
import static minimetro.ChunkMatrix.CHUNK_SIZE;

/**
 * A chunk is a simple 2d array of fixed size.
 *
 */
public class Chunk<T> {

    protected int rowIndexInMap;
    protected int colIndexInMap;

    private T elements[][];

    public Chunk(int newRowInMap, int newColInMap) {

        rowIndexInMap = newRowInMap;
        colIndexInMap = newColInMap;
        elements = (T[][]) new Object[CHUNK_SIZE][];
        for (int row = 0; row < CHUNK_SIZE; row++) {
            elements[row] = (T[]) new Object[CHUNK_SIZE];
        }
    }

    public T get(int row, int col) {
        return elements[row][col];
    }

    public void set(T newElem, int newRowNumber, int newColNumber) {
        elements[newRowNumber][newColNumber] = newElem;
    }

    /**
     * Return a list with all the non-null elements of this chunk.
     *
     * @return
     */
    public Collection<T> toList() {
        ArrayList<T> result = new ArrayList<>();
        for (int row = 0; row < CHUNK_SIZE; row++) {
            for (int col = 0; col < CHUNK_SIZE; col++) {
                T element = elements[row][col];
                if (element != null) {
                    result.add((T) element);
                }
            }
        }
        return result;
    }

    protected int getRow(T element) {
        for (int row = 0; row < CHUNK_SIZE; row++) {
            for (int col = 0; col < CHUNK_SIZE; col++) {
                if (get(row, col).equals(element)) {
                    return row;
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    protected int getCol(T element) {
        for (int row = 0; row < CHUNK_SIZE; row++) {
            for (int col = 0; col < CHUNK_SIZE; col++) {
                if (get(row, col).equals(element)) {
                    return col;
                }
            }
        }
        return Integer.MAX_VALUE;
    }
}
