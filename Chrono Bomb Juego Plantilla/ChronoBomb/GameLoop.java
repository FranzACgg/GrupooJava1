import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.Timer;

/**
 * Corazon del juego. Cada "tick" (60 por segundo) llama a actualizar(dt),
 * que hace siempre lo mismo y en este orden:
 *   1) timers  2) input y movimiento  3) golpes aturdidores
 *   4) bombas  5) explosiones y muertes  6) corona / victoria
 */
public class GameLoop implements ActionListener {
    private static final Color[] COLORES = {
        new Color(230, 50, 50), new Color(50, 110, 240), new Color(40, 190, 80), new Color(240, 200, 30)
    };

    private final Mapa mapa = new Mapa(new Random());
    private final Corona corona = new Corona();
    private final Jugador[] jugadores;
    private final List<Bomba> bombas = new ArrayList<>();
    private final List<Explosion> explosiones = new ArrayList<>();
    private final ControlTeclas controles;
    private final Timer timer;
    private final Runnable alRedibujar;

    private static final double PASO_MAXIMO = 0.2;     // casilleros por sub-paso de movimiento
    private static final double EPSILON = 1e-6;

    private boolean enEjecucion = false;
    private Jugador ganador = null;
    private long ultimoTick;

    public GameLoop(int cantJugadores, ControlTeclas controles, Runnable alRedibujar) {
        this.controles = controles;
        this.alRedibujar = alRedibujar;
        this.jugadores = inicializarJugadores(cantJugadores);
        this.timer = new Timer(1000 / Config.FPS, this);
    }


    private Jugador[] inicializarJugadores(int cant) {
        int ultimoX = Config.COLUMNAS - 2, ultimoY = Config.FILAS - 2;
        int[][] esquinas = {{1, 1}, {ultimoX, 1}, {1, ultimoY}, {ultimoX, ultimoY}};
        Jugador[] lista = new Jugador[cant];
        for (int i = 0; i < cant; i++)
            lista[i] = new Jugador(i, COLORES[i], esquinas[i][0], esquinas[i][1]);
        return lista;
    }

    public void iniciar() {
        enEjecucion = true;
        ultimoTick = System.nanoTime();
        timer.start();
    }

    public void detener() {
        enEjecucion = false;
        timer.stop();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long ahora = System.nanoTime();
        double dt = Math.min((ahora - ultimoTick) / 1e9, 0.05);   // tope por si se traba la ventana
        ultimoTick = ahora;
        if (enEjecucion) actualizar(dt);
        alRedibujar.run();
    }

    // ================== Un frame de juego ==================
    private void actualizar(double dt) {
        for (Jugador j : jugadores) {
            j.actualizarTemporizadores(dt);
            if (j.listoParaReaparecer()) reaparecer(j);
            procesarEntrada(j, dt);
            recogerObjetos(j);
        }
        aplicarGolpesAturdidores();
        actualizarBombas(dt);
        actualizarExplosiones(dt);
        actualizarVictoria();
    }

    private void reaparecer(Jugador j) {
        j.reiniciarJugador();
        permitirPasoSiSuperpone(j);   // si reaparece sobre una bomba, que pueda salir
    }

    // ---------- 2) Input y movimiento ----------
    private void procesarEntrada(Jugador j, double dt) {
        int i = j.getIndice();
        boolean pidioBomba = controles.consumirBomba(i);   // consumir siempre, aunque este muerto
        if (!j.estaVivo()) return;

        if (pidioBomba && !hayBombaEn(j.getCasilleroX(), j.getCasilleroY())) {
            Bomba b = j.plantarBomba();
            if (b != null) {
                // todos los que estan encima en este momento pueden salir caminando
                for (Jugador otro : jugadores)
                    if (otro.estaVivo() && otro.tocaCasillero(b.getPosX(), b.getPosY())) b.permitirPaso(otro);
                bombas.add(b);
            }
        }

        if (!j.puedeMoverse()) return;

        // Direccion como VECTOR: cada eje suma o resta; teclas opuestas se cancelan
        double dx = 0, dy = 0;
        if (controles.estaPresionada(i, ControlTeclas.ARRIBA)) dy -= 1;
        if (controles.estaPresionada(i, ControlTeclas.ABAJO)) dy += 1;
        if (controles.estaPresionada(i, ControlTeclas.IZQUIERDA)) dx -= 1;
        if (controles.estaPresionada(i, ControlTeclas.DERECHA)) dx += 1;
        if (dx == 0 && dy == 0) return;

        // En diagonal se normaliza: si no, iria 41% mas rapido que en linea recta
        double largo = Math.hypot(dx, dy);
        double velocidad = j.getVelocidad();
        moverConColision(j, dx / largo * velocidad * dt, dy / largo * velocidad * dt);
    }

