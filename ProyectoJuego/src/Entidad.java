import java.awt.image.BufferedImage;

public class Entidad {
    public int velocidad;
    public int x;
    public int y;
    public int xinicial;
    public int yinicial;

    public BufferedImage a1, a2, a3, b1, b2, b3, i1, i2, i3, d1, d2, d3;
    public String direccion;

    public int contadorSprite = 0;
    public int numeroSprite = 1;
    boolean reiniciarSprite;
}
