package minimetro;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Generates terrain with Perlin noise and biomes.
 *
 * @author arthu
 */
public class BiomePerlinNoise extends PerlinNoise {

    /*
      Biome centers shall be generated from the origin out. It not enough
        centers exist to find the biome of any coordinates, we regenerate that list
        with more elements.
     */
    private ArrayList<Point> biomeCenters;
    private int nbBiomes = 500;
    private int maxDistance = 5;

    public BiomePerlinNoise(double initLargestSpatialPeriod, long seed) {
        super(initLargestSpatialPeriod, seed);
        initBiomeCenters();
        System.out.println("Constructing BiomePerlinNoise "
                + "with period " + initLargestSpatialPeriod
                + " and seed " + seed);
    }

    @Override
    public double getNoise(double x, double y) {

        return super.getNoise(x, y);
    }

    private void initBiomeCenters() {
        int size = (int) Cell.cellSize;
        biomeCenters = new ArrayList<>();
        double radius = 0;
        for (int i = 0; i < nbBiomes; i++) {
            // Generate the coordinates of one biome vertex

            // Each biome center is positioned at random in an annular region around the origin;
            // all such regions have the same area so that the points are spread evenly.
            if (i > 0) {
                radius += 1 / Math.sqrt((double) i);
            }
            double bearing = r.nextDouble() * 2 * Math.PI;

            double radiusMultiplier = 250;

            int xBiome = size * (((int) (radiusMultiplier * radius * Math.cos(bearing))) / size);
            int yBiome = size * (((int) (radiusMultiplier * radius * Math.sin(bearing))) / size);
            biomeCenters.add(new Point(xBiome, yBiome));
        }
    }

    /**
     * Find which biome the point (x, y) belongs to.
     *
     * @param x
     * @param y
     * @return
     */
    protected int getBiome(double x, double y) {
        double minDistance = Double.MAX_VALUE;
        int closestBiomeIndex = -1;
        for (int currentIndex = 0; currentIndex < biomeCenters.size(); currentIndex++) {
            Point biomeCenter = biomeCenters.get(currentIndex);
            double currentDistance = getDistance(biomeCenter.x, biomeCenter.y, x, y);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                closestBiomeIndex = currentIndex;
            }
        }
        return closestBiomeIndex;
    }

    private double getDistance(int x, int y, double x0, double y0) {
        double dx = x - x0;
        double dy = y - y0;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public ArrayList<Point> getCenters() {
        ArrayList centersClone = new ArrayList();
        for (Point p : biomeCenters) {
            centersClone.add(new Point(p));
        }
        return centersClone;
    }
}
