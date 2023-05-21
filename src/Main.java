import com.beust.jcommander.JCommander;
import paramManager.MainCLIParameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * Aquesta classe és l'entrada principal del programa. S'encarrega de gestionar els arguments d'entrada, parsejar-los i
 * executar el programa principal.
 */
public class Main {

    /**
     * Instància dels paràmetres d'entrada del terminal.
     */
    final MainCLIParameters mainArgs = new MainCLIParameters();

    /**
     * Finestra on es reprodueix el video.
     */
    private Visor visor;

    /**
     * Variable per saber els fps reals.
     */
    private FPSCounter fpsCounter;

    /**
     * Classe que aplica el filtre average
     */
    private AverageFilterApplier average_filter_applier;

    /**
     * Classe que controla la progressbar.
     */
    private progressBar pb;

    /**
     * Nombre de fitxers que s'han operat.
     */
    private int fileCounter;

    /**
     * String amb el nom del output file
     */
    private String outputName;

    /**
     * Contador per saber el numero de frames a processar
     */
    private int numFiles;

    /**
     * Contador de frames processats
     */
    private int processed_frame_counter;

    /**
     * Llista de fitxers a comprimir a l'output zip
     */
    private ArrayList<Pair> image_list;

    /**
     * Variable global per tenir els fps dins el thread.
     */
    private int fps;

    /**
     * Variable global per saber si s'ha de fer encode o no
     */
    private boolean encode;

    /**
     * Variable global per saber si s'ha de fer decode o no
     */
    private boolean decode;

    /**
     * Variable global per saber el numero de tessel·les en el que dividir els frames
     */
    private int ntiles;

    /**
     * Variable global que conté el seekRange
     */
    private int seekRange;

    /**
     * Variable global que conté el GOP
     */
    private int gop;

    /**
     * Variable global que conté la qualitat
     */
    private int quality;

    /**
     * Variable global per poder tenir el zip dins del thread.
     */
    private ZipInputStream input_stream;

    /**
     * Variable global per poder tenir el zip dins del thread.
     */
    private ZipOutputStream zip_output_stream;

    /**
     * Mètode principal del programa. Crea una instància de la classe Main, gestiona els arguments d'entrada,
     * executa el programa principal.
     *
     * @param args
     *        Arguments d'entrada del terminal.
     * @throws IOException
     *         Si hi ha hagut algun error durant l'execució.
     */
    public static void main(String[] args) throws IOException {
        Main app = new Main();
        app.handleInputArgs(args);
        app.run();
    }

    /**
     * Gestiona els arguments d'entrada i els assigna a les propietats dels paràmetres d'entrada. Si hi ha hagut algun
     * error durant la gestió, es mostra l'ús correcte dels arguments i es finalitza el programa.
     * @param args
     *        Arguments d'entrada del terminal.
     */
    void handleInputArgs(String[] args) {
        JCommander jCommander = new JCommander(mainArgs);
        jCommander.setProgramName("Video Optimizer");

        try {
            jCommander.parse(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            showUsage(jCommander);
        }

        if (mainArgs.isHelp()) {
            showUsage(jCommander);
        }
    }

    /**
     * Mostra l'ús correcte dels arguments i finalitza el programa.
     * @param jCommander
     *        Instància de JCommander que conté els arguments d'entrada.
     */
    void showUsage(JCommander jCommander) {
        jCommander.usage();
        System.exit(0);
    }

    /**
     * On comença l'execució de la funcionalitat principal del programa, un còdec de video.
     * @throws IOException
     *         Lectura de fitxers TODO definir i actualitzar això
     */
    void run() throws IOException {
        visor = null;
        fpsCounter = new FPSCounter();
        fps = mainArgs.getFps();
        image_list = new ArrayList<>();
        encode = mainArgs.getEncode();
        decode = mainArgs.getDecode();
        if(encode) {
            gop = mainArgs.getGOP();
            ntiles = mainArgs.getnTiles();
            seekRange = mainArgs.getSeekRange();
            quality = mainArgs.getQuality();
            System.out.println("Encode: " + encode);
            System.out.println("ntiles: " + ntiles);
            System.out.println("SeekRange: " + seekRange);
            System.out.println("quality: " + quality);
        }
        System.out.println("ENCODE: " + encode);
        if(fps == 0) {
            fps = 24;
        }
        //Primer, mirem quants frames haurem de reproduir, per saber quan hem reproduit l'ultim.
        ZipInputStream zis = new ZipInputStream(new FileInputStream(mainArgs.getInputPath().toString()));
        ZipEntry entry;
        numFiles = 0;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                numFiles++;
            }
        }
        zis.close();

