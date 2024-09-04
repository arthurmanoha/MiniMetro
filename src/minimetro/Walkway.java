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

    protected boolean contains(int currentStationId) {
        return firstStationId == currentStationId || secondStationId == currentStationId;
    }

    /**
     * Get one of the two stations linked by this walkway,
     * but not the one specified with currentStationId. By default, return the
     * first id.
     *
     * @param currentStationId
     * @return
     */
    protected int getOtherStation(int currentStationId) {
        if (firstStationId == currentStationId) {
            return secondStationId;
        } else {
            return firstStationId;
        }
    }

    @Override
    public String toString() {
        return "w " + firstStationId + " " + secondStationId;
    }
}
