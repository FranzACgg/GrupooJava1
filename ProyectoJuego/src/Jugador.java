import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Jugador extends Entidad{

    PanelJuego pj;
    MapeoTeclas teclas;

    public Jugador(PanelJuego pj, MapeoTeclas teclas){
        this.pj = pj;
        this.teclas = teclas;
        valoresPredeterminados();
        mostrarSpriteJugador();
    }

    public void valoresPredeterminados(){
        x=100;
        y=100;
        xinicial=100;
        yinicial=100;
        velocidad=4;
        direccion = "derecha";
    }

    public void mostrarSpriteJugador(){
        try{
            a1 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/a1.png"));
            a2 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/a2.png"));
            a3 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/a3.png"));
            i1 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/i1.png"));
            i2= ImageIO.read(getClass().getResourceAsStream("res/Jugador/i2.png"));
            i3= ImageIO.read(getClass().getResourceAsStream("res/Jugador/i3.png"));
            b1 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/b1.png"));
            b2 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/b2.png"));
            b3 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/b3.png"));
            d1 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/d1.png"));
            d2 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/d2.png"));
            d3 = ImageIO.read(getClass().getResourceAsStream("res/Jugador/d3.png"));
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void update(){
        if (MapeoTeclas.arriba || MapeoTeclas.derecha || MapeoTeclas.abajo || MapeoTeclas.izquierda){
            if(MapeoTeclas.arriba){
                direccion = "arriba";
                y -= velocidad;
            }
            if(MapeoTeclas.izquierda){
                direccion = "izquierda";
                x -= velocidad;
            }
            if(MapeoTeclas.abajo){
                direccion = "abajo";
                y += velocidad;
            }
            if(MapeoTeclas.derecha){
                direccion = "derecha";
                x += velocidad;
            }
            contadorSprite++;
            // declared once, outside this method (a field)
            boolean reiniciarSprite = false;

// ...

            if (contadorSprite > 10) {
                if (numeroSprite == 1) {
                    reiniciarSprite = false;
                    numeroSprite = 2;
                } else if (numeroSprite == 2) {
                    if (reiniciarSprite) {
                        numeroSprite = 1;
                    } else {
                        numeroSprite = 3;
                    }
                } else if (numeroSprite == 3) {
                    reiniciarSprite = true;
                    numeroSprite = 2;
                }
                contadorSprite = 0;
            }
        }
    }

    public void draw(Graphics2D g2){
        BufferedImage imagen = null;
        switch(direccion){
            case "arriba":
                if(numeroSprite == 1){
                    imagen = a1;
                }
                if(numeroSprite == 2){
                    imagen = a2;
                }
                if(numeroSprite == 3){
                    imagen = a3;
                }
                break;
            case "abajo":
                if(numeroSprite == 1){
                    imagen = b1;
                }
                if(numeroSprite == 2){
                    imagen = b2;
                }
                if(numeroSprite == 3){
                    imagen = b3;
                }
                break;
            case "izquierda":
                if(numeroSprite == 1){
                    imagen = i1;
                }
                if(numeroSprite == 2){
                    imagen = i2;
                }
                if(numeroSprite == 3){
                    imagen = i3;
                }
                break;
            case "derecha":
                if(numeroSprite == 1){
                    imagen = d1;
                }
                if(numeroSprite == 2){
                    imagen = d2;
                }
                if(numeroSprite == 3){
                    imagen = d3;
                }
                break;
        }
        g2.drawImage(imagen, x, y, pj.tamanoCasillasReescalado, pj.tamanoCasillasReescalado, null);
    }
}
