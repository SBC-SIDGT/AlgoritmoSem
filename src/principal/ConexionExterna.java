package principal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

public class ConexionExterna {
	
	private URL url;
	
	public URL getUrl() {
		return url;
	}
	public void setUrl() {
		try {
			this.url = new URL ("https://my-json-server.typicode.com/typicode/demo/posts");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("URL no valida");
			e.printStackTrace();
		}
	}
	
	public void abrirConexion()throws IOException {
		setUrl();
	}
	/*
	 * Metodo que obtiene los datos de la API. Recibe JSON, pero devuelve un ArrayList de enteros
	 */
	public ArrayList<Integer> getDatos () throws IOException {
		ArrayList<Integer> resul = new ArrayList<Integer>();
//		HttpURLConnection con = (HttpURLConnection) this.url.openConnection();
//		con.setRequestMethod("GET");
//		con.setRequestProperty("Content-Type", "application/json");
//		
//		BufferedReader in = new BufferedReader( 
//			new InputStreamReader(con.getInputStream())); 
//		String inputLine; 
//		StringBuffer content = new StringBuffer(); 
//		while ((inputLine = in.readLine()) != null) { 
//			content.append(inputLine); 
////			resul.add(inputLine);
//		} 
		//LINEA SIGUIENTE PARA PRUEBAS
		resul= Auxiliar.generadorDatosTesting();
//		in.close(); 
		System.out.println("hola: "+resul);
		return resul;
	}
	/*
	 * Metodo que devuelve datos a la API. Env�a un JSON (clave: signal, value: x)
	 */
	public boolean postDatos(int modo, int posicion) {
		boolean resul = false;
		final String POST_PARAMS = "{\n" + "\"modo\": "+modo+",\r\n" +
		        "    \"posicion\": "+posicion+ "\n}";
		try {
			System.out.println("url: " +  this.url);
			this.url = new URL ("https://my-json-server.typicode.com/typicode/demo/posts");
			HttpURLConnection con = (HttpURLConnection) this.url.openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setDoOutput(true);
			OutputStream os = con.getOutputStream();
			os.write(POST_PARAMS.getBytes()); 
			os.flush();
			os.close();
			int responseCode = con.getResponseCode();
			System.out.println("POST Response Code :  " + responseCode);
			System.out.println("POST Response Message : " + con.getResponseMessage());
			if (responseCode == HttpURLConnection.HTTP_CREATED) { //success
				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in .readLine()) != null) {
					response.append(inputLine);
				} in .close();
				// print result
				System.out.println(response.toString());
			} else {
				System.out.println("POST NOT WORKED");
			}	
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resul;
	}
	
}
