import java.awt.image.BufferedImage;

public class Negative {

    public static void applyNegativeFilter(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int rgb = image.getRGB(x, y);
                int red = 255 - ((rgb >> 16) & 0xff);
                int green = 255 - ((rgb >> 8) & 0xff);
                int blue = 255 - (rgb & 0xff);
                int negativeRgb = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, negativeRgb);
            }
        }
    }
}