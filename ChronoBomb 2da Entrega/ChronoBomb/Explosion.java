import java.awt.Point;
import java.util.List;

/** Fuego que queda un instante en pantalla; mata a quien lo toque. */
public class Explosion {
    private final List<Point> celdas;
    private double tiempoRestante = Config.DURACION_EXPLOSION;

    public Explosion(List<Point> celdas) { this.celdas = celdas; }

    public void actualizar(double dt) { tiempoRestante -= dt; }
    public boolean haTerminado() { return tiempoRestante <= 0; }
    public List<Point> getCeldas() { return celdas; }

    public boolean contiene(int x, int y) {
        for (Point p : celdas) if (p.x == x && p.y == y) return true;
        return false;
    }
}
