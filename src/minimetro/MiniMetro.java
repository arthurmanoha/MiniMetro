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

        w.addTestTracks(15, 16, 7, 11);
        w.addTestTracks(17, 18, 7, 12);
        int nbWagons = 1;
        double xStart, yStart;

        w.toggleStation(15, 10);
        w.toggleStation(16, 8);
        w.toggleStation(17, 8);
        w.toggleStation(18, 10);

        xStart = 754;
        yStart = 18400;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 859;
        yStart = 18200;
        w.addTestTrain(xStart, yStart, nbWagons);

        w.setSpeedLimitValue(10);
        w.setSpeedIndicator(15, 7);
        w.setSpeedIndicator(16, 9);
        w.setSpeedIndicator(17, 7);
        w.setSpeedIndicator(18, 9);
        w.setStopTimerDuration(5);
        w.setStopTimer(15, 9);
        w.setStopTimer(16, 9);
        w.setStopTimer(17, 7);
        w.setStopTimer(18, 11);

    }

}
