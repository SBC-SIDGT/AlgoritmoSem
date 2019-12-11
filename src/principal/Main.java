package principal;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ConexionExterna conexionExt = new ConexionExterna();
		conexionExt.abrirConexion();
		conexionExt.postDatos(2, 4);
		//gestionCruce()
//		GestionCruce gc = new GestionCruce ();
//		gc.CambioLuz();
//		conexionExt.postDatos(6);
//		conexionExt.getDatos();
		Auxiliar aux = new Auxiliar();
		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(2);
		arr.add(2);
		arr.add(3);
		arr.add(3);
		arr.add(3);
		aux.generadorDatos(arr);
	}
}
