import java.awt.*;
import javax.swing.*;

/** Pantalla donde se elige cuantos juegan, el modo de juego y se ven los controles. */
public class MenuPartida extends JFrame {
    private int cantJuga = 2;
    private int modoElegido = 0;                 // posicion del modo en el arreglo "niveles"
    private final Nivel[] niveles;               // un nivel por cada archivo de Config.ARCHIVOS_NIVELES
    private final JTextArea info = new JTextArea();

    public MenuPartida() {
        super("Configurar partida");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 440);
        setLocationRelativeTo(null);
        setResizable(false);

        // Lee los archivos de configuracion de cada modo (BufferedReader + FileReader)
        niveles = new Nivel[Config.ARCHIVOS_NIVELES.length];
        for (int i = 0; i < niveles.length; i++)
            niveles[i] = Nivel.cargar(Config.ARCHIVOS_NIVELES[i]);

        // Fila 1: cantidad de jugadores
        JPanel botonesCant = new JPanel(new FlowLayout());
        botonesCant.add(new JLabel("Jugadores:"));
        ButtonGroup grupoCant = new ButtonGroup();
        for (int n = 2; n <= 4; n++) {
            final int cantidad = n;
            JRadioButton rb = new JRadioButton(n + " jugadores", n == cantJuga);
            rb.addActionListener(e -> seleccionarJugadores(cantidad));
            grupoCant.add(rb);
            botonesCant.add(rb);
        }

        // Fila 2: modo de juego
        JPanel botonesModo = new JPanel(new FlowLayout());
        botonesModo.add(new JLabel("Modo:"));
        ButtonGroup grupoModo = new ButtonGroup();
        for (int i = 0; i < niveles.length; i++) {
            final int posicion = i;
            JRadioButton rb = new JRadioButton((i + 1) + ") " + niveles[i].getNombre(), i == modoElegido);
            rb.addActionListener(e -> seleccionarModo(posicion));
            grupoModo.add(rb);
            botonesModo.add(rb);
        }

        JPanel arriba = new JPanel(new GridLayout(2, 1));
        arriba.add(botonesCant);
        arriba.add(botonesModo);

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

        add(arriba, BorderLayout.NORTH);
        add(info, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
    }

    public int seleccionarJugadores(int cantJuga) {
        this.cantJuga = cantJuga;
        mostrarInfo(cantJuga);
        return cantJuga;
    }

    public void seleccionarModo(int posicion) {
        modoElegido = posicion;
        mostrarInfo(cantJuga);
    }

    public void mostrarInfo(int cantJuga) {
        Nivel modo = niveles[modoElegido];

        StringBuilder sb = new StringBuilder("Modo: " + modo.getNombre() + "\n");
        sb.append("  ").append(modo.getDescripcion()).append("\n");
        if (modo.getError() != null)
            sb.append("  AVISO: ").append(modo.getError()).append("\n  (se usan valores normales)\n");

        sb.append("\nControles:\n\n");
        for (int i = 0; i < cantJuga; i++)
            sb.append("  Jugador ").append(i + 1).append(": ").append(ControlTeclas.descripcion(i)).append("\n");
        sb.append("\nReglas: la corona está en el centro. Si la llevás 20 s\n")
          .append("seguidos ganás. Si morís, la soltás y reaparecés en 3 s.\n")
          .append("ESC durante la partida vuelve al menú.");
        info.setText(sb.toString());
    }

    public void iniciarPartida() {
        try {
            new VentanaJuego(cantJuga, niveles[modoElegido]);
        } catch (RuntimeException e) {
            // Si la ventana del juego no se pudo abrir, avisamos y volvemos al menu principal
            JOptionPane.showMessageDialog(this, "No se pudo iniciar la partida: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            new MenuPrincipal().mostrarTitulo();
        } finally {
            dispose();   // en cualquiera de los dos casos, esta pantalla de configuracion se cierra
        }
    }
}
