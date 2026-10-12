import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class MapeoTeclas implements KeyListener{
    public static boolean arriba;
    public static boolean abajo;
    public static boolean izquierda;
    public static boolean derecha;
    public boolean bomba;

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int codigo = e.getKeyCode();
        if(codigo == KeyEvent.VK_W){
            arriba = true;
        }
        if(codigo == KeyEvent.VK_A){
            izquierda = true;
        }
        if(codigo == KeyEvent.VK_S){
            abajo = true;
        }
        if(codigo == KeyEvent.VK_D){
            derecha = true;
        }
        if(codigo == KeyEvent.VK_F){
            bomba = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int codigo = e.getKeyCode();
        if(codigo == KeyEvent.VK_W){
            arriba = false;
        }
        if(codigo == KeyEvent.VK_A){
            izquierda = false;
        }
        if(codigo == KeyEvent.VK_S){
            abajo = false;
        }
        if(codigo == KeyEvent.VK_D){
            derecha = false;
        }
        if(codigo == KeyEvent.VK_F){
            bomba = false;
        }
    }
}
