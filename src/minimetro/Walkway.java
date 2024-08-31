package minimetro;

/**
 * This class represents a path between two stations that a passenger may walk
 * through.
 *
 * @author arthu
 */
public class Walkway {

    private int firstStationId, secondStationId;

    public Walkway(int firstId, int secondId) {
        firstStationId = firstId;
        secondStationId = secondId;
    }

    public int getStation(int rank) {
        switch (rank) {
        case 0:
            return firstStationId;
        case 1:
            return secondStationId;
        default:
            return -1;
        }
    }
}
