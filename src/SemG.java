public class SemG extends Semaforo {
    private int MaxR;
    
    public SemG(int modo) {
    	super(modo);
    }

	public int getMaxR() {
		return MaxR;
	}

	public void setMaxR(int maxR) {
		MaxR = maxR;
	}
}
