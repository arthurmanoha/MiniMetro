package minimetro;

/**
 *
 * @author arthu
 */
public class MiniMetro {

    public static void main(String[] args) {

        World w = new World();
        GUI gui = new GUI(w);

        w.addTestTracks(31, 33, 1, 4);
        w.addTestTracks(29, 30, 1, 80);
    }

}
