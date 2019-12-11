import java.util.ArrayList;

public class Auxiliar {
	
	public ArrayList<Integer> generadorDatos(ArrayList<Integer> a){
		ArrayList<Integer> resul = new ArrayList<Integer>();
		for(int i = 0; i< a.size(); i++) {
			resul.add((int)  Math.floor(Math.random()*8));
		}
		System.out.println(resul);
		return resul;
		
	}
}
