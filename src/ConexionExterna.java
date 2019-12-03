import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.parser.JSONParser;

public class ConexionExterna {
	
	public HttpURLConnection abrirConexion()throws IOException {
		URL url = new URL("https://my-json-server.typicode.com/typicode/demo/posts");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
		return con;
	}
	/*
	 * Metodo que obtiene el JSON de la API
	 * https://www.oracle.com/technetwork/es/articles/java/api-java-para-json-2251318-esa.html
	 */
	public void getDatos (HttpURLConnection con) throws IOException {
		URL url = new URL("https://my-json-server.typicode.com/typicode/demo/posts");
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");
		con.connect();
		String inline="";
		int responsecode = con.getResponseCode(); 
		if(responsecode != 200)
			System.out.println(responsecode);
			else
			{
				Scanner sc = new Scanner(url.openStream());
				while(sc.hasNext())
				{
					inline+=sc.nextLine();
				}
				System.out.println("\nJSON data in string format");
				System.out.println(inline);
				sc.close();
			}
	}
	
}
