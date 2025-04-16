package minimetro;

import java.awt.Point;
import java.util.ArrayList;

/**
 * Generates terrain with Perlin noise and biomes.
 *
 * @author arthu
 */
public class BiomePerlinNoise extends PerlinNoise {

    private ArrayList<Point> biomeCenters;

    public BiomePerlinNoise(double initLargestSpatialPeriod) {
        super(initLargestSpatialPeriod);
    }

    @Override
    public double getNoise(double x, double y) {

        double factor = 0.3;
//        // Stretch half of the map:
//        double rescaledX = x, rescaledY = y;
//        double threshold = 2500;
//        if (x > threshold) {
//            rescaledX = threshold + factor * (x - threshold);
//        }
//        x = rescaledX;

        double r0 = 1500;
        double x0 = 2500, y0 = 2500;
        double dx = x - x0;
        double dy = y - y0;
        double radius = Math.sqrt(dx * dx + dy * dy);
        if (radius < r0) {
            x = factor * (x - x0);
            y = factor * (y - y0);
        }

////        System.out.println("radius2: " + radiusSquared + ", min: " + minRadius + ", max: " + maxRadius);
//        if (y > 4000) {
//            return super.getNoise(rescaledX, rescaledY) - 0.2;
//        } else if (y > 3000) {
//            return super.getNoise(rescaledX, rescaledY) + 0.0;
//        } else if (y > 2000) {
//            return super.getNoise(rescaledX, rescaledY) + 0.1;
//        } else if (y > 1000) {
//            return super.getNoise(rescaledX, rescaledY) / 3;
//        } else {
//            return super.getNoise(rescaledX, rescaledY) * 3;
//        }
        return super.getNoise(x, y);
    }
}