    /**
     * Mueve al jugador (desplazamiento en casilleros) sin atravesar paredes, bloques ni bombas.
     * Se avanza en pasos cortos para que a mucha velocidad nunca "salte" una pared.
     */
    private void moverConColision(Jugador j, double desplazX, double desplazY) {
        double mayor = Math.max(Math.abs(desplazX), Math.abs(desplazY));
        int pasos = (int) Math.ceil(mayor / PASO_MAXIMO);
        boolean soloUnEje = (desplazX == 0) != (desplazY == 0);

        for (int k = 0; k < pasos; k++) {
            moverEje(j, desplazX / pasos, 0, soloUnEje);
            moverEje(j, 0, desplazY / pasos, soloUnEje);
        }
    }

    /** Un paso en UN solo eje. Si choca, se pega a la pared (sin dejar huecos) y puede deslizar. */
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
        boolean horizontal = dx != 0;
        double actual = horizontal ? j.getPosY() : j.getPosX();
        double centro = Math.floor(actual) + 0.5;
        double desvio = centro - actual;
        if (Math.abs(desvio) < EPSILON) return;

        // el pasillo alineado tiene que estar libre un poquito mas adelante
        double adelanteX = horizontal ? j.getPosX() + Math.signum(dx) * 0.1 : centro;
        double adelanteY = horizontal ? centro : j.getPosY() + Math.signum(dy) * 0.1;
        if (colisiona(j, adelanteX, adelanteY)) return;

