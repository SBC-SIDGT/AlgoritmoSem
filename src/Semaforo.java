

public class Semaforo {
    int modo; //Color del semaforo 1 verde, 2 amarillo, 3 rojo
    
    public Semaforo(int mod) {
        modo = mod;
    }

    public void setModo(int mod) {
        modo = mod;
    }

    public int getModo() {
        return modo;
    }
}
