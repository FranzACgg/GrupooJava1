/**
 * Conjunto de mejoras que tiene un jugador (o que contiene un item especial).
 * Separarlo de Jugador permite "soltarlo" al morir y "recogerlo" despues.
 */
public class Mejoras {
    private int velocidad;      // cantidad de mejoras de velocidad
    private int rango;
    private int bombas;
    private int detonacion;
    private boolean escudo;     // no acumulable
    private boolean golpeAturdidor;

    public void aplicar(int tipo) {
        switch (tipo) {
            case Item.VELOCIDAD: velocidad++; break;
            case Item.RANGO: rango++; break;
            case Item.CANT_BOMBAS: bombas++; break;
            case Item.TIEMPO_DETONACION: detonacion++; break;
            case Item.ESCUDO: escudo = true; break;
            case Item.GOLPE_ATURDIDOR: golpeAturdidor = true; break;
            default: break;
        }
    }

    public void sumar(Mejoras otras) {
        velocidad += otras.velocidad;
        rango += otras.rango;
        bombas += otras.bombas;
        detonacion += otras.detonacion;
        escudo |= otras.escudo;
        golpeAturdidor |= otras.golpeAturdidor;
    }

    public Mejoras copiar() {
        Mejoras m = new Mejoras();
        m.sumar(this);
        return m;
    }

    public void reiniciar() {
        velocidad = rango = bombas = detonacion = 0;
        escudo = golpeAturdidor = false;
    }

    public boolean estaVacia() {
        return velocidad == 0 && rango == 0 && bombas == 0 && detonacion == 0
                && !escudo && !golpeAturdidor;
    }

    public int getVelocidad() { return velocidad; }
    public int getRango() { return rango; }
    public int getBombas() { return bombas; }
    public int getDetonacion() { return detonacion; }
    public boolean tieneEscudo() { return escudo; }
    public boolean tieneGolpeAturdidor() { return golpeAturdidor; }
    public void consumirEscudo() { escudo = false; }
}
