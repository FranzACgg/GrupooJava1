import java.util.HashSet;
import java.util.Set;

/**
 * Una bomba. Cada bomba tiene su PROPIO HILO que "duerme" el tiempo de detonacion
 * y al despertar se marca como explotada. El GameLoop pregunta haExplotado() en cada frame.
 */
public class Bomba implements Runnable {
    private final int posX;     // la bomba SI vive en un casillero exacto
    private final int posY;
    private final Jugador duenio;
    private final int rango;
    private final double tiempoDeto;          // segundos

    private final Thread hilo = new Thread(this, "Bomba");
    private volatile boolean explotada = false;   // volatile: lo escribe el hilo de la bomba y lo lee el GameLoop
    private volatile long inicio;                 // para animar el "latido" en pantalla

    private final Set<Jugador> atravesadores = new HashSet<>();

    public Bomba(int posX, int posY, Jugador duenio, double tiempoDeto, int rango) {
        this.posX = posX;
        this.posY = posY;
        this.duenio = duenio;
        this.tiempoDeto = tiempoDeto;
        this.rango = rango;
        hilo.setDaemon(true);   // si se cierra el juego, el hilo no lo mantiene vivo
    }

    // ---------- Hilo ----------
    /** Enciende la mecha: arranca el hilo. */
    public void iniciar() {
        inicio = System.nanoTime();
        hilo.start();
    }

    /** Esto es lo que ejecuta el hilo de la bomba. */
    @Override
    public void run() {
        try {
            Thread.sleep((long) (tiempoDeto * 1000));   // espera el tiempo de detonacion
        } catch (InterruptedException e) {
            // Nos interrumpieron: otra explosion o el fin de la partida. No es un error.
        } finally {
            explotada = true;   // pase lo que pase, al terminar la bomba queda explotada
        }
    }

    /** Para las reacciones en cadena: otra explosion la detona al instante. */
    public void detonarYa() {
        explotada = true;
        hilo.interrupt();    // despierta al hilo si todavia esta durmiendo
    }

    /** Corta la espera (se usa al terminar la partida). */
    public void cancelar() {
        hilo.interrupt();
    }

    public boolean haExplotado() { return explotada; }

    // ---------- Pasar por encima ----------
    public void permitirPaso(Jugador j) { atravesadores.add(j); }
    public boolean puedePasar(Jugador j) { return atravesadores.contains(j); }
    /** Quita el permiso a quien ya salio de encima de la bomba. */
    public void actualizarAtravesadores() {
        atravesadores.removeIf(j -> !j.tocaCasillero(posX, posY));
    }

    /** De 0 (recien puesta) a 1 (por explotar). */
    public double getProgreso() {
        if (explotada) return 1.0;
        double transcurrido = (System.nanoTime() - inicio) / 1e9;
        return Math.min(1.0, transcurrido / tiempoDeto);
    }

    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getRango() { return rango; }
    public Jugador getDuenio() { return duenio; }
}
