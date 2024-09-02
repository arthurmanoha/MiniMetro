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
        w.addTestTracks(12, 16, 7, 9);
        w.addTestTracks(17, 19, 9, 11);
        int nbWagons = 1;
        double xStart, yStart;
//        xStart = 1560;
//        yStart = 18498;
//        w.addTestTrain(xStart, yStart, nbWagons);
//        xStart = 1160;
//        yStart = 18498;
//        w.addTestTrain(xStart, yStart, nbWagons);
//        xStart = 1202;
//        yStart = 18227;
//        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1010;
        yStart = 18200;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1200;
        yStart = 18200;
        w.addTestTrain(xStart, yStart, nbWagons);
        xStart = 1200;
        yStart = 18400;
        w.addTestTrain(xStart, yStart, nbWagons);

//        w.setSpeedIndicator(14, 17);
//        w.setSpeedIndicator(16, 17);
//        w.setSpeedIndicator(17, 12);
//        w.setSpeedIndicator(17, 11);
        w.setStopTimerDuration(5);
        w.toggleStation(14, 11);
//        w.setStopTimer(14, 10);
        w.toggleStation(15, 13);
//        w.setStopTimer(15, 14);
        w.toggleStation(16, 13);
//        w.setStopTimer(16, 14);
        w.toggleStation(19, 13);
//        w.setStopTimer(19, 12);
        w.toggleStation(18, 12);
//        w.setStopTimer(17, 12);
        w.toggleStation(18, 11);
//        w.setStopTimer(19, 11);
        w.toggleStation(17, 6);
        w.setSpeedLimitValue(60);
        w.setSpeedIndicator(17, 6);
        w.setSpeedIndicator(17, 9);
        w.setSpeedIndicator(15, 12);
        w.setStopTimer(17, 9);
        w.setStopTimer(17, 11);
        w.toggleStation(14, 15);
//        w.setStopTimer(14, 14);
        w.toggleStation(15, 17);
//        w.setStopTimer(15, 18);
        w.toggleStation(12, 8);
//        w.setStopTimer(16, 18);
        w.toggleStation(16, 8);
//        w.setStopTimer(19, 16);
        w.toggleStation(17, 10);

        w.generatePassengers();
    }

}
