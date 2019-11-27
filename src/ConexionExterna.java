import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class ConexionExterna {
	
	public HttpURLConnection abrirConexion()throws IOException {
		URL url = new URL("https://my-json-server.typicode.com/typicode/demo");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		
		return con;
	}
	/*
	 * Metodo que obtiene el JSON de la API
	 * https://www.oracle.com/technetwork/es/articles/java/api-java-para-json-2251318-esa.html
	 */
	public void getDatos (HttpURLConnection con) throws IOException {
		con.setRequestMethod("GET");
		con.setRequestProperty("Content-Type", "application/json");
		BufferedReader in = new BufferedReader(
				  new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			content.append(inputLine);
		}
		in.close();
		System.out.println(in);
	}
	
}
