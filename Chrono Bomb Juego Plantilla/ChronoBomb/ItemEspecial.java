/** Lo que suelta un jugador al morir: contiene TODAS sus mejoras. */
public class ItemEspecial extends Item {
    private final Mejoras contenido;

    public ItemEspecial(int posX, int posY, Mejoras contenido) {
        super(posX, posY, TipoItem.ESPECIAL);
        this.contenido = contenido;
        revelar();   // nace visible
    }

    @Override
    public void aplicarEfecto(Jugador j) {
        j.getMejoras().sumar(contenido);
    }
}
