/**
 * Classe encarregada de gestionar la progress bar.
 */
public class progressBar {
    /**
     * Nombre de iteracions que s'han de realitzar.
     */
    private final int size;
    /**
     * Posició de la barra a modificar.
     */
    private int pos;
    /**
     * Barra.
     */
    private final char[] bar = "░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░".toCharArray();
    /**
     * Ratio per saber cada quant s'ha d'afegir un tros a la barra.
     */
    private final float ratio;
    /**
     * Variable per controlar quan s'ha d'afegir un tros a la barra.
     */
    private float last;

    /**
     * Constructor.
     * @param size Nombre de iteracions que s'han de realitzar.
     */
    public progressBar(int size) {
        this.size = size;
        this.pos = 0;
        this.ratio = (float) (size) / bar.length;
        this.last = 0;
    }

    /**
     * Funció per actualitzar la barra.
     * @param iteration Iteració que s'està duent a terme.
     */
    public void update(int iteration) {
        float mod = iteration % ratio;
        if (mod < last && pos < bar.length) {
            bar[pos] = '█';
            pos++;
        }
        last = mod;
        print((float) iteration/size);
    }

    /**
     * Funció per imprimir la barra.
     * @param percent percentatge realitzat.
     */
    private void print(float percent) {
        System.out.print(new String(bar) + " " + (int)(percent*100) + "%\r");
    }
}