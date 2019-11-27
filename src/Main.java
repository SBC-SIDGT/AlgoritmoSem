import java.io.IOException;
import java.net.HttpURLConnection;

public class Main {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ConexionExterna conexionExt = new ConexionExterna();
		HttpURLConnection con = conexionExt.abrirConexion();
		conexionExt.getDatos(con);
	}

}
