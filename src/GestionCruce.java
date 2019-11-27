import java.util.Timer;
import java.util.TimerTask;

public class GestionCruce {
 Calle c11;
 Calle c12;
 Calle c21;
 Calle c22;
 int maxA; //Tiempo en amarillo
 int maxR; //Tiempo en rojo hasta que el contrario se pone en verde
	int rojo1=0; //tiempo de calle 1 en rojo
	int rojo2=0; //tiempo de calle 2 en rojo
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

public void CambioLuz(){
	if(c11.SC.getModo() == 1) {
		c11.SC.setModo(2);
		c12.SC.setModo(2);
		Thread.sleep(maxA);
		c11.SC.setModo(3);
		c12.SC.setModo(3);
		Thread.sleep(maxR);
		c21.SC.setModo(0);
		c22.SC.setModo(0);
	}
	else {
		c21.SC.setModo(2);
		c22.SC.setModo(2);
		Thread.sleep(maxA);
		c21.SC.setModo(3);
		c22.SC.setModo(3);
		Thread.sleep(maxR);
		c11.SC.setModo(0);
		c12.SC.setModo(0);
	}
	rojo1=0;
	rojo2=0;
}

public void LuzGiro(int pos){
	switch(pos){
		case 1:
			c11.SG.setModo(2);
			c12.SG.setModo(2);
			break;
		case 2:
			c21.SG.setModo(2);
			c22.SG.setModo(2);
			break;
	}
}

public void Algoritmo(){
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run()
		{
			switch(c11.SC.getModo()){
				case 1:
					if(c21.totalCoches()>16)
						CambioLuz();

			}
		}
	};
	// Empezamos al instante y luego lanzamos la tarea cada 1000ms, mirar cada cuanto se hace una lectura para coordinar el timer
	timer.schedule(task,0, 1000);
}
public void Timer() {
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run()
		{
			if (c11.SC.getModo()==1)
				rojo1++;
			else
				rojo2++;
		}
	};
	// Empezamos al instante y luego lanzamos la tarea cada 1000ms, mirar cada cuanto se hace una lectura para coordinar el timer
	timer.schedule(task,0, 1000);

}
}