        pb = new progressBar(numFiles);
        fileCounter = 0;

        Timer timer = new Timer();

        // Thread individual per executar el video i així tenir els fps controlats.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ZipEntry entry;

                int avg_value = mainArgs.getAveraging_value();
                outputName = mainArgs.getOutputPath();
                Path file_path = mainArgs.getInputPath();
                boolean verbose = mainArgs.isVerbose();

                if(input_stream == null) {
                    try {
                        input_stream = new ZipInputStream(new FileInputStream(file_path.toString()));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    fileCounter++;
                    entry = input_stream.getNextEntry();
                    if(entry != null) {
                        if(!entry.isDirectory()) {
                            BufferedImage image = ImageIO.read(input_stream);
                            BufferedImage display_image = image;

                            //Comprovem que l'usuari hagi definit un valor a avg_value per paràmetres, en cas afirmatiu, apliquem el filtre convolucional
                            if(avg_value != 0) {
                                if(average_filter_applier == null) {
                                    average_filter_applier = new AverageFilterApplier(avg_value);
                                }

                                display_image = average_filter_applier.applyAverageFilter(image);
                            }

                            // Aplicar el filtre negatiu.
                            if(mainArgs.applyNegative()) {
                                Negative.applyNegativeFilter(display_image);
                            }

                            // Actualitzar o iniciar el visor de video.
                            if(!mainArgs.hasWindow()) {
                                if (visor == null) {
                                    visor = new Visor(display_image);
                                    visor.setVisible(true);
                                    fpsCounter.increase_counter();
                                } else {
                                    visor.update_image(display_image);
                                    fpsCounter.increase_counter();
                                    if (verbose && fpsCounter.getCounter() % fps == 0) {
                                        fpsCounter.printFPS();
                                    }
                                }
                            }

                            //En cas de que s'hagi introduït un output file per paràmetres, es guarda el frame a la llista de imatges.
                            if(outputName != null) {
                                Pair pair = new Pair(entry.getName(), display_image);
                                image_list.add(pair);
                            }
                        }
                    } else {
                        return;
                    }
                    // Indiquem que hem acabat de llegir aquesta entrada
                    input_stream.closeEntry();
                    if (zip_output_stream != null) {
                        zip_output_stream.close();
                        zip_output_stream = null;
                    }

                    // Actualitzar la progress bar.
                    pb.update(fileCounter);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                processed_frame_counter++;

                // Guarder les imatges filtrades en un zip (desactivar si es fa encoding, ja que la classe encoder ho guarda ella.)
                if(processed_frame_counter == numFiles && outputName != null) {
                    if(encode) {
                        System.out.println("ENCODING");
                        Codifier codifier = new Codifier(image_list, gop, ntiles, seekRange, quality, outputName);
                        codifier.encode();
                    }
                    else {
                        zip_files();
                    }
                }
            }
        };

        timer.schedule(task, 0, 1000/fps);

    }

    /**
     * Mètode per comprimir tots els frames de image_list a un zip que tingui el nom introduït per paràmetres.
     */
    public void zip_files() {
        File output_file = new File(mainArgs.getOutputPath());
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(output_file));
            zip_output_stream = new ZipOutputStream(bos);

            for(Pair pair: image_list) {
                String file_name = (String) pair.getFirst();
                File image_file = new File(file_name);
                BufferedImage display_image = (BufferedImage) pair.getSecond();
                ImageIO.write(display_image, "png", image_file);
                zip_output_stream.putNextEntry(new ZipEntry(file_name));
                Files.copy(image_file.toPath(), zip_output_stream);
                zip_output_stream.closeEntry();
                image_file.delete();
            }
            zip_output_stream.close();
            System.out.println("ZIP Generated");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

