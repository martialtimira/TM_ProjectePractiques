import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class JPEGCompressor {

    /**
     * Compressor JPEG.
     * @param image imatge a comprimir.
     * @param name nom del directori.
     * @param outputIName nom de la imatge a ser guardada.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void compress(BufferedImage image, String name, String outputIName) throws FileNotFoundException, IOException {

        File compressedImageFile = new File(name + "/" + outputIName);
        OutputStream outputStream = new FileOutputStream(compressedImageFile);
        float imageQuality = 1.0f;
        BufferedImage bufferedImage = image;
        //Get image writers
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByFormatName("jpg");
        if (!imageWriters.hasNext()) {
            throw new IllegalStateException("No ImageWriter");
        }
        ImageWriter imageWriter = (ImageWriter) imageWriters.next();
        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);
        imageWriter.setOutput(imageOutputStream);
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        //Set the compress quality metrics
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(imageQuality);

        imageWriter.write(null, new IIOImage(bufferedImage, null, null), imageWriteParam);
        outputStream.close();
        imageOutputStream.close();
        imageWriter.dispose();
    }
}
