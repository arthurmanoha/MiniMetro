package minimetro;

/**
 * This class represents a trainElement that is leaving one cell in a specific
 * direction.
 *
 * @author arthu
 */
public class TransferringTrain {

    private TrainElement te;
    private CardinalPoint direction;

    public TransferringTrain(TrainElement trainElement, CardinalPoint leavingDirection) {
        this.te = trainElement;
        this.direction = leavingDirection;
    }

    public TrainElement getTrainElement() {
        return this.te;
    }

    public CardinalPoint getDirection() {
        return this.direction;
    }

}
