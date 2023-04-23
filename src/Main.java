import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;


public class Main {

    public static void main(String[] args) throws IOException {

        String zipFilePath = "Cubo.zip";

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                // Només llegim les entrades que són fitxers
                if (!entry.isDirectory()) {

                    // Creem un objecte BufferedImage a partir de l'entrada ZIP
                    BufferedImage image = ImageIO.read(zis);

                    // Mostrem la imatge
                    Visor visor = new Visor(image);
                    visor.setVisible(true);

                }

                // Indiquem que hem acabat de llegir aquesta entrada
                zis.closeEntry();

            }

        }

    }

}
