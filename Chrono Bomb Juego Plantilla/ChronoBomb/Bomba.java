import java.util.HashSet;
import java.util.Set;

public class Bomba {
    private final int posX;     // la bomba SI vive en un casillero exacto
    private final int posY;
    private final Jugador duenio;
    private final int rango;
    private final double tiempoDeto;
    private double tiempoTranscu = 0;

    /**
     * Jugadores que estaban encima cuando se planto: pueden salir caminando.
     * Apenas dejan de tocarla, la bomba se vuelve solida para ellos tambien.
     */
    private final Set<Jugador> atravesadores = new HashSet<>();

    public Bomba(int posX, int posY, Jugador duenio, double tiempoDeto, int rango) {
        this.posX = posX;
        this.posY = posY;
        this.duenio = duenio;
        this.tiempoDeto = tiempoDeto;
        this.rango = rango;
    }

    public void actualizarTiempo(double dt) { tiempoTranscu += dt; }
    public boolean debeExplotar() { return tiempoTranscu >= tiempoDeto; }
    /** Para las reacciones en cadena: otra explosion la detona al instante. */
    public void detonarYa() { tiempoTranscu = tiempoDeto; }

    public void permitirPaso(Jugador j) { atravesadores.add(j); }
    public boolean puedePasar(Jugador j) { return atravesadores.contains(j); }
    /** Quita el permiso a quien ya salio de encima de la bomba. */
    public void actualizarAtravesadores() {
        atravesadores.removeIf(j -> !j.tocaCasillero(posX, posY));
    }

    public double getProgreso() { return Math.min(1.0, tiempoTranscu / tiempoDeto); }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getRango() { return rango; }
    public Jugador getDuenio() { return duenio; }
}
