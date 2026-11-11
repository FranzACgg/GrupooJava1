import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Grilla del juego: paredes, bloques rompibles e items escondidos.
public class Mapa {
    // Que hay en cada casillero
    public static final int VACIO = 0;    // se puede caminar
    public static final int PARED = 1;    // indestructible
    public static final int BLOQUE = 2;   // se rompe con una explosion

    private final int[][] casillas = new int[Config.FILAS][Config.COLUMNAS];
    private final Item[][] items = new Item[Config.FILAS][Config.COLUMNAS];

    public Mapa(Random azar) {
        generarEstructura();
        repartirItems(azar);
    }

    private void generarEstructura() {
        for (int y = 0; y < Config.FILAS; y++) {
            for (int x = 0; x < Config.COLUMNAS; x++) {
                boolean borde = x == 0 || y == 0 || x == Config.COLUMNAS - 1 || y == Config.FILAS - 1;
                if (borde) {
                    casillas[y][x] = PARED;
                } else if (enZonaLibre(x, y)) {
                    casillas[y][x] = VACIO;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    casillas[y][x] = PARED;     // pilares fijos
                } else {
                    casillas[y][x] = BLOQUE;
                }
            }
        }
    }

    // Esquinas 3x3 (donde aparecen los jugadores) y centro 3x3 (corona).
    private boolean enZonaLibre(int x, int y) {
        boolean esquinaX = x <= 3 || x >= Config.COLUMNAS - 4;
        boolean esquinaY = y <= 3 || y >= Config.FILAS - 4;
        boolean centro = Math.abs(x - Config.COLUMNAS / 2) <= 1 && Math.abs(y - Config.FILAS / 2) <= 1;
        return (esquinaX && esquinaY) || centro;
    }

    // Esconde 60 items debajo de bloques elegidos al azar.
    private void repartirItems(Random azar) {
        List<Point> bloques = new ArrayList<>();
        for (int y = 0; y < Config.FILAS; y++)
            for (int x = 0; x < Config.COLUMNAS; x++)
                if (casillas[y][x] == BLOQUE) bloques.add(new Point(x, y));

        Collections.shuffle(bloques, azar);
        int cantidad = Math.min(Config.ITEMS_TOTALES, bloques.size());
        for (int i = 0; i < cantidad; i++) {
            Point p = bloques.get(i);
            items[p.y][p.x] = new Item(p.x, p.y, Item.tipoAleatorio(azar));
        }
    }

    public boolean esTransitable(int x, int y) {
        return casillas[y][x] == VACIO;
    }

    // Rompe el bloque y deja a la vista el item que tenia abajo (si habia).
    public void destruirObjeto(int x, int y) {
        casillas[y][x] = VACIO;
        if (items[y][x] != null) items[y][x].revelar();
    }

    public int getCasilla(int x, int y) { return casillas[y][x]; }
    public Item getItem(int x, int y) { return items[y][x]; }
    public void ponerItem(int x, int y, Item item) { items[y][x] = item; }
    public void quitarItem(int x, int y) { items[y][x] = null; }

    // Quita el item solo si ya estaba a la vista (los ocultos estan protegidos por su bloque).
    public void quitarItemVisible(int x, int y) {
        if (items[y][x] != null && items[y][x].isVisible()) items[y][x] = null;
    }
}
