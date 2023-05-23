import ImageClass.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class Decoder {
    public ArrayList<Integer> ids;
    public ArrayList<Integer> xCoords;
    public ArrayList<Integer> yCoords;
    public ArrayList<BufferedImage> images;

    private ArrayList<Pair> pairList;
    public int gop;
    public int tileWidth;
    public int tileHeight;
    public int nTiles;
    public int fps;
    private String outputPath;

    private String inputPath;

    private Utils utils;

    public Decoder(int fps, int gop, int nTiles, String outputPath, String inputPath) {
        this.outputPath = outputPath;
        this.inputPath = inputPath;
        this.fps = fps;
        this.gop = gop;
        this.nTiles = nTiles;
        this.ids = new ArrayList<>();
        this.xCoords = new ArrayList<>();
        this.yCoords = new ArrayList<>();
        this.images = new ArrayList<>();
        this.pairList = new ArrayList<>();
        this.utils = new Utils();
    }

    public ArrayList<Pair> decode() {
        System.out.println("DECODING");
        this.readZIP();
        //maybe run this buildImages on 1 thread and then make the timertask next here to reproduce it
        this.buildImages();
        //timertask to reproduce here
        new File("Decompressed").mkdirs();
        int counter = 0;
        for(BufferedImage image :  this.images) {
            try {
                String imageName = "frame" + counter + ".jpeg";
                JPEGCompressor.compress(image, "Decompressed/", imageName);
                this.pairList.add(new Pair(imageName, image));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            counter++;
        }
        this.utils.createZipFolder("Decompressed", this.outputPath);
        File outputFile = new File(this.outputPath);
        this.utils.deleteDirectory(new File("Decompressed"));
        return this.pairList;
    }

    private void buildImages() {
        this.tileWidth = this.images.get(0).getWidth() / nTiles;
        this.tileHeight = this.images.get(0).getHeight() / nTiles;
        BufferedImage iFrame = null;
        int idMultiplier = 0;
        for(int i = 0; i < this.images.size() - 1; i++) {
            BufferedImage currentFrame = this.images.get(i);
            if (i % this.gop == 0) {
                iFrame = currentFrame;
            } else if(i == this.images.size() - 2) {
                this.buildPframes(iFrame, currentFrame, idMultiplier);
                idMultiplier += this.nTiles * this.nTiles;
                this.buildPframes(iFrame, this.images.get(i+1), idMultiplier);
            } else {
                this.buildPframes(iFrame, currentFrame, idMultiplier);
                idMultiplier += this.nTiles * this.nTiles;
            }
        }
    }

    private void buildPframes(BufferedImage iFrame, BufferedImage pFrame, int idMultiplier) {
        ArrayList<Tile> tiles = generateMacroBlocks(iFrame);
        int startingId = 0;
        int lastId = this.nTiles * this.nTiles;
        if (idMultiplier != 0) {
            startingId = idMultiplier;
            lastId += Math.min(idMultiplier, ids.size());
        }
        for(int i = startingId; i < lastId; i++) {
            Tile tile = tiles.get(ids.get(i));
            int x = xCoords.get(i);
            int y = yCoords.get(i);
            if(x != -1 && y != -1) {
                for(int j = 0; j < this.tileHeight; j++) {
                    for(int k = 0; k < this.tileWidth; k++) {
                        int rgb = tile.getTile().getRGB(k, j);
                        //MAYBE WRONG
                        pFrame.setRGB(k + y, j+x, rgb);
                    }
                }
            }
        }
    }

    private ArrayList<Tile> generateMacroBlocks(BufferedImage image) {
        ArrayList<Tile> tiles = new ArrayList<>();
        Tile tile;
        int count = 0;
        for(int y = 0; y < image.getHeight(); y += this.tileHeight) {
            for(int x = 0; x < image.getWidth(); x += this.tileWidth) {
                tile = new Tile(image.getSubimage(x, y, this.tileWidth, this.tileHeight), count);
                tiles.add(tile);
                count++;
            }
        }
        return tiles;
    }

    private void readZIP() {
        try {
            File file = new File(inputPath);
            ZipFile zipFile = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if(name.equalsIgnoreCase("Compressed/coords.txt")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    String line;
                    while((line = reader.readLine()) != null) {
                        String[] lineElements = line.split(" ");
                        ids.add(Integer.parseInt(lineElements[0]));
                        xCoords.add(Integer.parseInt(lineElements[1]));
                        yCoords.add(Integer.parseInt(lineElements[2]));
                    }
                    reader.close();
                } else {
                    BufferedImage image = ImageIO.read(zipFile.getInputStream(entry));
                    this.images.add(image);
                }
            }
        } catch (ZipException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
