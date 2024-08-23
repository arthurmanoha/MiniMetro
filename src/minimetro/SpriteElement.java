package minimetro;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * This class represents any object that will be drawn using a sprite.
 *
 * @author arthu
 */
public abstract class SpriteElement {

    protected String imagePath;
    protected Image image;

    protected void loadImage() {
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException ex) {
            System.out.println("Image " + imagePath + " not found.");
        }
    }

    public void paint(Graphics g, double x0, double y0, double zoom) {
    }
}
