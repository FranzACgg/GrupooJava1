import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

/** Ventana de la partida: une el panel, el teclado y el GameLoop. */
public class VentanaJuego extends JFrame {
    private final PanelJuego panel;

    public VentanaJuego(int cantJugadores, Nivel nivel) {
        super("Chrono Bomb - " + nivel.getNombre());
        ControlTeclas controles = new ControlTeclas();
        panel = new PanelJuego(cantJugadores, controles, nivel);

        panel.addKeyListener(controles);
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) volverAlMenu();
            }
        });

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        panel.requestFocusInWindow();
        panel.getGameLoop().iniciar();
    }

    private void volverAlMenu() {
        panel.getGameLoop().detener();
        dispose();
        new MenuPrincipal().mostrarTitulo();
    }
}
