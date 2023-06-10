/* TODO enlloc de dividir el quadre desti en teseles regulars i buscar
    coincidencies al quadre origen, ferho al reves.
    Primer dividir el quadre origen en teseles qudriculades i despres
    buscar coincidencies al quadre desti.
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import ImageClass.ImageFrame;
import ImageClass.Tile;

/**
 * Classe que permet codificar una llista d'imatges seguint el mètode explicat a classe.
 */
public class Codifier {

    private final ArrayList<Pair> imageList;
    private final int gop;
    private final int seekRange;
    private final int nTiles;
    private int nTilesX, nTilesY;
    private final int quality;
    private int height;
    private int width;
    private final ArrayList<ArrayList<ImageFrame>> gopListList = new ArrayList<>();
    private ArrayList<ImageFrame> gopList = new ArrayList<>();

    private final ArrayList<ImageFrame> compressedFrameList = new ArrayList<>();
    private final ArrayList<Tile> tileList = new ArrayList<>();

    private final Utils utils;

    private final String outputPath;

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
        this.width =  nTiles;
        this.height = nTiles;
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
        for(int i = 0; i < imageList.size(); i++) {
            if(i % gop == 0 || i + 1 >= imageList.size()) {
                if(!gopList.isEmpty()) {
                    if(i + 1 >= imageList.size()) {
                        gopList.add(new ImageFrame(imageList.get(i).getSecond(), i));
                    }
                    gopListList.add(gopList);
                }
                gopList = new ArrayList<>();
            }
            ImageFrame iframe = new ImageFrame(imageList.get(i).getSecond(), i);
            gopList.add(iframe);
        }
    }

    private void iterateGOP() {
        progressBar pb = new progressBar(gopListList.size());
        ImageFrame n, n_1;
        for(int p = 0; p < gopListList.size(); p++) {
            ArrayList<ImageFrame> currentGOPList = gopListList.get(p);
            n = currentGOPList.get(0);
            this.compressedFrameList.add(n);
            //System.out.println("Base image ID: " + n.getId());
            for(int z = 1; z < currentGOPList.size(); z++) {
                n_1 = currentGOPList.get(z);

                n.setTiles(subdivideImageTiles(n.getImage()));
                n.setTiles(findEqualTiles(n, n_1.getImage()));
                ImageFrame result = new ImageFrame(setColorPFrame(n.getTiles(), n_1.getImage()), 1);
                compressedFrameList.add(result);
                tileList.addAll(n.getTiles());
                pb.update(p);
            }

        }
        this.saveZip();
        System.out.println("DONE");
    }

    private ArrayList<Tile> subdivideImageTiles(BufferedImage image) {
        ArrayList<Tile> tiles = new ArrayList<>();

        Tile tile;

        int counter = 0, counterY = 0, counterX = 0;
        //System.out.println("IMAGE DIMENSIONS: x = " + image.getWidth() + " y = " + image.getHeight());
        //System.out.println("TILE SIZE: " + this.nTiles);
        for (float y = 0; y < (image.getHeight() - this.height); y += this.height) {
            for(float x = 0; x < (image.getWidth() - this.width); x += this.width) {
                x = Math.round(x);
                y = Math.round(y);
                tile = new Tile(image.getSubimage((int)x, (int)y, this.width, this.height), counter);
                tile.setX((int)x);
                tile.setY((int)y);
                tiles.add(tile);
                counter++;
            }
            counterY++;
        }

        counterX = counter/counterY;
        this.nTilesX = counterX;
        this.nTilesY = counterY;
        //System.out.println("TOTAL Tiles Generated: " + counter);
        //System.out.println("XTiles: " + counterX);
        //System.out.println("YTiles: " + counterY);
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

            //Might need to change this for the new nTiles
            x = ((int) Math.ceil(id/nTilesX)) * height;
            y = (id % nTilesY) * width;

            minX = Math.max((x - seekRange), 0);
            minY = Math.max((y - seekRange), 0);

            maxX = Math.min((x + height + seekRange), imageList.get(0).getSecond().getHeight());
            maxY = Math.min((y + width + seekRange), imageList.get(0).getSecond().getWidth());

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
            }else {
                tile.setCoordX(-1);
                tile.setCoordY(-1);
            }
            resultTiles.add(tile);
        }
        return resultTiles;
    }

    private float getPSNRScore(Tile tile, BufferedImage pFrame) {
        float noise = 0, mse, psnr;
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
        String name = "Compressed/coords.bin";
        int last = 0;
        boolean added = false;
        try(FileOutputStream fos = new FileOutputStream(name)) {
            fos.write(getHeader());

            for(Tile tile : this.tileList) {
                if (tile.getId() < last) {
                    if(!added) {
                        fos.write(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}); // Frame sense coincidences
                    }
                    else added = false;
                }
                if(tile.getCoordX() != -1 && tile.getCoordY() != -1) {
                    fos.write(tileToBin(tile));
                    last = tile.getId();
                    added = true;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] tileToBin(Tile tile) {
        byte[] idB = ByteBuffer.allocate(4).putInt(tile.getId()).array();
        byte[] xB = ByteBuffer.allocate(4).putInt(tile.getCoordX()).array();
        byte[] yB = ByteBuffer.allocate(4).putInt(tile.getCoordY()).array();

        return new byte[]{idB[2], idB[3], xB[2], xB[3], yB[2], yB[3]};
    }


    private byte[] getHeader() {
        byte[] gopB = ByteBuffer.allocate(4).putInt(gop).array();
        byte[] tilesB = ByteBuffer.allocate(4).putInt(nTiles).array();
        byte[] sizeB = ByteBuffer.allocate(4).putInt(imageList.size()).array();

        return new byte[]{gopB[3], tilesB[3], sizeB[2], sizeB[3]};
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
            int imageX = tile.getX();
            int imageY = tile.getY();
            int x = tile.getCoordX();
            int y = tile.getCoordY();
            //System.out.println("imageX: " + imageX + " x: " + x);
            //System.out.println("imageY: " + imageY + " y: " + y);
            if (x != -1 && y != -1) {
                Color color = getAverageColor(tile.getTile());
                for(int xCoord = imageY; xCoord < (imageY+height); xCoord++) {
                    for (int yCoord = imageX; yCoord < (imageX+width); yCoord++) {
                        //System.out.println("IMAGE: " + pFrame.getWidth() + "Y: " + pFrame.getHeight());
                        //System.out.println("X: " + xCoord + "Y: " + yCoord);
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