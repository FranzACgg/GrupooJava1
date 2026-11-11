import java.util.List;

/**
 * Se encarga SOLO de mover jugadores sin atravesar paredes, bloques ni bombas.
 * (Se separo del GameLoop para que cada clase haga una sola cosa.)
 */
public class Movimiento {
    private static final double EPSILON = 1e-6;   // margen chiquito para no quedar "metido" en la pared

    private final Mapa mapa;
    private final List<Bomba> bombas;

    public Movimiento(Mapa mapa, List<Bomba> bombas) {
        this.mapa = mapa;
        this.bombas = bombas;
    }

    /**
     * Mueve al jugador (desplazamiento en casilleros). Primero el eje X y despues el eje Y,
     * asi si choca de costado puede seguir deslizandose por la pared.
     * Un desplazamiento por frame es siempre menor a 1 casillero, asi que no puede "saltar" una pared.
     */
    public void mover(Jugador j, double desplazX, double desplazY) {
        boolean soloUnEje = (desplazX == 0) != (desplazY == 0);
        moverEje(j, desplazX, 0, soloUnEje);
        moverEje(j, 0, desplazY, soloUnEje);
    }

    /** Un movimiento en UN solo eje. Si choca, se pega a la pared (sin dejar huecos). */
    private void moverEje(Jugador j, double dx, double dy, boolean asistir) {
        if (dx == 0 && dy == 0) return;
        double h = Config.MEDIO_HITBOX;
        double nx = j.getPosX() + dx;
        double ny = j.getPosY() + dy;

        if (!colisiona(j, nx, ny)) {
            j.moverJugador(nx, ny);
            return;
        }

        // Choco: acomodar el borde del jugador justo contra el casillero bloqueado
        if (dx > 0) nx = Math.floor(nx + h) - h - EPSILON;
        if (dx < 0) nx = Math.floor(nx - h) + 1 + h + EPSILON;
        if (dy > 0) ny = Math.floor(ny + h) - h - EPSILON;
        if (dy < 0) ny = Math.floor(ny - h) + 1 + h + EPSILON;
        if (!colisiona(j, nx, ny)) j.moverJugador(nx, ny);

        if (asistir) deslizarHaciaPasillo(j, dx, dy);
    }

    /**
     * Ayuda de esquina: si el jugador va casi alineado con un pasillo pero roza la esquina
     * de un bloque, lo empuja suavemente al centro del pasillo para que pueda doblar.
     */
    private void deslizarHaciaPasillo(Jugador j, double dx, double dy) {
        if (dx != 0) {
            // iba en horizontal: lo alineamos en vertical con el centro de su fila
            double centroY = Math.floor(j.getPosY()) + 0.5;
            double adelanteX = j.getPosX() + Math.signum(dx) * 0.1;
            if (colisiona(j, adelanteX, centroY)) return;        // el pasillo esta tapado
            double nuevoY = acercar(j.getPosY(), centroY, Math.abs(dx));
            if (!colisiona(j, j.getPosX(), nuevoY)) j.moverJugador(j.getPosX(), nuevoY);
        } else {
            // iba en vertical: lo alineamos en horizontal con el centro de su columna
            double centroX = Math.floor(j.getPosX()) + 0.5;
            double adelanteY = j.getPosY() + Math.signum(dy) * 0.1;
            if (colisiona(j, centroX, adelanteY)) return;
            double nuevoX = acercar(j.getPosX(), centroX, Math.abs(dy));
            if (!colisiona(j, nuevoX, j.getPosY())) j.moverJugador(nuevoX, j.getPosY());
        }
    }

    /** Se acerca de "actual" hacia "objetivo" sin avanzar mas que "maximo". */
    private double acercar(double actual, double objetivo, double maximo) {
        double diferencia = objetivo - actual;
        if (Math.abs(diferencia) <= maximo) return objetivo;
        return actual + Math.signum(diferencia) * maximo;
    }

    /** true si un jugador con el centro en (cx, cy) pisaria algo solido. */
    private boolean colisiona(Jugador j, double cx, double cy) {
        double h = Config.MEDIO_HITBOX;
        int desdeX = (int) Math.floor(cx - h), hastaX = (int) Math.floor(cx + h);
        int desdeY = (int) Math.floor(cy - h), hastaY = (int) Math.floor(cy + h);

        // Revisa todos los casilleros que toca el cuerpo del jugador
        for (int y = desdeY; y <= hastaY; y++) {
            for (int x = desdeX; x <= hastaX; x++) {
                if (!mapa.esTransitable(x, y)) return true;
                Bomba b = bombaEn(x, y);
                if (b != null && !b.puedePasar(j)) return true;
            }
        }
        return false;
    }

    private Bomba bombaEn(int x, int y) {
        for (Bomba b : bombas)
            if (b.getPosX() == x && b.getPosY() == y) return b;
        return null;
    }
}
