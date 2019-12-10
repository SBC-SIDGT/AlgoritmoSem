import java.util.ArrayList;

public class Calle {
    ArrayList<Integer> vias = new ArrayList<Integer>(); //Almacena el número de vehículos en cada via de la calle
    private int Pos;
    protected SemC SC;
    protected SemG SG;
    public Calle(ArrayList<Integer> vias, int pos) {
        this.vias = vias;
        Pos = pos;
        SC= new SemC(2);
        SG= new SemG(2);
    }

    public ArrayList<Integer> getVias() {
        return vias;
    }

    public void setVias(ArrayList<Integer> vias) {
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
            result += vias.get(i);
        }
        return result;
    }
    public void reset(){
        vias.clear();
    }
    public void add(int x){
        vias.add(x);
    }
    public int Via(int x){
        return vias.get(x);
    }
}
