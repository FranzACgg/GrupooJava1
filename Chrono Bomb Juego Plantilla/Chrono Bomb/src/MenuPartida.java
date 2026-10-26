import java.awt.*;
import javax.swing.*;

/** Pantalla donde se elige cuantos juegan y se ven los controles. */
public class MenuPartida extends JFrame {
    private int cantJuga = 2;
    private final JTextArea info = new JTextArea();

    public MenuPartida() {
        super("Configurar partida");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 360);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel botonesCant = new JPanel(new FlowLayout());
        ButtonGroup grupo = new ButtonGroup();
        for (int n = 2; n <= 4; n++) {
            final int cantidad = n;
            JRadioButton rb = new JRadioButton(n + " jugadores", n == cantJuga);
            rb.addActionListener(e -> seleccionarJugadores(cantidad));
            grupo.add(rb);
            botonesCant.add(rb);
        }

        info.setEditable(false);
        info.setFont(new Font("Monospaced", Font.PLAIN, 13));
        info.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        mostrarInfo(cantJuga);

        JButton iniciar = new JButton("Iniciar partida");
        iniciar.addActionListener(e -> iniciarPartida());
        JButton volver = new JButton("Volver");
        volver.addActionListener(e -> { dispose(); new MenuPrincipal().mostrarTitulo(); });
        JPanel abajo = new JPanel(new FlowLayout());
        abajo.add(volver);
        abajo.add(iniciar);

        add(botonesCant, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
    }

    public int seleccionarJugadores(int cantJuga) {
        this.cantJuga = cantJuga;
        mostrarInfo(cantJuga);
        return cantJuga;
    }

    public void mostrarInfo(int cantJuga) {
        StringBuilder sb = new StringBuilder("Controles:\n\n");
        for (int i = 0; i < cantJuga; i++)
            sb.append("  Jugador ").append(i + 1).append(": ").append(ControlTeclas.descripcion(i)).append("\n");
        sb.append("\nReglas: la corona está en el centro. Si la llevás 20 s\n")
          .append("seguidos ganás. Si morís, la soltás y reaparecés en 3 s.\n")
          .append("ESC durante la partida vuelve al menú.");
        info.setText(sb.toString());
    }

    public void iniciarPartida() {
        dispose();
        new VentanaJuego(cantJuga);
    }
}
