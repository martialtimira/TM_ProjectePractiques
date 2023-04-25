/**
 * Classe per mesurar la taxa de fotogrames per segon (FPS).
 */
public class FPSCounter {

    /**
     * Contador del nombre de fotogrames processats.
     */
    private int counter;

    /**
     * Temps en mil·lisegons en el que es va començar a processar.
     */
    private long start_time;

    /**
     * Temps transcorregut des de l'inici fins ara.
     */
    private long elapsed_time;

    /**
     * Constructor per defecte.
     */
    public FPSCounter() {
        this.counter = 0;
    }

    /**
     * Incrementa el contador de fotogrames processats.
     */
    public void increase_counter() {
        if(counter == 0) {
            this.start_time = System.currentTimeMillis();
        }
        this.counter++;
    }

    /**
     * Calcula la taxa de fotogrames per segon (FPS).
     * @return la taxa de fotogrames per segon (FPS).
     */
    public double getFPS() {
        long l = this.counter / (System.currentTimeMillis() - start_time);
        return l;
    }

    /**
     * Imprimeix per pantalla el nombre de fotogrames processats, el temps transcorregut i la taxa de fotogrames per segon (FPS).
     */
    public void printFPS() {
        double time = ((double)(System.currentTimeMillis() - this.start_time)) / 1000;
        System.out.println(this.counter + " frames in " + time + "s, FPS: " + (this.counter/time));
    }

    /**
     * Getter del contador de fotogrames.
     * @return el contador de fotogrames processats.
     */
    public int getCounter() {
        return this.counter;
    }

    /**
     * Getter del moment d'inici.
     * @return el temps en mil·lisegons en el que es va començar a processar.
     */
    public long getStart_time(){
        return this.start_time;
    }
}
