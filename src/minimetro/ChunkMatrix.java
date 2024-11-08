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
        return rowNumber / CHUNK_SIZE;
    }

    private int getChunkCol(int colNumber) {
        return colNumber / CHUNK_SIZE;
    }

    private int getCellRow(int chunkRow) {
        return chunkRow * CHUNK_SIZE;
    }

    private int getCellCol(int chunkCol) {
        return chunkCol * CHUNK_SIZE;
    }

    /**
     * Get the chunk that has the specified indices.
     *
     * @param chunkRow
     * @param chunkCol
     * @return
     */
    public Chunk getChunk(int chunkRow, int chunkCol) {
        for (Chunk c : chunks) {
            if (c.rowIndexInMap == chunkRow && c.colIndexInMap == chunkCol) {
                return c;
            }
        }
        // At this stage, the chunk does not exist. We need to create it.
        Chunk c = new Chunk(chunkRow, chunkCol);
        chunks.add(c);
        return c;
    }

    public T get(int row, int col) {
        int chunkRow = getChunkRow(row);
        int chunkCol = getChunkCol(col);
        Chunk c = getChunk(chunkRow, chunkCol);
        return (T) c.get(row - CHUNK_SIZE * chunkRow, col - CHUNK_SIZE * chunkCol);
    }

    public void set(T newElem, int newRowNumber, int newColNumber) {
        int chunkRow = getChunkRow(newRowNumber);
        int chunkCol = getChunkCol(newColNumber);
        Chunk newChunk = getChunk(chunkRow, chunkCol);

        int rowInChunk = newRowNumber - CHUNK_SIZE * chunkRow;
        int colInChunk = newColNumber - CHUNK_SIZE * chunkCol;
        newChunk.set(newElem, rowInChunk, colInChunk);
    }

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
                return row;
            }
        }
        return Integer.MAX_VALUE;
    }

    public int getCol(T element) {
        for (Chunk c : chunks) {
            int row = c.getRow(element);
            if (row != Integer.MAX_VALUE) {
                return row;
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

    protected void loadChunk(int chunkRow, int chunkCol) {
        Chunk ch = getChunk(chunkRow, chunkCol);
        // Create all cells within this chunk.
        for (int cellRow = 0; cellRow < CHUNK_SIZE; cellRow++) {
            for (int cellCol = 0; cellCol < CHUNK_SIZE; cellCol++) {
                Cell cell = new Cell();
                ch.set(cell, cellRow, cellCol);
            }
        }
    }
}
