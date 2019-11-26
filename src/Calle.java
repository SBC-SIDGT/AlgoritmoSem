public class Calle {
    int via[]; //Almacena el número de vehículos en cada via de la calle
    int Pos;

    public Calle(int[] via, int pos) {
        this.via = via;
        Pos = pos;
    }

    public int[] getVia() {
        return via;
    }

    public int getPos() {
        return Pos;
    }

    public void setVia(int[] via) {
        this.via = via;
    }

    public void setPos(int pos) {
        Pos = pos;
    }
}
