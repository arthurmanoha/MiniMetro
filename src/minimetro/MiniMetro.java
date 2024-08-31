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

        w.addTestTracks(14, 15, 10, 18);
        w.addTestTracks(16, 19, 12, 14);
        w.addTestTracks(16, 19, 16, 18);
        w.addTestTracks(17, 19, 5, 11);
        int nbWagons = 4;
        double xStart, yStart;
        xStart = 1560;
        yStart = 18498;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1160;
        yStart = 18498;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1202;
        yStart = 18227;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1606;
        yStart = 18239;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 503;
        yStart = 18120;
        w.addTestTrain(xStart, yStart, nbWagons);

        w.setSpeedLimitValue(30);
        w.setSpeedIndicator(14, 17);
        w.setSpeedIndicator(16, 17);
        w.setSpeedIndicator(17, 12);
        w.setSpeedIndicator(17, 11);
        w.setStopTimerDuration(10);
        w.toggleStation(14, 11);
        w.setStopTimer(14, 10);
        w.toggleStation(15, 13);
        w.setStopTimer(15, 14);
        w.toggleStation(16, 13);
        w.setStopTimer(16, 14);
        w.toggleStation(19, 13);
        w.setStopTimer(19, 12);
        w.toggleStation(18, 12);
        w.setStopTimer(17, 12);
        w.toggleStation(18, 11);
        w.setStopTimer(19, 11);
        w.toggleStation(17, 6);
        w.setStopTimer(17, 7);
        w.toggleStation(14, 15);
        w.setStopTimer(14, 14);
        w.toggleStation(15, 17);
        w.setStopTimer(15, 18);
        w.toggleStation(16, 17);
        w.setStopTimer(16, 18);
        w.toggleStation(19, 17);
        w.setStopTimer(19, 16);

        w.generatePassengers();
    }

}
