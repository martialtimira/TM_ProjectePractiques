public class FPSCounter {

    private int counter;
    private long start_time;
    private long elapsed_time;
    public FPSCounter() {
        this.counter = 0;
    }

    public void increase_counter() {
        if(counter == 0) {
            this.start_time = System.currentTimeMillis();
        }
        this.counter++;
    }

    public double getFPS() {
        long l = this.counter / (System.currentTimeMillis() - start_time);
        return l;
    }

    public void printFPS() {
        double time = ((double)(System.currentTimeMillis() - this.start_time)) / 1000;
        System.out.println(this.counter + " frames in " + time + "s, FPS: " + (this.counter/time));
    }

    public int getCounter() {
        return this.counter;
    }

    public long getStart_time(){
        return this.start_time;
    }
}
