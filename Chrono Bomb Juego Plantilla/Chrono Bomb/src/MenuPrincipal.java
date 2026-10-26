import java.awt.*;
import javax.swing.*;

public class MenuPrincipal extends JFrame {
    private final String tituloPrincipal = "CHRONO BOMB";

    public MenuPrincipal() {
        super("Chrono Bomb");
        //Cierra la aplicacion por completo cuando se seleccione "x" y es 3 definida por java (Cerrar definitivamente)
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 320);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 60, 25, 60));
        panel.setBackground(new Color(25, 25, 35));

        JLabel titulo = new JLabel(tituloPrincipal, SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 36));
        titulo.setForeground(new Color(255, 205, 40));

        JLabel subtitulo = new JLabel("Mantené la corona 20 segundos", SwingConstants.CENTER);
        subtitulo.setForeground(Color.BLACK);

        JButton jugar = new JButton("Jugar");
        jugar.addActionListener(e -> iniciarConfigPartida());
        JButton salir = new JButton("Salir");
        salir.addActionListener(e -> salirDelJuego());

        panel.add(titulo);
        panel.add(subtitulo);
        panel.add(jugar);
        panel.add(salir);
        add(panel);
    }

    public void mostrarTitulo() { setVisible(true); }

    public MenuPartida iniciarConfigPartida() {
        MenuPartida menu = new MenuPartida();
        menu.setVisible(true);
        //Finaliza los recursos que usa Swing
        dispose();
        return menu;
    }

    public void salirDelJuego() { System.exit(0); }
}
