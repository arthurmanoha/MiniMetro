package minimetro;

/**
 *
 * @author arthu
 */
public class MiniMetro {

    public static void main(String[] args) {

        World w = new World();
        GUI gui = new GUI(w);


        w.addTestTracks(31, 33, 0, 7);
        double xStart = 3.7;
        double yStart = 5.5;
        int nbWagons = 20;
        w.addTestTrain(xStart, yStart, nbWagons);
        yStart = 7.5;
        nbWagons = 5;
        w.addTestTrain(xStart, yStart, nbWagons);
    }

}
