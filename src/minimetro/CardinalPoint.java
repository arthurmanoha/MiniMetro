package minimetro;

/**
 *
 * @author arthu
 */
public enum CardinalPoint {
    NORTH,
    NORTHEAST,
    EAST,
    SOUTHEAST,
    SOUTH,
    SOUTHWEST,
    WEST,
    NORTHWEST,
    CENTER;

    public static CardinalPoint getOpposite(CardinalPoint p) {
        if (p == null) {
            return null;
        }
        switch (p) {
        case NORTH:
            return SOUTH;
        case NORTHEAST:
            return SOUTHWEST;
        case EAST:
            return WEST;
        case SOUTHEAST:
            return NORTHWEST;
        case SOUTH:
            return NORTH;
        case SOUTHWEST:
            return NORTHEAST;
        case WEST:
            return EAST;
        case NORTHWEST:
            return SOUTHEAST;
        default:
            return CENTER;
        }
    }

    protected int getIntValue() {
        switch (this) {
        case NORTH:
            return 0;
        case NORTHEAST:
            return 1;
        case EAST:
            return 2;
        case SOUTHEAST:
            return 3;
        case SOUTH:
            return 4;
        case SOUTHWEST:
            return 5;
        case WEST:
            return 6;
        case NORTHWEST:
            return 7;
        default:
            return -1;
        }
    }

    protected int difference(CardinalPoint other) {
        if (this.equals(CENTER) || other.equals(CENTER)) {
            return Integer.MAX_VALUE;
        }
        return (this.getIntValue() - other.getIntValue() + 8) % 8;
    }

}
