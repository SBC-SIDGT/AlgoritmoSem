import java.util.Timer;

public class SemC extends Semaforo{
    int maxR;
    int maxV;
    Timer timer;


    public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
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
