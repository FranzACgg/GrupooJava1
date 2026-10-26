import javax.swing.JPanel;
import java.awt.*;

public class PanelJuego extends JPanel implements Runnable{

    //Ajustes de Pantalla predeterminados, determina el tamaño de TO-DO jugador mapa etc.
    public static final int tamanoCasillas = 16;
    int reescalado = 4; //De ejemplo ponemos 4

    final int tamanoCasillasReescalado = tamanoCasillas * reescalado;
    final int columnasEnPantalla = 16;
    final int filasEnPantalla = 12;
    final int anchoPantalla = tamanoCasillasReescalado * columnasEnPantalla;
    final int altoPantalla = tamanoCasillasReescalado * filasEnPantalla;
    final int FPS = 60; //Cambia la velocidad

    MapeoTeclas MapeoTeclas = new MapeoTeclas();
    Jugador jugador1 = new Jugador(this, MapeoTeclas);
    Thread hiloJuego;

    public PanelJuego(int reescalado){
        this.reescalado = reescalado;
        this.setPreferredSize(new Dimension(anchoPantalla, altoPantalla));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(MapeoTeclas);
        this.setFocusable(true);
    }

    public void iniciarHilo(){
        hiloJuego = new Thread(this);
        hiloJuego.start();
    }

    public void run(){
        double intervalo = 1000000000/FPS;
        double delta = 0;
        long ultimaVez = System.nanoTime();
        long tiempoActual;
        while(hiloJuego != null){
            try{
                tiempoActual = System.nanoTime();
                delta += (tiempoActual - ultimaVez) / intervalo;
                ultimaVez = tiempoActual;
                if (delta >= 1){
                    update();
                    repaint();
                    delta--;
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void update(){
        jugador1.update();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        jugador1.draw(g2);
        g2.dispose();
    }

}
