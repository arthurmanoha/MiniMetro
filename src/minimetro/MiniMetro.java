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
        double xStart = 1652;
        double yStart = 18498;
        int nbWagons = 4;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1422;
        yStart = 18398;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1202;
        yStart = 18227;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1606;
        yStart = 18139;
        w.addTestTrain(xStart, yStart, nbWagons);

        w.setSpeedLimitValue(60);
        w.setSpeedIndicator(14, 17);
        w.setSpeedIndicator(16, 17);
        w.setSpeedIndicator(17, 12);
        w.setStopTimerDuration(0);
        w.toggleStation(14, 11);
        w.toggleStation(15, 13);
        w.toggleStation(16, 13);
        w.toggleStation(19, 13);
        w.toggleStation(14, 15);
        w.toggleStation(15, 17);
        w.toggleStation(16, 17);
        w.toggleStation(19, 17);

        w.generatePassengers();
    }

}
