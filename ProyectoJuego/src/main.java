import javax.swing.JFrame;

public class main {
    public static void main(String[] args){
        JFrame ventana = new JFrame();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.setTitle("Cronobomba");

        PanelJuego PanelJuego = new PanelJuego(4);
        ventana.add(PanelJuego);

        ventana.pack();

        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);

        PanelJuego.iniciarHilo();
    }
}
