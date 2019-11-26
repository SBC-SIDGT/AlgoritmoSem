import java.util.ArrayList;

public class Calle {
    ArrayList<Integer> vias = new ArrayList<Integer>; //Almacena el número de vehículos en cada via de la calle
    int Pos;

    public Calle(ArrayList<int> vias, int pos) {
        this.vias = vias;
        Pos = pos;
    }

    public ArrayList<int> getVias() {
        return vias;
    }

    public void setVias(ArrayList<int> vias) {
        this.vias = vias;
    }

    public int getPos() {
        return Pos;
    }

    public void setPos(int pos) {
        Pos = pos;
    }

    public int totalCoches(){
        int result=0;
        for (int i = 0; i < vias.size(); i++) {
            result += vias.get(i)
        }
        return result;
    }
}
