package minimetro;

/**
 *
 * @author arthu
 */
public class MiniMetro {

    public static void main(String[] args) {

        World w = new World();
        GUI gui = new GUI(w);

        w.addTestTracks(32, 33, 1, 3);
        double xStart = 175;
        double yStart = 600;
        int nbWagons = 0;
        w.addTestTrain(xStart, yStart, nbWagons);
        w.setSpeedLimitValue(5);
        w.setSpeedIndicator(33, 2);
    }

}
