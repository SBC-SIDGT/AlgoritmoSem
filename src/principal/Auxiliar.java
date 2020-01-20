package principal;

import java.util.ArrayList;

public class Auxiliar {
	
	public ArrayList<Integer> generadorDatos(ArrayList<Integer> a){
		ArrayList<Integer> resul = new ArrayList<Integer>();
		for(int i = 0; i< a.size(); i++) {
			resul.add((int)  Math.floor(Math.random()*8));
		}
		System.out.println("Array random: "+resul);
		return resul;
		
	}
	public static ArrayList<Integer> generadorDatosTesting(){
		ArrayList<Integer> resul = new ArrayList<Integer>();
		for(int i = 0; i< 5; i++) {
			resul.add((int)  Math.floor(Math.random()*8));
		}
		System.out.println(resul);
		return resul;
		
	}
	
	public static ArrayList<Integer> obtenerIntegerArray(String[] stringArray) {
        ArrayList<Integer> resul = new ArrayList<Integer>();
        for(String stringValue : stringArray) {
            try {
            	System.out.println("valuee: "+ stringValue);
                //Convierte String a Integer y lo guarda en un Integer arrayList.              result.add(Integer.parseInt(stringValue));
            } catch(NumberFormatException nfe) {
               System.out.println("No pudo parsear " + nfe);
                
            } 
        }       
        return resul;
    }
}
