import com.beust.jcommander.JCommander;
import paramManager.MainCLIParameters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


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

    private AverageFilterApplier average_filter_applier;

    /**
     * Variable global per tenir els fps dins el thread.
     */
    private int fps;

    /**
     * Variable global per poder tenir el zip dins del thread.
     */
    private ZipInputStream input_stream;

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
        if(fps == 0) {
            fps = 24;
        }


        Timer timer = new Timer();

        // Thread individual per executar el video i així tenir els fps controlats.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int avg_value = mainArgs.getAveraging_value();
                ZipEntry entry;
                Path file_path = mainArgs.getInputPath();
                if(input_stream == null) {
                    try {
                        input_stream = new ZipInputStream(new FileInputStream(file_path.toString()));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    entry = input_stream.getNextEntry();
                    if(entry != null) {
                        if(!entry.isDirectory()) {
                            BufferedImage image = ImageIO.read(input_stream);
                            BufferedImage display_image = image;
                            if(avg_value != 0) {
                                if(average_filter_applier == null) {
                                    average_filter_applier = new AverageFilterApplier(avg_value);
                                }

                                display_image = average_filter_applier.applyAverageFilter(image);
                                //add code to save into a new ZIP?
                            }
                            if(visor == null) {
                                visor = new Visor(display_image);
                                visor.setVisible(true);
                                fpsCounter.increase_counter();
                            }
                            else {
                                visor.update_image(display_image);
                                fpsCounter.increase_counter();
                                if(fpsCounter.getCounter() % fps == 0) {
                                    fpsCounter.printFPS();
                                }
                            }
                        }
                    }
                    // Indiquem que hem acabat de llegir aquesta entrada
                    input_stream.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        timer.schedule(task, 0, 1000/fps);

    }
}