package ImageClass;

import java.awt.image.BufferedImage;

public class Tile {
    private int id, coordX, coordY, x, y;
    private BufferedImage tile;

    public Tile(BufferedImage tile, int id) {
        this.id = id;
        this.tile = tile;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCoordX() {
        return coordX;
    }

    public void setCoordX(int coordX) {
        this.coordX = coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setCoordY(int coordY) {
        this.coordY = coordY;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public BufferedImage getTile() {
        return tile;
    }

    public void setTile(BufferedImage tile) {
        this.tile = tile;
    }
}
