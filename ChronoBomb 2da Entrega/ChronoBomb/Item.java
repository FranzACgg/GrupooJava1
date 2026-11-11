import java.awt.Color;
import java.util.Random;

/**
 * Power-up que aparece en el mapa al romper un bloque.
 * El "tipo" es un numero; las constantes de abajo le ponen nombre a cada numero
 * y los arreglos paralelos guardan los datos de cada tipo (la posicion = el numero del tipo).
 */
public class Item {
    // Tipos de item
    public static final int VELOCIDAD = 0;
    public static final int GOLPE_ATURDIDOR = 1;
    public static final int ESCUDO = 2;
    public static final int RANGO = 3;
    public static final int CANT_BOMBAS = 4;
    public static final int TIEMPO_DETONACION = 5;
    public static final int ESPECIAL = 6;     // lo suelta un jugador al morir (no sale al azar)

    // Datos de cada tipo, en el mismo orden que las constantes
    private static final int[] PROBABILIDAD = {20, 10, 15, 15, 20, 20, 0};   // suman 100
    private static final String[] LETRAS = {"V", "G", "E", "R", "B", "T", "?"};
    private static final Color[] COLORES = {
        new Color(0, 200, 255), new Color(255, 160, 0), new Color(80, 120, 255),
        new Color(255, 80, 80), new Color(170, 170, 170), new Color(190, 90, 255),
        new Color(255, 0, 200)
    };

    private final int posX;
    private final int posY;
    private final int tipo;
    private boolean visible = false;   // oculto hasta que se rompe el bloque que lo cubre

    public Item(int posX, int posY, int tipo) {
        this.posX = posX;
        this.posY = posY;
        this.tipo = tipo;
    }

    /** Elige un tipo al azar respetando los porcentajes del enunciado. */
    public static int tipoAleatorio(Random azar) {
        int n = azar.nextInt(100);          // 0..99
        int acumulado = 0;
        for (int tipo = 0; tipo < PROBABILIDAD.length; tipo++) {
            acumulado += PROBABILIDAD[tipo];
            if (n < acumulado) return tipo;
        }
        return VELOCIDAD;
    }

    /** Efecto al ser recogido. ItemEspecial lo redefine (polimorfismo). */
    public void aplicarEfecto(Jugador j) {
        j.getMejoras().aplicar(tipo);
    }

    public void revelar() { visible = true; }
    public boolean isVisible() { return visible; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getTipo() { return tipo; }
    public String getLetra() { return LETRAS[tipo]; }
    public Color getColor() { return COLORES[tipo]; }
}
