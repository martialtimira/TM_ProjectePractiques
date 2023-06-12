import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
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

    /**
     * Itera tots els frames de cada GOP, divideix els frames amb tessel·les i busca les similars al frame de
     * referència.
     */
    private void iterateGOP() {
        progressBar pb = new progressBar(gopListList.size());
        ImageFrame n, n_1;
        for(int p = 0; p < gopListList.size(); p++) {
            ArrayList<ImageFrame> currentGOPList = gopListList.get(p);
            n = currentGOPList.get(0);
            this.compressedFrameList.add(n);
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

    /**
     * Divideix el frame en tessel·les.
     * @param image imatge a dividir.
     * @return llista amb les tessel·les de la imatge.
     */
    private ArrayList<Tile> subdivideImageTiles(BufferedImage image) {
        ArrayList<Tile> tiles = new ArrayList<>();

        Tile tile;

        int counter = 0, counterY = 0, counterX = 0;
        for (float y = 0; y < image.getHeight(); y += this.height) {
            for(float x = 0; x < image.getWidth(); x += this.width) {
                x = Math.round(x);
                y = Math.round(y);
                if(x+this.height <= image.getWidth() && y+this.width <= image.getHeight()){
                    tile = new Tile(image.getSubimage((int)x, (int)y, this.width, this.height), counter);
                    tile.setX((int)x);
                    tile.setY((int)y);
                    tiles.add(tile);
                    counter++;
                }
            }
            if(y+this.width <= image.getHeight()) {
                counterY++;
            }
        }

        counterX = counter/counterY;
        this.nTilesX = counterX;
        this.nTilesY = counterY;
        return tiles;
    }

    /**
     * Busca les al frame de referència les tessel·les de l'iframe.
     * @param iFrame frame que ha de ser comprimit.
     * @param pFrame frame de referència.
     * @return Llista amb les tessel·les que s'han trobat el pframe.
     */
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
            }
            else {
                tile.setCoordX(-1);
                tile.setCoordY(-1);
            }
            resultTiles.add(tile);
        }
        return resultTiles;
    }

    /**
     * Calcula el psnr per determinar si dues tessel·les són prou similars.
     * @param tile Tessel·la a calcular el psnr.
     * @param pFrame Frame de referència.
     * @return Coeficient del psnr.
     */
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

    /**
     * Crea el fitxer amb les coordenades de les tessel·les que han estat substituïdes i on es poden trobar al frame
     * de referència.
     */
    private void createCoordFile() {
        String name = "Compressed/coords.txt";
        int frame = 1, count = 0;
        int tilesXframe = (imageList.get(0).getSecond().getHeight() * imageList.get(0).getSecond().getWidth())
                / (nTiles * nTiles);
        try(BufferedWriter bf = new BufferedWriter(new FileWriter(name))) {
            bf.write(Integer.toString(gop) + " " + Integer.toString(nTiles) + "\n");
            for(Tile tile : this.tileList) {
                if(tile.getCoordX() != -1 && tile.getCoordY() != -1) {
                    bf.write(frame + " " + tile.getId() + " " + tile.getCoordX() + " " + tile.getCoordY() +"\n");
                }
                count++;

                if(count == tilesXframe) {
                    count = 0;
                    frame++;
                }

            }

            bf.flush();
            bf.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calcula el color mitjà d'una imatge.
     * @param image imatge a calcular el color mitjà.
     * @return color mitjà de la imatge.
     */
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

    /**
     * Elimina les tessel·les de la imatge, per fer-ho, pinta l'espai que
     * ocupen amb el color mitjà del frame.
     * @param tiles tessel·les a eliminar.
     * @param pFrame frame d'on s'han d'eliminar les tessel·les.
     * @return imatge amb els espais de les tessel·les ocupats pel color mitjà.
     */
    private BufferedImage setColorPFrame(ArrayList<Tile> tiles, BufferedImage pFrame) {
        BufferedImage result = pFrame;
        Color color = getAverageColor(pFrame);
        tiles.forEach((tile) -> {
            int imageX = tile.getX();
            int imageY = tile.getY();
            int x = tile.getCoordX();
            int y = tile.getCoordY();
            if (x != -1 && y != -1) {
                for(int xCoord = imageY; xCoord < (imageY+height); xCoord++) {
                    for (int yCoord = imageX; yCoord < (imageX+width); yCoord++) {
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
     * Guarda en un ZIP el fitxer de coordenades i les imatges codificades.
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