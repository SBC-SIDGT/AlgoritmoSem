import java.util.Timer;

public class SemC extends Semaforo {
    int maxR;
    int maxV;

    public int getMaxV() {
        return maxV;
    }

    public void setMaxV(int maxVerde) {
        maxV = maxVerde;
    }

    public SemC(short modo) {
        super(modo);
    }

    public int getMaxR() {
        return maxR;
    }

    public void setMaxR(int maxRojo) {
        maxR = maxRojo;
    }

}
