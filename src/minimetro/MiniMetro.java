package minimetro;

/**
 *
 * @author arthu
 */
public class MiniMetro {

    public static boolean TEST_PASSENGERS = true;

    public static void main(String[] args) {

        World w = new World();
        GUI gui = new GUI(w);

        w.addTestTracks(6, 7, 0, 4);
        double xStart = 149;
        double yStart = 300;
        int nbWagons = 1;
        w.addTestTrain(xStart, yStart, nbWagons);
        w.setSpeedLimitValue(10);
        w.setSpeedIndicator(6, 1);
        w.setStopTimerDuration(10);
        w.setStopTimer(6, 0);
        w.setStopTimer(6, 2);
        w.setStopTimer(7, 3);
        w.toggleStation(6, 1);
        w.toggleStation(6, 3);
        w.toggleStation(7, 2);

        w.generatePassengers();
    }

}
