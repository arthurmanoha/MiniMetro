package minimetro;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author arthu
 * This class represents the same data as a 2d m*n matrix, and groups elements
 * in square chunks that can be loaded independantly.
 */
public class ChunkMatrix<T> {

    public static final int CHUNK_SIZE = 10;

    private ArrayList<Chunk> chunks;

    public ChunkMatrix() {
        chunks = new ArrayList<>();
    }

    private int getChunkRow(int rowNumber) {
        int chunkRow = rowNumber / CHUNK_SIZE;
        if (chunkRow < 0) {
            chunkRow--;
//            rowNumber += CHUNK_SIZE;
        }
        return chunkRow;
    }

    private int getChunkCol(int colNumber) {
        int chunkCol = colNumber / CHUNK_SIZE;
        if (colNumber < 0) {
            chunkCol--;
//            colNumber += CHUNK_SIZE;
        }
//        System.out.println("ChunkMatrix.getChunk(" + colNumber + "): " + chunkCol);
        return chunkCol;
    }

    /**
     * Make sure the chunk containing the specified cell exists; generate it if
     * necessary.
     *
     * @param row the row of the cell we need (not the chunk row)
     * @param col the column of the cell we need (not the chunk col)
     * @return true if the chunk was generated by the current call to thuis
     * function,
     * false if it already existed.
     */
    public boolean generateChunkIfNeeded(int row, int col) {

        boolean wasGeneratedNow = false;

        int chunkRow = getChunkRow(row);
        int chunkCol = getChunkCol(col);

        if (getChunk(chunkRow, chunkCol) == null) {

            Chunk newChunk = getChunk(chunkRow, chunkCol);
            chunks.add(newChunk);
            wasGeneratedNow = true;
        }
        return wasGeneratedNow;
    }

    private int getCellRow(int chunkRow) {
        return chunkRow * CHUNK_SIZE;
    }

    private int getCellCol(int chunkCol) {
        return chunkCol * CHUNK_SIZE;
    }

    /**
     * Get the chunk that has the specified indices. This function does not
     * create any chunk.
     *
     * @param chunkRow
     * @param chunkCol
     * @return the chunk if it exists, null otherwise.
     */
    public Chunk getChunk(int chunkRow, int chunkCol) {
        Chunk result = null;
        for (Chunk c : chunks) {
            if (c.rowIndexInMap == chunkRow && c.colIndexInMap == chunkCol) {
                result = c;
            }
        }
        return result;
    }

    public T get(int row, int col) {
        T result = null;
        int chunkRow = getChunkRow(row);
        int chunkCol = getChunkCol(col);
        Chunk c = getChunk(chunkRow, chunkCol);
        if (c != null) {
            result = (T) c.get(row - CHUNK_SIZE * chunkRow, col - CHUNK_SIZE * chunkCol);
        }
        return result;
    }

    public void set(T newElem, int newRowNumber, int newColNumber) {
//        System.out.println("ChunkMatrix.set(" + newElem + ", " + newRowNumber + ", " + newColNumber + ");");
        int chunkRow = getChunkRow(newRowNumber);
        int chunkCol = getChunkCol(newColNumber);
        Chunk newChunk = getChunk(chunkRow, chunkCol);

        int rowInChunk = newRowNumber - CHUNK_SIZE * chunkRow;
        int colInChunk = newColNumber - CHUNK_SIZE * chunkCol;
        newChunk.set(newElem, rowInChunk, colInChunk);
//        touchNeighboringChunks(chunkRow, chunkCol);
    }

//    private void touchNeighboringChunks(int chunkRow, int chunkCol) {
//
//    }
    protected Collection<T> toList() {
        ArrayList<T> result = new ArrayList<>();
        for (Chunk c : chunks) {
            if (c == null) {
                System.out.println("Error ChunkMatrix.toList()");
            } else {
                result.addAll(c.toList());
            }
        }
        return result;
    }

    public int getRow(T element) {
        for (Chunk c : chunks) {
            int row = c.getRow(element);
            if (row != Integer.MAX_VALUE) {
                return row + CHUNK_SIZE * c.rowIndexInMap;
            }
        }
        return Integer.MAX_VALUE;
    }

    public int getCol(T element) {
        for (Chunk c : chunks) {
            int col = c.getCol(element);
            if (col != Integer.MAX_VALUE) {
                return col + CHUNK_SIZE * c.colIndexInMap;
            }
        }
        return Integer.MAX_VALUE;
    }

    void remove(Cell oldCell) {
        System.out.println("ChunkMatrix.remove: TODO");
    }

    public ArrayList<Chunk> getAllChunks() {
        return chunks;
    }

    protected Chunk loadChunk(int chunkRow, int chunkCol) {
        System.out.println("loading chunk " + chunkRow + ", " + chunkCol);
        Chunk ch = getChunk(chunkRow, chunkCol);
        // Create all cells within this chunk.
        for (int cellRow = 0; cellRow < CHUNK_SIZE; cellRow++) {
            for (int cellCol = 0; cellCol < CHUNK_SIZE; cellCol++) {
                Cell cell = new Cell();
                ch.set(cell, cellRow, cellCol);
            }
        }
        return ch;
    }
}
