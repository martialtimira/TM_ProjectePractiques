import ImageClass.Tile;
import paramManager.MainCLIParameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Decoder {
    public ArrayList<Integer> ids;
    public ArrayList<Integer> xCoords;
    public ArrayList<Integer> yCoords;
    public ArrayList<BufferedImage> images;

    public int reproCounter;
    private final ArrayList<Pair> pairList;
    public int gop;
    public int tileWidth;
    public int tileHeight;
    public int nTiles;
    public int fps;

    public progressBar pb;
    public FPSCounter fpsCounter;
    public Visor visor;

    private final String outputPath;

    private String inputPath;

    private final boolean batch;
    private final boolean verbose;
    private final Utils utils;

    public Decoder(MainCLIParameters mainArgs, Visor visor) {
        this.outputPath = mainArgs.getOutputPath();
        this.inputPath = mainArgs.getInputPath().toString();
        if(mainArgs.getEncode() && mainArgs.getDecode()) {
            this.inputPath = this.outputPath;
        }
        this.fps = mainArgs.getFps();
        if (this.fps == 0) {
            this.fps = 24;
        }
        this.gop = mainArgs.getGOP();
        this.nTiles = mainArgs.getnTiles();
        this.ids = new ArrayList<>();
        this.xCoords = new ArrayList<>();
        this.yCoords = new ArrayList<>();
        this.images = new ArrayList<>();
        this.pairList = new ArrayList<>();
        this.utils = new Utils();
        this.visor = visor;
        this.reproCounter = 0;
        this.batch = mainArgs.hasWindow();
        this.verbose = mainArgs.isVerbose();
    }

    public ArrayList<Pair> decode() {
        System.out.println("DECODING");
        this.readZIP();
        //START DECODING THREAD
        class DecodeTask implements Runnable {
            final Decoder decoder;
            public DecodeTask(Decoder decoder) {this.decoder = decoder;}
            @Override
            public void run() {
                decoder.buildImages();

                new File("Decompressed").mkdirs();
                int counter = 0;
                for(BufferedImage image :  decoder.images) {
                    try {
                        String imageName = "frame" + counter + ".jpeg";
                        JPEGCompressor.compress(image, "Decompressed/", imageName);
                        decoder.pairList.add(new Pair(imageName, image));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    counter++;
                }
                decoder.utils.createZipFolder("Decompressed", decoder.outputPath);
                File outputFile = new File(decoder.outputPath);
                decoder.utils.deleteDirectory(new File("Decompressed"));
            }
        }
        Thread decodeThread = new Thread(new DecodeTask(this));
        decodeThread.start();
        //END DECODING THREAD
        //timertask to reproduce here
        if(!batch) {
            this.reproCounter = 0;
            this.pb = new progressBar(this.images.size());
            this.fpsCounter = new FPSCounter();
            class ReproTask extends TimerTask {
                final Decoder decoder;

                public ReproTask(Decoder decoder) { this.decoder = decoder;}

                @Override
                public void run() {
                    decoder.reproduceImages();
                }
            }

            Timer timer = new Timer();

            timer.schedule(new ReproTask(this), 2, 1000/this.fps);
        }

        try {
            decodeThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this.pairList;
    }

    public void reproduceImages() {
        if (reproCounter < this.images.size()) {
            BufferedImage image = this.images.get(reproCounter);
            if(this.visor == null) {
                visor = new Visor(image);
                visor.setVisible(true);
            }
            else {
                visor.update_image(image);
            }
            reproCounter++;
            if(pb != null) {
                pb.update(reproCounter);
            }
            if(fpsCounter != null) {
                fpsCounter.increase_counter();
            }
            if (verbose) {
                assert fpsCounter != null;
                if (fpsCounter.getCounter() % fps == 0) {
                    fpsCounter.printFPS();
                }
            }
        }
    }
    private void buildImages() {
        this.tileWidth = nTiles;
        this.tileHeight = nTiles;
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
        ArrayList<Tile> tiles = generateMacroBlocks(pFrame);
        int startingId = 0;
        int lastId = this.nTiles * this.nTiles;
        if (idMultiplier != 0) {
            startingId = idMultiplier;
            lastId += Math.min(idMultiplier, ids.size());
        }
        for(int i = startingId; i < lastId; i++) {
            //System.out.println("TILE N" + i);
            Tile tile = tiles.get(ids.get(i));
            int x = xCoords.get(i);
            int y = yCoords.get(i);
            int tileX = tile.getX();
            int tileY = tile.getY();
            if(x != -1 && y != -1) {
                //System.out.println("COORDS in Iframe: " + x + ", " + y);
                BufferedImage baseSubimage = iFrame.getSubimage(x, y, tileHeight, tileWidth);
                //System.out.println("Iframe subimage" + baseSubimage.getWidth() + ", " + baseSubimage.getHeight());
                //System.out.println("X: " + tileX + " Y: " + tileY);
                //System.out.println("PFRAME DIM: " + pFrame.getWidth()+ ", " + pFrame.getHeight());
                for(int j = 0; j < this.tileHeight; j++) {
                    for(int k = 0; k < this.tileWidth; k++) {
                        int rgb = baseSubimage.getRGB(k, j);
                        //int rgb = tile.getTile().getRGB(k, j);
                        //System.out.println("WRITING RGB ON " + (k+tileY) + ", "+ (j+tileX));
                        pFrame.setRGB(k + tileX, j+tileY, rgb);
                    }
                }
            }
        }
    }

    private ArrayList<Tile> generateMacroBlocks(BufferedImage image) {
        ArrayList<Tile> tiles = new ArrayList<>();
        Tile tile;
        int count = 0;
        for(int y = 0; y < image.getHeight() - this.tileHeight; y += this.tileHeight) {
            for(int x = 0; x < image.getWidth() - this.tileWidth; x += this.tileWidth) {
                tile = new Tile(image.getSubimage(x, y, this.tileWidth, this.tileHeight), count);
                tile.setX(x);
                tile.setY(y);
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
            ArrayList<Pair> tempImages =  new ArrayList<>();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if(name.equalsIgnoreCase("Compressed/coords.txt")) {
                    // TODO read this binary
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
                    tempImages.add(new Pair(name, image));
                }
            }
            Collections.sort(tempImages, new Comparator<Pair>() {
                @Override
                public int compare(Pair pair1, Pair pair2) {
                    return pair1.getFirst().compareTo(pair2.getFirst());
                }
            });

            for(Pair p: tempImages) {
                this.images.add((BufferedImage) p.getSecond());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}