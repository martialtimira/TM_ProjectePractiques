import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Arrays;


public class AverageFilterApplier {

    private float[] kernel;
    private Kernel convolve_kernel;
    public AverageFilterApplier() {

        kernel =  new float[3 * 3];
        Arrays.fill(kernel, 1.0f / (3 * 3));
        convolve_kernel = new Kernel(3, 3, kernel);
    }

    public AverageFilterApplier(int kernel_size) {

        kernel =  new float[kernel_size * kernel_size];
        Arrays.fill(kernel, 1.0f / (kernel_size * kernel_size));
        convolve_kernel = new Kernel(kernel_size, kernel_size, kernel);
    }

    public BufferedImage applyAverageFilter(BufferedImage image) {
        BufferedImageOp op = new ConvolveOp(convolve_kernel);
        BufferedImage filtered_image = op.filter(image, null);

        return filtered_image;
    }

}
