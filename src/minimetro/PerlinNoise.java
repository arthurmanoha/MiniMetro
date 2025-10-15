package minimetro;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author arthu
 */
public class PerlinNoise {

    private static long seed = 264305296l;
    protected static Random r;

    // The largest spatial period of the noise, i.e. the smallest frequency
    private double largestSpatialPeriod;

    private ArrayList<Integer> amplitudesPercentagesList;
    private int nbLevels = 7;

    public PerlinNoise(double initLargestSpatialPeriod, long newSeed) {
        largestSpatialPeriod = initLargestSpatialPeriod;
        seed = newSeed;
        r = new Random(seed);

        // Set all harmonics to 100%.
        amplitudesPercentagesList = new ArrayList<>(nbLevels);
        for (int level = 0; level < nbLevels; level++) {
            amplitudesPercentagesList.add(level, 100);
        }
    }

    public double getNoise(double x, double y) {
        double result = 0;
        double spatialPeriod = largestSpatialPeriod;
        double divisor = 1;
        for (int level = 0; level < nbLevels; level++) {
            double amplitude = ((double) amplitudesPercentagesList.get(level)) / 100;
            result += amplitude * getNoise(x, y, spatialPeriod) / divisor;
            spatialPeriod = spatialPeriod / 2;
            divisor = divisor * 2;
        }
        return result;
    }

    public double getNoise(double x, double y, double currentScale) {

        // Position of the cell in the noise grid
        int perlinCol = (int) (x / currentScale);
        int perlinRow = (int) (y / currentScale);

        // Fix the negative x or y parts of the map.
        if (x < 0) {
            perlinCol--;
        }
        if (y < 0) {
            perlinRow--;
        }

        double dotSW = dotprod(perlinRow, perlinCol, x, y, currentScale);
        double dotSE = dotprod(perlinRow, perlinCol + 1, x, y, currentScale);
        double dotNW = dotprod(perlinRow + 1, perlinCol, x, y, currentScale);
        double dotNE = dotprod(perlinRow + 1, perlinCol + 1, x, y, currentScale);

        // Coordinates of the point within the Perlin cell, remapped from 0 to 1.
        double xP = x / currentScale - perlinCol;
        double yP = y / currentScale - perlinRow;

        // Interpolate between the 4 dot products
        // The square ABCD represents the points NW, NE, SE, SW
        // Interpolation is done on segments [AB] (north segment) and [DC] (south segment)
        // South:
        double f_xp_0 = interpolate(xP, dotSW, dotSE);
        // North:
        double f_xp_1 = interpolate(xP, dotNW, dotNE);

        // Last step of the interpolation is done between (xP, 0) and (xP, 1)
        double result = interpolate(yP, f_xp_0, f_xp_1);
        return result;
    }

    /**
     * Compute the dot product between the gradient on the Perlin grid at (row,
     * col) and the offset vector to the (x, y) point.
     */
    public double dotprod(int row, int col, double x, double y, double scale) {
        // Offset is in [-1,0]
        double offsetX = ((col) * scale - x) / scale;
        double offsetY = ((row) * scale - y) / scale;

        double gradX = getGradX(row, col);
        double gradY = getGradY(row, col);

        // dotProd in in [-1, 1]
        double dotProd = offsetX * gradX + offsetY * gradY;
        return dotProd;
    }

    public double getGradX(int row, int col) {
        r.setSeed(seed * 73 * (row + 23) * (col + 37));
        return r.nextDouble() - 0.5;
    }

    public double getGradY(int row, int col) {
        r.setSeed(seed * 19 * (row + 13) * (col + 19));
        return r.nextDouble() - 0.5;
    }

    /**
     * Perform an interpolation.
     *
     * @param x the argument, between 0 and 1
     * @param val0 the value of the function when interpolated at zero
     * @param val1 the value of the function when interpolated at one
     * @return the value of the function when interpolated at @param x
     */
    public double interpolate(double x, double val0, double val1) {

        // Linear interpolation
        return x * val1 + (1 - x) * val0;
    }

    public void setAmplitudes(int newAmplitude, int level) {

        amplitudesPercentagesList.set(level, newAmplitude);
    }
}
