package ImageClass;

public class Coords {
    private int frame;
    private int group;
    private int id;
    private int x;
    private int y;

    public Coords(int frame, int group, int id, int x, int y) {
        this.frame = frame;
        this.group = group;
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getFrame() { return frame;}
    public int getGroup() {return group;}
    public int getId() {return id;}
    public int getX() {return x;}
    public int getY() {return  y;}

    public int[] getIdAsCoords(int tileSize, int frameLength, int frameHeight) {
        return new int[]{(int) (Math.ceil(id/tileSize) * frameHeight),(id%tileSize) * frameLength };
    }
}
