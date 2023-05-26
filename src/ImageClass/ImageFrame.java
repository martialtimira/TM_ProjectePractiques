package ImageClass;

import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Classe ImageFrame per a manipular imatges i gestionar tesseles
 */
public class ImageFrame {
    private BufferedImage image;
    private int id;
    private ArrayList<Tile> tiles;

    public ImageFrame(BufferedImage image, int id) {
        this.image = image;
        this.tiles = new ArrayList<>();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getId() {
        return this.id;
    }


    public ArrayList<Tile> getTiles() {
        return this.tiles;
    }

    public void setTiles(ArrayList<Tile> tiles) {
        this.tiles = tiles;
    }

}
