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

        w.addTestTracks(4, 5, 2, 6);
        w.addTestTracks(6, 7, 2, 8);
        double xStart = 349;
        double yStart = 500;
        int nbWagons = 0;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 549;
        yStart = 300;
        w.addTestTrain(xStart, yStart, nbWagons);
        w.setSpeedLimitValue(30);
        w.setSpeedIndicator(4, 3);
        w.setStopTimerDuration(0);
        w.setStopTimer(4, 2);
        w.setStopTimer(4, 4);
        w.setStopTimer(5, 5);
        w.toggleStation(4, 3);
        w.toggleStation(4, 5);
        w.toggleStation(5, 4);
        w.toggleStation(6, 4);
        w.setSpeedIndicator(6, 4);
        w.setStopTimer(6, 3);
        w.toggleStation(7, 5);
        w.setStopTimer(7, 4);
        w.toggleStation(7, 7);
        w.setStopTimer(7, 8);

        w.generatePassengers();
    }

}
