

public class Semaforo {
    short modo; //Color del semaforo 1 verde, 2 amarillo, 4 rojo, 3 apagado
    ConexionExterna
    
    public Semaforo(short mod) {
        modo = mod;
    }

    public void setModo(int mod) {
        modo = mod;
    }

    public short getModo() {
        return modo;
    }
}
