import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/** Traduce el teclado a acciones de cada jugador (implementa la interfaz KeyListener). */
public class ControlTeclas implements KeyListener {
    public static final int ARRIBA = 0, ABAJO = 1, IZQUIERDA = 2, DERECHA = 3, BOMBA = 4;

    // Fila = jugador, columna = accion
    private static final int[][] TECLAS = {
        {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE},
        {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER},
        {KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_U},
        {KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD0}
    };

    private final Set<Integer> presionadas = new HashSet<>();
    private final Set<Integer> bombasPendientes = new HashSet<>();

    @Override
    public void keyPressed(KeyEvent e) {
        int codigo = e.getKeyCode();
        // add() devuelve false si ya estaba apretada: ignora la repeticion automatica del teclado
        if (presionadas.add(codigo)) {
            for (int j = 0; j < TECLAS.length; j++)
                if (TECLAS[j][BOMBA] == codigo) bombasPendientes.add(j);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) { presionadas.remove(e.getKeyCode()); }

    @Override
    public void keyTyped(KeyEvent e) { }

    public boolean estaPresionada(int jugador, int accion) {
        return presionadas.contains(TECLAS[jugador][accion]);
    }

    /** true una sola vez por cada pulsacion de la tecla de bomba. */
    public boolean consumirBomba(int jugador) {
        return bombasPendientes.remove(jugador);
    }

    public static String descripcion(int jugador) {
        String[] d = {"W A S D + ESPACIO", "Flechas + ENTER", "I J K L + U", "Numpad 8 4 5 6 + 0"};
        return d[jugador];
    }
}
