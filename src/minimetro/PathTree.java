package minimetro;

/**
 *
 * @author arthu
 */
public class PathTree {

    // The station represented by this node.
    private final int stationId;
    // The station that got us to this node. -1 if no previous station.
    private final PathTree originStationId;

    public PathTree(int newStationId, PathTree newOoriginStationId) {
        this.stationId = newStationId;
        this.originStationId = newOoriginStationId;
    }

    public int getStationId() {
        return stationId;
    }

    public PathTree getPrev() {
        return originStationId;
    }

// <editor-fold defaultstate="collapsed" desc="old tree version">
//    private int stationId;
//    private ArrayList<PathTree> neighbors;
//
//    public PathTree(int currentStationId) {
//        stationId = currentStationId;
//        neighbors = new ArrayList<>();
//    }
//    /**
//     * Return true if the specified station id is contained in the tree, false
//     * otherwise.
//     *
//     * @param targetStationId
//     * @return
//     */
//    protected boolean contains(int targetStationId) {
//        if (stationId == targetStationId) {
//            return true;
//        }
//        for (PathTree neighbor : neighbors) {
//            if (neighbor.contains(targetStationId)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void print() {
//        print(0);
//    }
//
//    private void print(int depth) {
//        String offset = computeOffset(depth);
//        System.out.println(offset + stationId);
//        for (PathTree subtree : neighbors) {
//            subtree.print(depth + 1);
//        }
//    }
//
//    private String computeOffset(int depth) {
//        String res = "";
//        for (int i = 0; i < depth; i++) {
//            res += "-";
//        }
//        return res;
//    }
//
//    /**
//     * Find and return the first leave, doing a horizontal tree traversal.
//     * (i.e. scan the root, then all nodes one level deep, then all nodes two
//     * levels deep...
//     *
//     * @return
//     */
//    protected int getFirstLeave() {
//
//    }
// </editor-fold>
}