        double empuje = Math.min(Math.abs(desvio), Math.abs(horizontal ? dx : dy)) * Math.signum(desvio);
        double nx = horizontal ? j.getPosX() : j.getPosX() + empuje;
        double ny = horizontal ? j.getPosY() + empuje : j.getPosY();
        if (!colisiona(j, nx, ny)) j.moverJugador(nx, ny);
    }

    /** true si un jugador con el centro en (cx, cy) pisaria algo solido. */
    private boolean colisiona(Jugador j, double cx, double cy) {
        double h = Config.MEDIO_HITBOX;
        int desdeX = (int) Math.floor(cx - h), hastaX = (int) Math.floor(cx + h);
        int desdeY = (int) Math.floor(cy - h), hastaY = (int) Math.floor(cy + h);

        for (int y = desdeY; y <= hastaY; y++) {
            for (int x = desdeX; x <= hastaX; x++) {
                if (!mapa.esTransitable(x, y)) return true;
                Bomba b = bombaEn(x, y);
                if (b != null && !b.puedePasar(j)) return true;
            }
        }
        return false;
    }

    private void permitirPasoSiSuperpone(Jugador j) {
        for (Bomba b : bombas)
            if (j.tocaCasillero(b.getPosX(), b.getPosY())) b.permitirPaso(j);
    }

    /** Items y corona se levantan en el casillero donde esta el CENTRO del jugador. */
    private void recogerObjetos(Jugador j) {
        if (!j.estaVivo()) return;
        int cx = j.getCasilleroX(), cy = j.getCasilleroY();

        Item item = mapa.getItem(cx, cy);
        if (item != null && item.isVisible()) {
            item.aplicarEfecto(j);
            mapa.quitarItem(cx, cy);
        }
        if (corona.estaLibre() && cx == corona.getPosX() && cy == corona.getPosY()) {
            corona.recoger(j);
        }
    }

    // ---------- 3) Golpe aturdidor ----------
    private void aplicarGolpesAturdidores() {
        for (Jugador a : jugadores) {
            if (!a.estaVivo() || !a.tieneGolpeAturdidor()) continue;
            for (Jugador b : jugadores) {
                if (a == b || !b.estaVivo()) continue;
                if (a.distanciaA(b) <= Config.DISTANCIA_GOLPE) b.aturdir();
            }
        }
    }

    // ---------- 4) Bombas ----------
    private void actualizarBombas(double dt) {
        for (Bomba b : bombas) {
            b.actualizarTiempo(dt);
            b.actualizarAtravesadores();
        }

        // Se repite porque una explosion puede detonar otras bombas (reaccion en cadena)
        boolean huboExplosion;
        do {
            huboExplosion = false;
            for (Bomba b : new ArrayList<>(bombas)) {
                if (b.debeExplotar()) {
                    explotar(b);
                    huboExplosion = true;
                }
            }
        } while (huboExplosion);
    }

    private void explotar(Bomba bomba) {
        bombas.remove(bomba);
        bomba.getDuenio().bombaExplotada();

        List<Point> celdas = calcularCeldas(bomba);
        explosiones.add(new Explosion(celdas));

        for (Bomba otra : bombas)
            for (Point p : celdas)
                if (p.x == otra.getPosX() && p.y == otra.getPosY()) otra.detonarYa();
    }

    /** Explosion en cruz: avanza hasta el rango, se frena en paredes y rompe el primer bloque. */
    private List<Point> calcularCeldas(Bomba bomba) {
        List<Point> celdas = new ArrayList<>();
        celdas.add(new Point(bomba.getPosX(), bomba.getPosY()));
        mapa.quitarItemVisible(bomba.getPosX(), bomba.getPosY());

        int[][] direcciones = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] d : direcciones) {
            for (int paso = 1; paso <= bomba.getRango(); paso++) {
                int x = bomba.getPosX() + d[0] * paso;
                int y = bomba.getPosY() + d[1] * paso;
                TipoCasilla tipo = mapa.getCasilla(x, y);

                if (tipo == TipoCasilla.PARED) break;
                celdas.add(new Point(x, y));
                if (tipo == TipoCasilla.BLOQUE) {
                    mapa.destruirObjeto(x, y);   // revela el item, que NO se destruye ahora
                    break;
                }
                mapa.quitarItemVisible(x, y);    // el fuego destruye items que ya estaban a la vista
            }
        }
        return celdas;
    }

    // ---------- 5) Explosiones y muertes ----------
    private void actualizarExplosiones(double dt) {
        for (Explosion e : explosiones) {
            e.actualizar(dt);
            for (Jugador j : jugadores) {
                if (j.estaVivo() && e.contiene(j.getCasilleroX(), j.getCasilleroY()) && j.recibirExplosion()) {
                    matar(j);
                }
            }
        }
        explosiones.removeIf(Explosion::haTerminado);
    }

    private void matar(Jugador j) {
        if (corona.getPortador() == j) corona.soltar();   // la corona vuelve al centro
        Mejoras soltadas = j.morir();
        if (!soltadas.estaVacia()) {
            mapa.ponerItem(j.getCasilleroX(), j.getCasilleroY(),
                    new ItemEspecial(j.getCasilleroX(), j.getCasilleroY(), soltadas));
        }
    }

    // ---------- 6) Victoria ----------
    private void actualizarVictoria() {
        Jugador portador = corona.getPortador();
        if (portador != null && portador.getTiempoCorona() >= Config.TIEMPO_CORONA_VICTORIA) {
            ganador = portador;
            detener();
        }
    }

    // ---------- Consultas ----------
    private Bomba bombaEn(int x, int y) {
        for (Bomba b : bombas) if (b.getPosX() == x && b.getPosY() == y) return b;
        return null;
    }

    private boolean hayBombaEn(int x, int y) { return bombaEn(x, y) != null; }

    public Mapa getMapa() { return mapa; }
    public Corona getCorona() { return corona; }
    public Jugador[] getJugadores() { return jugadores; }
    public List<Bomba> getBombas() { return bombas; }
    public List<Explosion> getExplosiones() { return explosiones; }
    public Jugador getGanador() { return ganador; }
    public boolean isEnEjecucion() { return enEjecucion; }
}
