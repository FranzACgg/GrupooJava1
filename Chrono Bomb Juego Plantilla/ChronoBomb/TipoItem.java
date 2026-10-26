import java.awt.Color;
import java.util.Random;

/** Tipos de item con su probabilidad de aparicion (suman 100). */
public enum TipoItem {
    VELOCIDAD(20, "V", new Color(0, 200, 255)),
    GOLPE_ATURDIDOR(10, "G", new Color(255, 160, 0)),
    ESCUDO(15, "E", new Color(80, 120, 255)),
    RANGO(15, "R", new Color(255, 80, 80)),
    CANT_BOMBAS(20, "B", new Color(170, 170, 170)),
    TIEMPO_DETONACION(20, "T", new Color(190, 90, 255)),
    ESPECIAL(0, "?", new Color(255, 0, 200));   // lo suelta un jugador al morir (no sale al azar)

    private final int probabilidad;
    private final String letra;
    private final Color color;

    TipoItem(int probabilidad, String letra, Color color) {
        this.probabilidad = probabilidad;
        this.letra = letra;
        this.color = color;
    }

    public String getLetra() { return letra; }
    public Color getColor() { return color; }

    /** Elige un tipo respetando los porcentajes del enunciado. */
    public static TipoItem aleatorio(Random azar) {
        int n = azar.nextInt(100);
        int acumulado = 0;
        for (TipoItem t : values()) {
            acumulado += t.probabilidad;
            if (n < acumulado) return t;
        }
        return VELOCIDAD;
    }
}
