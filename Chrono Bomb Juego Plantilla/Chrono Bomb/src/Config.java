/**
 * Todas las constantes del juego en un solo lugar.
 * Si te piden cambiar una regla (ej: "la corona se mantiene 30 segundos"),
 * casi seguro es tocar solo este archivo.
 */
public final class Config {
    private Config() {}

    // Mapa: 15x13 jugables + 1 casillero de borde por lado
    public static final int COLUMNAS = 17;
    public static final int FILAS = 15;
    public static final int TAM_CASILLA = 40;      // pixeles
    public static final int ITEMS_TOTALES = 60;

    // Jugador
    public static final double VELOCIDAD_BASE = 3;          // casilleros por segundo
    public static final double VELOCIDAD_POR_MEJORA = 0.5;
    public static final int RANGO_BASE = 2;
    public static final double DETONACION_BASE = 1.5;       // segundos
    public static final double DETONACION_POR_MEJORA = 0.5;
    public static final double TIEMPO_REAPARICION = 3;
    public static final double TIEMPO_INVULNERABLE = 1;
    public static final double TIEMPO_STUN = 0.5;
    public static final double MEDIO_HITBOX = 0.38;         // mitad del lado del jugador, en casilleros
    public static final double DISTANCIA_GOLPE = 1.0;       // alcance del golpe aturdidor (casilleros)

    // Partida
    public static final double TIEMPO_CORONA_VICTORIA = 20;
    public static final double DURACION_EXPLOSION = 0.4;
    public static final int FPS = 60;
}
