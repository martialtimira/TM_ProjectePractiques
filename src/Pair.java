import java.awt.image.BufferedImage;

public class Pair {
    private String first;
    private BufferedImage second;

    public Pair(String first, BufferedImage second) {
        this.first = first;
        this.second = second;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public BufferedImage getSecond() {
        return second;
    }

    public void setSecond(BufferedImage second) {
        this.second = second;
    }
}