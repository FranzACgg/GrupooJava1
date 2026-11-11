import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Corazon del juego. Corre en su PROPIO HILO (implements Runnable): mientras la partida
 * esta en marcha repite ~60 veces por segundo:  actualizar(dt)  ->  redibujar  ->  dormir un ratito.
 *
 * actualizar(dt) hace siempre lo mismo y en este orden:
 *   1) timers, input y movimiento  2) golpes aturdidores
 *   3) bombas  4) explosiones y muertes  5) corona / victoria
 *
 * Hay 3 tipos de hilos en el juego:
 *   - el hilo de Swing (EDT): dibuja la pantalla y recibe el teclado
 *   - el hilo del GameLoop (esta clase): hace avanzar la logica
 *   - un hilo por cada bomba: espera su tiempo de detonacion
 * Como varios hilos comparten datos, se usan volatile, synchronized y CopyOnWriteArrayList.
 */
public class GameLoop implements Runnable {
    private static final Color[] COLORES = {
        new Color(230, 50, 50), new Color(50, 110, 240), new Color(40, 190, 80), new Color(240, 200, 30)
    };

    private final Mapa mapa = new Mapa(new Random());
    private final Corona corona = new Corona();
    private final Jugador[] jugadores;
    // CopyOnWriteArrayList: se puede recorrer (para dibujar) mientras otro hilo agrega o saca elementos
    private final List<Bomba> bombas = new CopyOnWriteArrayList<>();
    private final List<Explosion> explosiones = new CopyOnWriteArrayList<>();
    private final Movimiento movimiento;
    private final ControlTeclas controles;
    private final Nivel nivel;
    private final Runnable alRedibujar;

    private Thread hilo;
    private volatile boolean enEjecucion = false;   // volatile: lo cambia un hilo y lo lee otro
    private volatile Jugador ganador = null;

    public GameLoop(int cantJugadores, ControlTeclas controles, Nivel nivel, Runnable alRedibujar) {
        this.controles = controles;
        this.nivel = nivel;
        this.alRedibujar = alRedibujar;
        this.jugadores = inicializarJugadores(cantJugadores);
        this.movimiento = new Movimiento(mapa, bombas);
    }

    private Jugador[] inicializarJugadores(int cant) {
        int ultimoX = Config.COLUMNAS - 2, ultimoY = Config.FILAS - 2;
        int[][] esquinas = {{1, 1}, {ultimoX, 1}, {1, ultimoY}, {ultimoX, ultimoY}};
        Jugador[] lista = new Jugador[cant];
        for (int i = 0; i < cant; i++)
            lista[i] = new Jugador(i, COLORES[i], esquinas[i][0], esquinas[i][1], nivel);
        return lista;
    }

    // ================== Hilo del juego ==================
    public void iniciar() {
        enEjecucion = true;
        hilo = new Thread(this, "GameLoop");
        hilo.setDaemon(true);
        hilo.start();           // start() hace que el hilo ejecute run()
    }

    public void detener() {
        enEjecucion = false;
        if (hilo != null) hilo.interrupt();   // despierta al hilo si esta dormido entre frames
    }

    @Override
    public void run() {
        long anterior = System.nanoTime();
        try {
            while (enEjecucion) {
                long ahora = System.nanoTime();
                double dt = Math.min((ahora - anterior) / 1e9, 0.05);   // segundos desde el ultimo frame
                anterior = ahora;

                actualizar(dt);
                alRedibujar.run();
                Thread.sleep(1000 / Config.FPS);     // descansa ~16 ms (60 frames por segundo)
            }
        } catch (InterruptedException e) {
            // detener() nos interrumpio mientras dormiamos: es la forma normal de cortar
        } catch (RuntimeException e) {
            // Si algo falla dentro del juego, lo mostramos en consola en vez de dejar el hilo muerto en silencio
            System.err.println("Error en el bucle del juego:");
            e.printStackTrace();
        } finally {
            // Se ejecuta SIEMPRE, termine bien o mal: corta las bombas y deja la pantalla final dibujada
            enEjecucion = false;
            cancelarBombas();
            alRedibujar.run();
        }
    }

    private void cancelarBombas() {
        for (Bomba b : bombas) b.cancelar();
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
        actualizarBombas();
        actualizarExplosiones(dt);
        actualizarVictoria();
    }

    private void reaparecer(Jugador j) {
        j.reiniciarJugador();
        // si reaparece sobre una bomba, que pueda salir caminando
        for (Bomba b : bombas)
            if (j.tocaCasillero(b.getPosX(), b.getPosY())) b.permitirPaso(j);
    }

    // ---------- 1) Input y movimiento ----------
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
                b.iniciar();      // enciende la mecha: arranca el hilo de la bomba
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
        movimiento.mover(j, dx / largo * velocidad * dt, dy / largo * velocidad * dt);
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

    // ---------- 2) Golpe aturdidor ----------
    private void aplicarGolpesAturdidores() {
        for (Jugador a : jugadores) {
            if (!a.estaVivo() || !a.tieneGolpeAturdidor()) continue;
            for (Jugador b : jugadores) {
                if (a == b || !b.estaVivo()) continue;
                if (a.distanciaA(b) <= Config.DISTANCIA_GOLPE) b.aturdir();
            }
        }
    }

    // ---------- 3) Bombas ----------
    private void actualizarBombas() {
        for (Bomba b : bombas) b.actualizarAtravesadores();

        // Se repite porque una explosion puede detonar otras bombas (reaccion en cadena)
        boolean huboExplosion;
        do {
            huboExplosion = false;
            for (Bomba b : bombas) {
                if (b.haExplotado()) {      // el hilo de la bomba la marco como explotada
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

        // las bombas que estaban en el fuego explotan tambien
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
                int casilla = mapa.getCasilla(x, y);

                if (casilla == Mapa.PARED) break;
                celdas.add(new Point(x, y));
                if (casilla == Mapa.BLOQUE) {
                    mapa.destruirObjeto(x, y);   // revela el item, que NO se destruye ahora
                    break;
                }
                mapa.quitarItemVisible(x, y);    // el fuego destruye items que ya estaban a la vista
            }
        }
        return celdas;
    }

    // ---------- 4) Explosiones y muertes ----------
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

    // ---------- 5) Victoria ----------
    private void actualizarVictoria() {
        Jugador portador = corona.getPortador();
        if (portador != null && portador.getTiempoCorona() >= Config.TIEMPO_CORONA_VICTORIA) {
            ganador = portador;
            enEjecucion = false;      // el while de run() termina y el finally deja la pantalla final
        }
    }

    // ---------- Consultas ----------
    private boolean hayBombaEn(int x, int y) {
        for (Bomba b : bombas)
            if (b.getPosX() == x && b.getPosY() == y) return true;
        return false;
    }

    public Mapa getMapa() { return mapa; }
    public Corona getCorona() { return corona; }
    public Jugador[] getJugadores() { return jugadores; }
    public List<Bomba> getBombas() { return bombas; }
    public List<Explosion> getExplosiones() { return explosiones; }
    public Nivel getNivel() { return nivel; }
    public Jugador getGanador() { return ganador; }
    public boolean isEnEjecucion() { return enEjecucion; }
}
