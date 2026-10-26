/** La corona: esta en el centro hasta que alguien la agarra; si el portador muere vuelve al centro. */
public class Corona {
    private final int centroX = Config.COLUMNAS / 2;
    private final int centroY = Config.FILAS / 2;
    private Jugador portador;

    public boolean estaLibre() { return portador == null; }
    public Jugador getPortador() { return portador; }
    public int getPosX() { return centroX; }
    public int getPosY() { return centroY; }

    public void recoger(Jugador j) {
        portador = j;
        j.setTieneCorona(true);
    }

    public void soltar() {
        if (portador != null) portador.perderCorona();
        portador = null;
    }
}
