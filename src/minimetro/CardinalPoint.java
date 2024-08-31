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
}
