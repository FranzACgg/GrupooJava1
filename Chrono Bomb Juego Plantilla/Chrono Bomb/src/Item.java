/** Power-up que aparece en el mapa al romper un bloque. */
public class Item {
    private final int posX;
    private final int posY;
    private final TipoItem tipo;
    private boolean visible = false;   // oculto hasta que se rompe el bloque que lo cubre

    public Item(int posX, int posY, TipoItem tipo) {
        this.posX = posX;
        this.posY = posY;
        this.tipo = tipo;
    }

    /** Efecto al ser recogido. ItemEspecial lo redefine (polimorfismo). */
    public void aplicarEfecto(Jugador j) {
        j.getMejoras().aplicar(tipo);
    }

    public void revelar() { visible = true; }
    public boolean isVisible() { return visible; }
    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public TipoItem getTipo() { return tipo; }
}
