import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.*;
import java.util.ArrayList;
import ImageClass.ImageFrame;
import ImageClass.Tile;

/**
 * Classe que permet codificar una llista d'imatges seguint el mètode explicat a classe.
 */
public class Codifier {

    ArrayList<Pair> imageList = new ArrayList<>();
    int gop = 5, seekRange, nTiles, quality, height, width;
    ArrayList<ArrayList> gopListList = new ArrayList<>();
    ArrayList<ImageFrame> gopList = new ArrayList<>();

    ArrayList<ImageFrame> compressedFrameList = new ArrayList<>();
    ArrayList<Tile> tileList = new ArrayList<>();

    Utils utils;

    String outputPath;

    /**
     * Constructor de codificador
     * @param imageList Llista d'imatges a codificar
     * @param gop       GOP
     * @param nTiles    Numero de tiles en els que dividir els frames
     * @param seekRange SeekRange
     * @param quality   Qualitat en la que fer el tall
     * @param outputPath Path de l'output
     */
    public Codifier(ArrayList<Pair> imageList, int gop, int nTiles, int seekRange, int quality, String outputPath) {
        this.imageList = imageList;
        this.gop = gop;
        this.nTiles = nTiles;
        this.seekRange = seekRange;
        this.quality = quality;
        this.outputPath = outputPath;
        this.utils = new Utils();
    }

    /**
     * Metode per executar la codificació de les imatges
     */
    public void encode() {
        this.fillGOP();
        this.iterateGOP();
    }

    /**
     * S'omple la llista de grups d'imatges depenent del paràmentre GOP
     */
    private void fillGOP() {
        System.out.println("FILLING GOP");
        for(int i = 0; i < imageList.size(); i++) {
            if(i % gop == 0 || i + 1 >= imageList.size()) {
                if(!gopList.isEmpty()) {
                    if(i + 1 >= imageList.size()) {
                        gopList.add(new ImageFrame((BufferedImage) imageList.get(i).getSecond(), i));
                    }
                    //this.utils.saveZip(gopList, "newZip.zip");
                    gopListList.add(gopList);
                }
                gopList = new ArrayList<>();
            }
            gopList.add(new ImageFrame((BufferedImage) imageList.get(i).getSecond(), i));
        }
    }

    private void iterateGOP() {
        progressBar pb = new progressBar(gopListList.size());
        ImageFrame n, n_1;
        for(int p = 0; p < gopListList.size(); p++) {
            System.out.println("GOP " + p + "/" + gopListList.size());
            ArrayList<ImageFrame> currentGOPList = gopListList.get(p);
            for(int z = 0; z < currentGOPList.size() - 1; z++) {
                n = (ImageFrame) currentGOPList.get(z);
                if(z == 0) {
                    this.compressedFrameList.add(n);
                }
                n_1 = currentGOPList.get(z + 1);
                this.width = n_1.getImage().getWidth() / this.nTiles;
                this.height = n_1.getImage().getHeight() / this.nTiles;

                n.setTiles(subdivideImageTiles(n.getImage()));
                n.setTiles(findEqualTiles(n, n_1.getImage()));
                ImageFrame result = new ImageFrame(setColorPFrame(n.getTiles(), n_1.getImage()), 5);
                compressedFrameList.add(result);
                tileList.addAll(n.getTiles());
            }
            pb.update(p);
        }
        this.saveZip();
        System.out.println("DONE");
    }

    private ArrayList<Tile> subdivideImageTiles(BufferedImage image) {
        ArrayList<Tile> tiles = new ArrayList<>();

        Tile tile;

        int counter = 0;
        for (float y = 0; y < Math.round(image.getHeight()); y += this.height) {
            for(float x = 0; x < Math.round(image.getWidth()); x += this.width) {
                x = Math.round(x);
                y = Math.round(y);
                tile = new Tile(image.getSubimage((int)x, (int)y, (int)this.width, (int)this.height), counter);
                tiles.add(tile);
                counter++;
            }
        }
        //System.out.println("-------------------------------------------------------");
        //System.out.println("teseles:" + tiles.size());
        //System.out.println("-------------------------------------------------------");

        return tiles;
    }

