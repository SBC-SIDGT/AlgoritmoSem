package principal;


public class SemC extends Semaforo {
    private int maxR;
    private int maxV;

    public int getMaxV() {
        return maxV;
    }

    public void setMaxV(int maxVerde) {
        maxV = maxVerde;
    }

    public SemC(int modo) {
        super(modo);
    }

    public int getMaxR() {
        return maxR;
    }

    public void setMaxR(int maxRojo) {
        maxR = maxRojo;
    }

}
