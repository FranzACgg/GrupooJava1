import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;
import javax.swing.JPanel;

/** Solo DIBUJA el estado del GameLoop. No contiene reglas del juego. */
public class PanelJuego extends JPanel {
    private static final int T = Config.TAM_CASILLA;
    private static final int ALTO_HUD = 70;

    private final GameLoop gameLoop;

    public PanelJuego(int cantJugadores, ControlTeclas controles) {
        gameLoop = new GameLoop(cantJugadores, controles, this::repaint);
        setPreferredSize(new Dimension(Config.COLUMNAS * T, Config.FILAS * T + ALTO_HUD));
        setBackground(new Color(25, 25, 35));
        setFocusable(true);
    }

    public GameLoop getGameLoop() { return gameLoop; }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //Suavizado de bordes

        dibujarMapa(g);
        dibujarItems(g);
        dibujarBombas(g);
        dibujarExplosiones(g);
        dibujarCoronaLibre(g);
        dibujarJugadores(g);
        dibujarHud(g);
        if (gameLoop.getGanador() != null) dibujarFinal(g);
    }

    private void dibujarMapa(Graphics2D g) {
        Mapa mapa = gameLoop.getMapa();
        for (int y = 0; y < Config.FILAS; y++) {
            for (int x = 0; x < Config.COLUMNAS; x++) {
                switch (mapa.getCasilla(x, y)) {
                    case PARED:
                        g.setColor(new Color(90, 95, 110));
                        g.fillRect(x * T, y * T, T, T);
                        g.setColor(new Color(60, 65, 80));
                        g.drawRect(x * T, y * T, T - 1, T - 1);
                        break;
                    case BLOQUE:
                        g.setColor(new Color(170, 130, 90));
                        g.fillRect(x * T + 1, y * T + 1, T - 2, T - 2);
                        g.setColor(new Color(120, 90, 60));
                        g.drawRect(x * T + 3, y * T + 3, T - 7, T - 7);
                        break;
                    default:
                        g.setColor(((x + y) % 2 == 0) ? new Color(50, 55, 70) : new Color(45, 50, 65));
                        g.fillRect(x * T, y * T, T, T);
                }
            }
        }
    }

    private void dibujarItems(Graphics2D g) {
        Mapa mapa = gameLoop.getMapa();
        for (int y = 0; y < Config.FILAS; y++) {
            for (int x = 0; x < Config.COLUMNAS; x++) {
                Item item = mapa.getItem(x, y);
                if (item == null || !item.isVisible()) continue;
                g.setColor(item.getTipo().getColor());
                g.fill(new Ellipse2D.Double(x * T + 8, y * T + 8, T - 16, T - 16));
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString(item.getTipo().getLetra(), x * T + 15, y * T + 26);
            }
        }
    }

    private void dibujarBombas(Graphics2D g) {
        for (Bomba b : gameLoop.getBombas()) {
            int tam = (b.getProgreso() * 10 % 2 < 1) ? T - 14 : T - 10;   // "late" mas rapido al final
            int off = (T - tam) / 2;
            g.setColor(Color.BLACK);
            g.fillOval(b.getPosX() * T + off, b.getPosY() * T + off, tam, tam);
            g.setColor(Color.ORANGE);
            g.fillOval(b.getPosX() * T + T / 2 - 3, b.getPosY() * T + off - 2, 6, 6);
        }
    }

    private void dibujarExplosiones(Graphics2D g) {
        for (Explosion e : gameLoop.getExplosiones()) {
            for (Point p : e.getCeldas()) {
                g.setColor(new Color(255, 140, 0, 220));
                g.fillRoundRect(p.x * T + 3, p.y * T + 3, T - 6, T - 6, 12, 12);
                g.setColor(new Color(255, 230, 80, 230));
                g.fillRoundRect(p.x * T + 12, p.y * T + 12, T - 24, T - 24, 8, 8);
            }
        }
    }

    private void dibujarCoronaLibre(Graphics2D g) {
        Corona c = gameLoop.getCorona();
        if (c.estaLibre()) dibujarCorona(g, c.getPosX() * T + 6, c.getPosY() * T + 8, T - 12);
    }

    private void dibujarCorona(Graphics2D g, int x, int y, int ancho) {
        int alto = ancho * 3 / 4;
        int[] xs = {x, x, x + ancho / 4, x + ancho / 2, x + 3 * ancho / 4, x + ancho, x + ancho};
        int[] ys = {y + alto, y, y + alto / 2, y, y + alto / 2, y, y + alto};
        g.setColor(new Color(255, 205, 40));
        g.fillPolygon(xs, ys, xs.length);
        g.setColor(new Color(160, 110, 0));
        g.drawPolygon(xs, ys, xs.length);
    }

    private void dibujarJugadores(Graphics2D g) {
        int lado = (int) Math.round(Config.MEDIO_HITBOX * 2 * T);   // el dibujo coincide con la hitbox
        for (Jugador j : gameLoop.getJugadores()) {
            if (!j.estaVivo()) continue;

            // ilusion de parpadeo anlternando entre true y false
            boolean parpadeo = j.getTiempoInvulnerable() > 0 && ((int) (j.getTiempoInvulnerable() * 10) % 2 == 0);
            if (parpadeo) continue;

            // posicion en pantalla = centro (en casilleros) * tamanio - mitad del cuerpo
            int cx = (int) Math.round(j.getPosX() * T);
            int cy = (int) Math.round(j.getPosY() * T);
            int px = cx - lado / 2, py = cy - lado / 2;

            g.setColor(j.getColor());
            g.fillRoundRect(px, py, lado, lado, 14, 14);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString(String.valueOf(j.getNumero()), cx - 5, cy + 6);

            if (j.tieneEscudo()) {
                g.setColor(new Color(120, 200, 255));
                g.setStroke(new BasicStroke(3));
                g.drawOval(px - 4, py - 4, lado + 8, lado + 8);
                g.setStroke(new BasicStroke(1));
            }
            if (j.estaAturdido()) {
                g.setColor(Color.YELLOW);
                g.drawString("zZ", cx + 6, py - 2);
            }
            if (j.tieneCorona()) dibujarCorona(g, cx - 10, py - 16, 20);
        }
    }

    private void dibujarHud(Graphics2D g) {
        int base = Config.FILAS * T;
        g.setColor(new Color(15, 15, 22));
        g.fillRect(0, base, getWidth(), ALTO_HUD);

        Jugador[] js = gameLoop.getJugadores();
        int ancho = getWidth() / js.length;
        for (int i = 0; i < js.length; i++) {
            Jugador j = js[i];
            int x = i * ancho + 10;
            g.setColor(j.getColor());
            g.setFont(new Font("SansSerif", Font.BOLD, 15));
            g.drawString("Jugador " + j.getNumero(), x, base + 20);

            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));

            if (j.tieneCorona()) {
                g.setColor(new Color(255, 205, 40));
                g.drawString(String.format("CORONA %.1f / %.0fs", j.getTiempoCorona(),
                        Config.TIEMPO_CORONA_VICTORIA), x, base + 66);
            }
        }
    }

    private void dibujarFinal(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
        Jugador ganador = gameLoop.getGanador();
        g.setColor(ganador.getColor());
        g.setFont(new Font("SansSerif", Font.BOLD, 44));
        centrar(g, "¡Gana el Jugador " + ganador.getNumero() + "!", getHeight() / 2 - 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        centrar(g, "Presioná ESC para volver al menú", getHeight() / 2 + 35);
    }

    private void centrar(Graphics2D g, String texto, int y) {
        int ancho = g.getFontMetrics().stringWidth(texto);
        g.drawString(texto, (getWidth() - ancho) / 2, y);
    }
}
