package minimetro;

/**
 *
 * @author arthu
 */
public class MiniMetro {

    public static void main(String[] args) {

        World w = new World();
        GUI gui = new GUI(w);

        w.addTestTracks(31, 33, 1, 7);
        double xStart = 370;
        double yStart = 550;
        int nbWagons = 10;
        w.addTestTrain(xStart, yStart, nbWagons);
    }

}
