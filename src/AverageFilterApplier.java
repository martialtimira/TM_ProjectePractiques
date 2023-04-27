import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;


public class AverageFilterApplier {

    private float[] kernel;
    private Kernel convolve_kernel;

    /**
     * Constructor base de la classe, on crea un kernel de 3x3
     */
    public AverageFilterApplier() {

        kernel =  new float[3 * 3];
        Arrays.fill(kernel, 1.0f / (3 * 3));
        convolve_kernel = new Kernel(3, 3, kernel);
    }

    /**
     * Constructor amb paràmetres de la classe, on crea un kernel de tamany {@code kernel_size} x {@code kernel_size}
     * @param kernel_size tamany de la matriu mxm kernel
     */
    public AverageFilterApplier(int kernel_size) {

        kernel =  new float[kernel_size * kernel_size];
        Arrays.fill(kernel, 1.0f / (kernel_size * kernel_size));
        convolve_kernel = new Kernel(kernel_size, kernel_size, kernel);
    }

    /**
     * Aplica el filtre convolucional average a la {@code image} passada per paràmetres
     * @param image
     *        imatge a la que es vol aplicar el filtre
     * @return
     *        imatge resultant amb el filtre aplicat
     */
    public BufferedImage applyAverageFilter(BufferedImage image) {
        BufferedImageOp op = new ConvolveOp(convolve_kernel);
        BufferedImage filtered_image = op.filter(image, null);

        return filtered_image;
    }

}
