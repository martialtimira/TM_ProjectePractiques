import ImageClass.Tile;
import com.beust.ah.A;
import paramManager.MainCLIParameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Decoder {
    public ArrayList<Integer> ids;
    public ArrayList<Integer> xCoords;
    public ArrayList<Integer> yCoords;
    public ArrayList<BufferedImage> images;

    public int reproCounter;

    private ArrayList<ArrayList> idsListList;
    private ArrayList<ArrayList> xCoordsListList;
    private ArrayList<ArrayList> yCoordsListList;
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

    /**
     * Constructor del decodificador
     * @param mainArgs Arguments d'entrada de l'execució del codi.
     * @param visor finestra on es mostrarà la imatge.
     */
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
        this.idsListList = new ArrayList<>();
        this.xCoordsListList = new ArrayList<>();
        this.yCoordsListList = new ArrayList<>();
    }

    /**
     * Funció per començar el procés de decodificar la imatge.
     * @return Llista amb totes les imatges descomprimides.
     */
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

    /**
     * Funció per reproduir les imatges dins del visor.
     */
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

    /**
     * Funció per descomprimir les imatges a partir de les dades que tenim del fitxer de 
     * i els frames de referència.
     */
    private void buildImages() {
        this.tileWidth = nTiles;
        this.tileHeight = nTiles;
        BufferedImage iFrame = null;
        int counter = 0;
        for(int i = 0; i < this.images.size(); i++) {
            BufferedImage currentFrame = this.images.get(i);
            if (i % this.gop == 0) {
                iFrame = currentFrame;
            } else {
                this.buildPframes(iFrame, 
                        currentFrame, 
                        this.idsListList.get(counter), 
                        this.xCoordsListList.get(counter), 
                        this.yCoordsListList.get(counter));
                counter++;
            }
        }

        System.out.println("FINAL COUNTER: " + counter);
    }

    /**
     * Funció per substituir les tessel·less buides del frame comprimit amb les equivalents del frame de referència.
     * @param iFrame frame que ha de ser construït.
     * @param pFrame frame de referència.
     * @param idList llista amb els identificadors de les tessel·les que han de ser substituïdes.
     * @param xCoordList llista amb les coordenades de l'eix X equivalents a les tessel·les al frame de referència.
     * @param yCoordList llista amb les coordenades de l'eix Y equivalents a les tessel·les al frame de referència.
     */
    private void buildPframes(BufferedImage iFrame, BufferedImage pFrame, ArrayList<Integer> idList, ArrayList<Integer> xCoordList, ArrayList<Integer> yCoordList) {
        ArrayList<Tile> tiles = generateMacroBlocks(pFrame);
        int tileID, x, y;
        for(int i = 0; i < idList.size(); i++) {
            //get tile, xcoords, and ycoords, and then do the same as the old method
            tileID = idList.get(i);
            x = xCoordList.get(i);
            y = yCoordList.get(i);
            Tile tile = tiles.get(tileID);
            for(int j = 0; j < this.tileHeight; j++) {
                for(int k = 0; k < this.tileWidth; k++) {
                    int rgb = iFrame.getRGB(y+j, x+k);
                    pFrame.setRGB((tile.getX()+k), (tile.getY()+j), rgb);
                }
            }
        }
    }

    /**
     * Divideix la imatge amb tessel·les.
     * @param image imatge a dividir.
     * @return llista dmb les tessel·les de la imatge.
     */
    private ArrayList<Tile> generateMacroBlocks(BufferedImage image) {
        ArrayList<Tile> tiles = new ArrayList<>();
        Tile tile;
        int count = 0;
        for(int y = 0; y < image.getHeight(); y += this.tileHeight) {
            for(int x = 0; x < image.getWidth(); x += this.tileWidth) {
                if(x+this.tileHeight <= image.getWidth() && y+this.tileWidth <= image.getHeight()) {
                    tile = new Tile(image.getSubimage(x, y, this.tileWidth, this.tileHeight), count);
                    tile.setX(x);
                    tile.setY(y);
                    tiles.add(tile);
                    count++;
                }
            }
        }
        return tiles;
    }

    /**
     * Llegeix el fitxer zip on es troben les imatges i el fitxer de coordenades.
     */
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
                    BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    String line;
                    line = reader.readLine();
                    String[] lineElements = line.split(" ");
                    this.gop = Integer.parseInt(lineElements[0]);
                    this.nTiles = Integer.parseInt(lineElements[1]);
                    System.out.println("GOP: " + this.gop + " nTiles: " + this.nTiles);
                    int currentFrame = 1;
                    int frameRead = 0;
                    while((line = reader.readLine()) != null) {
                        lineElements = line.split(" ");
                        frameRead = Integer.parseInt(lineElements[0]);
                        if (frameRead != currentFrame) {
                            idsListList.add(ids);
                            xCoordsListList.add(xCoords);
                            yCoordsListList.add(yCoords);
                            ids = new ArrayList<>();
                            xCoords = new ArrayList<>();
                            yCoords = new ArrayList<>();
                            currentFrame = frameRead;
                        }
                        ids.add(Integer.parseInt(lineElements[1]));
                        xCoords.add(Integer.parseInt(lineElements[2]));
                        yCoords.add(Integer.parseInt(lineElements[3]));
                    }
                    idsListList.add(ids);
                    xCoordsListList.add(xCoords);
                    yCoordsListList.add(yCoords);
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