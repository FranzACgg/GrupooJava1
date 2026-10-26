import java.awt.Color;

/**
 * Un jugador. Su posicion es VECTORIAL: (posX, posY) son numeros decimales medidos en
 * "casilleros" y apuntan al CENTRO del jugador. Ej: (1.5, 1.5) es el medio del casillero (1,1).
 * El jugador ocupa un cuadrado de lado 2*MEDIO_HITBOX alrededor de ese centro.
 *
 * Regla clave: mientras lleva la corona, las mejoras NO se aplican (pero no se pierden)
 * y su velocidad pasa a ser 5.
 */
public class Jugador {
    private final int indice;            // 0..3
    private final Color color;
    private final double spawnX, spawnY;

    private double posX, posY;           // centro, en casilleros (decimales)
    private final Mejoras mejoras = new Mejoras();
    private boolean vivo = true;
    private boolean tieneCorona = false;
    private int bombasActivas = 0;

    private double tiempoCorona = 0;        // segundos acumulados con la corona
    private double tiempoReaparicion = 0;
    private double tiempoInvulnerable = 0;
    private double tiempoStun = 0;
    private double tiempoInmuneStun = 0;    // evita quedar aturdido infinitamente

    /** (casilleroX, casilleroY) es el casillero de salida; el jugador arranca en su centro. */
    public Jugador(int indice, Color color, int casilleroX, int casilleroY) {
        this.indice = indice;
        this.color = color;
        this.spawnX = casilleroX + 0.5;
        this.spawnY = casilleroY + 0.5;
        this.posX = spawnX;
        this.posY = spawnY;
    }

    // ---------- Stats efectivos ----------
    public double getVelocidad() {
        if (tieneCorona) return Config.VELOCIDAD_CORONA;
        return Config.VELOCIDAD_BASE + mejoras.getVelocidad() * Config.VELOCIDAD_POR_MEJORA;
    }
    public int getRango() {
        return tieneCorona ? Config.RANGO_BASE : Config.RANGO_BASE + mejoras.getRango();
    }
    public int getMaxBombas() {
        return tieneCorona ? 1 : 1 + mejoras.getBombas();
    }
    public double getTiempoDetonacion() {
        return tieneCorona ? Config.DETONACION_BASE
                : Config.DETONACION_BASE + mejoras.getDetonacion() * Config.DETONACION_POR_MEJORA;
    }
    public boolean tieneEscudo() { return !tieneCorona && mejoras.tieneEscudo(); }
    public boolean tieneGolpeAturdidor() { return !tieneCorona && mejoras.tieneGolpeAturdidor(); }

    // ---------- Posicion ----------
    /** Casillero que contiene el centro del jugador (para bombas, items, corona y explosiones). */
    public int getCasilleroX() { return (int) Math.floor(posX); }
    public int getCasilleroY() { return (int) Math.floor(posY); }

    /** true si el cuerpo del jugador se superpone (aunque sea un poco) con ese casillero. */
    public boolean tocaCasillero(int cx, int cy) {
        double h = Config.MEDIO_HITBOX;
        return posX + h > cx && posX - h < cx + 1 && posY + h > cy && posY - h < cy + 1;
    }

    public double distanciaA(Jugador otro) {
        return Math.hypot(posX - otro.posX, posY - otro.posY);
    }

    // ---------- Acciones ----------
    public void actualizarTemporizadores(double dt) {
        if (!vivo) {
            tiempoReaparicion -= dt;
            return;
        }
        tiempoInvulnerable = Math.max(0, tiempoInvulnerable - dt);
        tiempoStun = Math.max(0, tiempoStun - dt);
        tiempoInmuneStun = Math.max(0, tiempoInmuneStun - dt);
        if (tieneCorona) tiempoCorona += dt;
    }

    public boolean puedeMoverse() {
        return vivo && tiempoStun <= 0;
    }

    /** Coloca al jugador en una posicion ya validada (las colisiones las resuelve el GameLoop). */
    public void moverJugador(double nuevoX, double nuevoY) {
        posX = nuevoX;
        posY = nuevoY;
    }

    /** Devuelve la bomba creada (en el casillero donde esta parado), o null si llego a su limite. */
    public Bomba plantarBomba() {
        if (!vivo || bombasActivas >= getMaxBombas()) return null;
        bombasActivas++;
        return new Bomba(getCasilleroX(), getCasilleroY(), this, getTiempoDetonacion(), getRango());
    }

    public void bombaExplotada() { bombasActivas = Math.max(0, bombasActivas - 1); }

    /** @return true si murio (el escudo y la invulnerabilidad lo salvan). */
    public boolean recibirExplosion() {
        if (!vivo || tiempoInvulnerable > 0) return false;
        if (tieneEscudo()) {
            mejoras.consumirEscudo();
            tiempoInvulnerable = 0.5;   // evita que la misma explosion lo mate enseguida
            return false;
        }
        return true;
    }

    /** Lo deja muerto y devuelve las mejoras que suelta. */
    public Mejoras morir() {
        vivo = false;
        tiempoReaparicion = Config.TIEMPO_REAPARICION;
        Mejoras soltadas = mejoras.copiar();
        mejoras.reiniciar();
        return soltadas;
    }

    public boolean listoParaReaparecer() { return !vivo && tiempoReaparicion <= 0; }

    public void reiniciarJugador() {
        posX = spawnX;
        posY = spawnY;
        vivo = true;
        tiempoInvulnerable = Config.TIEMPO_INVULNERABLE;
        tiempoStun = 0;
    }

    public void aturdir() {
        if (tiempoStun > 0 || tiempoInmuneStun > 0) return;
        tiempoStun = Config.TIEMPO_STUN;
        tiempoInmuneStun = Config.TIEMPO_STUN + 1.0;
    }

    public void setTieneCorona(boolean valor) { tieneCorona = valor; }
    public void perderCorona() { tieneCorona = false; tiempoCorona = 0; }

    // ---------- Getters ----------
    public int getNumero() { return indice + 1; }
    public int getIndice() { return indice; }
    public Color getColor() { return color; }
    public double getPosX() { return posX; }
    public double getPosY() { return posY; }
    public Mejoras getMejoras() { return mejoras; }
    public boolean estaVivo() { return vivo; }
    public boolean tieneCorona() { return tieneCorona; }
    public double getTiempoCorona() { return tiempoCorona; }
    public double getTiempoReaparicion() { return Math.max(0, tiempoReaparicion); }
    public double getTiempoInvulnerable() { return tiempoInvulnerable; }
    public boolean estaAturdido() { return tiempoStun > 0; }
}
