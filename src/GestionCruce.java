import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GestionCruce {
 private Calle c11;
	private Calle c12;
	private Calle c21;
	private Calle c22;
	private int maxA; //Tiempo en amarillo
	private int maxR; //Tiempo en rojo hasta que el contrario se pone en verde
	private int rojo1=0; //tiempo de calle 1 en rojo
	private int rojo2=0; //tiempo de calle 2 en rojo
	private ConexionExterna conexionExt = new ConexionExterna();
public Calle getC11() {
	return c11;
}
public void setC11(Calle c11) {
	this.c11 = c11;
}
public Calle getC12() {
	return c12;
}
public void setC12(Calle c12) {
	this.c12 = c12;
}
public Calle getC21() {
	return c21;
}
public void setC21(Calle c21) {
	this.c21 = c21;
}
public Calle getC22() {
	return c22;
}
public void setC22(Calle c22) {
	this.c22 = c22;
}
public int getMaxA() {
	return maxA;
}
public void setMaxA(int maxA) {
	this.maxA = maxA;
}

public GestionCruce(){
	c11 = new Calle(1);
	c12 = new Calle(2);
	c21 = new Calle(3);
	c22 = new Calle(4);
	Algoritmo();
	Timer();
}

public static void esperar(int segundos){
		try {
			Thread.sleep(segundos * 1000);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void CambioLuz(){
	if(c11.SC.getModo() == 1) {
		c11.SC.setModo(2);
		conexionExt.postDatos(2);
		c12.SC.setModo(2);
		esperar(maxA);
		c11.SC.setModo(4);
		conexionExt.postDatos(4);
		c12.SC.setModo(4);
		esperar(maxR);
		c21.SC.setModo(1);
		c22.SC.setModo(1);
	}
	else {
		c21.SC.setModo(2);
		c22.SC.setModo(2);
		esperar(maxA);
		c21.SC.setModo(4);
		c22.SC.setModo(4);
		esperar(maxR);
		c11.SC.setModo(1);
		conexionExt.postDatos(1);
		c12.SC.setModo(1);
	}
	rojo1=0;
	rojo2=0;
}

public void LuzGiro(int pos){
	switch(pos){
		case 1:
			c11.SG.setModo(2);
			conexionExt.postDatos(2);
			c12.SG.setModo(2);
			break;
		case 2:
			c21.SG.setModo(2);
			c22.SG.setModo(2);
			break;
	}
}
	public void LuzGiroOff(int pos){
		switch(pos){
			case 1:
				c11.SG.setModo(4);
				conexionExt.postDatos(4);
				c12.SG.setModo(4);
				break;
			case 2:
				c21.SG.setModo(4);
				c22.SG.setModo(4);
				break;
		}
	}

	public void Algoritmo() {
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Calles();
				switch (c11.SC.getModo()) {
					case 1:
						if (c21.Via(0) > 4 || c22.Via(0) > 4 || (c22.Via(0) + c21.Via(0)) > 8)
							LuzGiro(2);
						else if(c21.Via(0) < 4 || c22.Via(0) < 4 || (c22.Via(0) + c21.Via(0)) < 8)
							LuzGiroOff(2);
						if (rojo2>120)
							CambioLuz();
						else if((c21.totalCoches() + c22.totalCoches()) > 32 || c21.totalCoches() > 16 || c22.totalCoches() > 16)
							CambioLuz();
						break;
					case 4:
						if (rojo1>120)
							LuzGiro(1);
						else if(c11.Via(0) > 4 || c12.Via(0) > 4 || (c12.Via(0) + c11.Via(0)) > 8)
							CambioLuz();
						else if (c11.Via(0) < 4 || c12.Via(0) < 4 || (c12.Via(0) + c11.Via(0)) < 8)
							LuzGiroOff(1);
						if ((c11.totalCoches() + c22.totalCoches()) > 32 || c11.totalCoches() > 16 || c12.totalCoches() > 16)
							CambioLuz();
							break;
				}
			}
		};
			// Empezamos al instante y luego lanzamos la tarea cada 1000ms, mirar cada cuanto se hace una lectura para coordinar el timer
				timer.schedule(task,0,1000);
	}

		public void Calles() {
					ArrayList<Integer> street = new ArrayList<Integer>();
					try {
						street = conexionExt.getDatos();
					} catch (IOException e) {
						e.printStackTrace();
					}
					c11.setVias(street);
		}

		public void Timer() {
			Timer timer = new Timer();
			TimerTask task = new TimerTask() {
				@Override
				public void run() {

					if (c11.SC.getModo() == 1)
						rojo1++;
					else
						rojo2++;
				}
			};
			// Empezamos al instante y luego lanzamos la tarea cada 1000ms, mirar cada cuanto se hace una lectura para coordinar el timer
			timer.schedule(task, 0, 1000);

		}
	}
