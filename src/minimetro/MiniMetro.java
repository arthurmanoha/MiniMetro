package minimetro;

/**
 *
 * @author arthu
 */
public class MiniMetro {

    public static boolean TEST_PASSENGERS = false;

    public static void main(String[] args) {

        World w = new World();
        GUI gui = new GUI(w);

        w.addTestTracks(6, 9, 0, 6);
        double xStart = 400;
        double yStart = 300;
        int nbWagons = 4;
        w.addTestTrain(xStart, yStart, nbWagons);
        w.setSpeedLimitValue(10);
        w.setSpeedIndicator(6, 4);
        w.setSpeedIndicator(7, 6);
        w.setSpeedLimitValue(30);
        w.setSpeedIndicator(7, 0);
        w.setStopTimerDuration(10);
        w.setStopTimer(6, 0);
        w.setStopTimer(6, 2);
        w.setStopTimer(6, 5);
        w.toggleStation(6, 1);
        w.toggleStation(6, 3);
        w.toggleStation(6, 6);

        w.generatePassengers();
    }

}
