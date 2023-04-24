import com.beust.jcommander.JCommander;
import paramManager.MainCLIParameters;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.*;
import javax.imageio.ImageIO;


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
        // De moment ho poso aqui pk corri
        // ideal-ment s'hauria de moure a una altra classe
        String zipFilePath = "Cubo.zip";
        Visor visor = null;
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                // Només llegim les entrades que són fitxers
                if (!entry.isDirectory()) {

                    // Creem un objecte BufferedImage a partir de l'entrada ZIP
                    BufferedImage image = ImageIO.read(zis);

                    // Mostrem la imatge. En cas de que sigui la 1era, inicialitzem Visor, si no, actualitzem la imatge
                    if (visor == null) {
                        visor = new Visor(image);
                        visor.setVisible(true);
                    }
                    else {
                        visor.update_image(image);
                    }

                }

                // Indiquem que hem acabat de llegir aquesta entrada
                zis.closeEntry();

            }

        }

    }
}