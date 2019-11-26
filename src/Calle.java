public class Calle {
    ArrayList<int> vias = new ArrayList<int>(); //Almacena el número de vehículos en cada via de la calle
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
        for(int x=0;x<al.size();x++)
            result=al.get(x);
        return result;
    }
}
