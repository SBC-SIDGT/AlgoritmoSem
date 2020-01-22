package testing;
import java.io.IOException;
import java.util.ArrayList;

import principal.*;

public class Run {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(3);
		arr.add(5);
		arr.add(5);
		arr.add(7);
		
		GestionCruce gc = new GestionCruce ();
		gc.iniciar();

	}

}