    private ArrayList<Tile> findEqualTiles(ImageFrame iFrame, BufferedImage pFrame) {
        float maxPSNR;
        int xMaxValue = 0;
        int yMaxValue = 0;
        int x, y, minX, maxX, minY, maxY, id;
        ArrayList<Tile> resultTiles = new ArrayList<>();

        for (Tile tile: iFrame.getTiles()) {
            maxPSNR = Float.MIN_VALUE;
            id = tile.getId();

            x = ((int) Math.ceil(id/nTiles)) * height;
            y = (id % nTiles) * width;

            minX = Math.max((x - seekRange), 0);
            minY = Math.max((y - seekRange), 0);

            maxX = Math.min((x + height + seekRange), ((BufferedImage) imageList.get(0).getSecond()).getHeight());
            maxY = Math.min((y + width + seekRange), ((BufferedImage) imageList.get(0).getSecond()).getWidth());

            for(int i = minX; i <= maxX - height; i++) {
                for(int j = minY; j <= maxY - width; j++) {
                    float psnr = getPSNRScore(tile, pFrame.getSubimage(j, i, width, height));
                    //System.out.println("PSNR: " + psnr);
                    if (psnr > maxPSNR && psnr >= quality) {
                        maxPSNR = psnr;
                        xMaxValue = i;
                        yMaxValue = j;
                    }
                }
            }

            if(maxPSNR != Float.MIN_VALUE) {
                tile.setCoordX(xMaxValue);
                tile.setCoordY(yMaxValue);
            }
            else {
                tile.setCoordX(-1);
                tile.setCoordY(-1);
            }
            resultTiles.add(tile);
        }
        return resultTiles;
    }

    private float getPSNRScore(Tile tile, BufferedImage pFrame) {
        float noise = 0, mse = 0, psnr = 0;
        BufferedImage iFrame = tile.getTile();
        for(int y = 0; y < iFrame.getHeight(); y++) {
            for(int x = 0; x < iFrame.getWidth(); x++) {
                Color iFrameRGB = new Color(iFrame.getRGB(x, y));
                Color pFrameRGB = new Color(pFrame.getRGB(x, y));
                noise = (float) (noise + Math.pow(pFrameRGB.getRed() - iFrameRGB.getRed(), 2));
                noise = (float) (noise + Math.pow(pFrameRGB.getGreen() - iFrameRGB.getGreen(), 2));
                noise = (float) (noise + Math.pow(pFrameRGB.getBlue() - iFrameRGB.getBlue(), 2));
            }
        }
        mse = noise / (iFrame.getHeight() * iFrame.getWidth() * 3);
        psnr = (float) (10 * Math.log10(Math.pow(255, 2) / mse));
        return psnr;
    }

    private void createCoordFile() {
        try {
            String name = "Compressed/coords.txt";
            BufferedWriter writer = new BufferedWriter(new FileWriter(name));
            for(Tile tile: this.tileList) {
                writer.write(tile.getId() + " " + tile.getCoordX() + " " + tile.getCoordY() + "\n");
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Color getAverageColor(BufferedImage image) {
        Color color;
        int sumR = 0;
        int sumG = 0;
        int sumB = 0;
        int pixelCount = 0;
        int red, green, blue;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                color = new Color(image.getRGB(x, y));
                pixelCount++;
                sumR += color.getRed();
                sumG += color.getGreen();
                sumB += color.getBlue();
            }
        }

        red = sumR / pixelCount;
        green = sumG / pixelCount;
        blue = sumB / pixelCount;

        return new Color(red, green, blue);
    }
    private BufferedImage setColorPFrame(ArrayList<Tile> tiles, BufferedImage pFrame) {
        BufferedImage result = pFrame;
        tiles.forEach((tile) -> {
            int x = tile.getCoordX();
            int y = tile.getCoordY();
            if (x != -1 && y != -1) {
                Color color = getAverageColor(tile.getTile());
                for(int xCoord = x; xCoord < (x+height); xCoord++) {
                    for (int yCoord = y; yCoord < (y+width); yCoord++) {
                        result.setRGB(yCoord, xCoord, color.getRGB());
                    }
                }
            }
        });

        return result;
    }
    private void saveImages() {
        for(ArrayList<ImageFrame> frame: gopListList) {
            frame.forEach((file) -> {
                try {
                    JPEGCompressor.compress(file.getImage(), "Compressed/", "frame" + String.format("%02d", file.getId()) + ".jpeg");

                } catch (IOException e) {
                    System.err.println("IOException saving images" + e);
                }
            });
        }
    }

    /**
     * Guarda en un ZIP el fitxer de coordenades i les imatges codificades
     */
    private void saveZip() {
        new File("Compressed").mkdirs();
        this.createCoordFile();
        this.saveImages();
        this.utils.createZipFolder("Compressed", this.outputPath);
        File outputFile = new File(this.outputPath);
        this.utils.deleteDirectory(new File("Compressed"));
    }
}
