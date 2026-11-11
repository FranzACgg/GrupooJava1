import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuracion de un nivel (modo de juego). Los valores se leen de un archivo de texto
 * con lineas "clave=valor" usando FileReader + BufferedReader (ver el metodo cargar).
 * Si el archivo falta o tiene errores, se usan los valores por defecto (partida normal).
 */
public class Nivel {
    // Valores por defecto = juego normal
    private String nombre = "Normal";
    private String descripcion = "";
    private int bombasIniciales = 1;
    private double velocidadExtra = 0;       // casilleros por segundo que se suman a la velocidad base
    private boolean escudoInicial = false;
    private String error = null;             // null si el archivo se leyo bien

    /** Lee el archivo de configuracion y devuelve el nivel ya armado. */
    public static Nivel cargar(String ruta) {
        Nivel nivel = new Nivel();
        BufferedReader lector = null;
        try {
            // FileReader abre el archivo; BufferedReader permite leerlo linea por linea
            lector = new BufferedReader(new FileReader(ruta, StandardCharsets.UTF_8));
            String linea;
            while ((linea = lector.readLine()) != null) {
                nivel.interpretarLinea(linea);
            }
        } catch (FileNotFoundException e) {
            nivel.error = "No se encontro el archivo " + ruta;
        } catch (IOException e) {
            nivel.error = "Error al leer " + ruta + ": " + e.getMessage();
        } catch (NumberFormatException e) {
            nivel.error = "Hay un numero invalido en " + ruta;
        } finally {
            // Se ejecuta SIEMPRE: el archivo tiene que cerrarse haya o no haya errores
            try {
                if (lector != null) lector.close();
            } catch (IOException e) {
                System.err.println("No se pudo cerrar " + ruta);
            }
        }
        return nivel;
    }

    /** Interpreta una linea "clave=valor". Ignora lineas vacias y comentarios (#). */
    private void interpretarLinea(String linea) {
        linea = linea.trim();
        if (linea.isEmpty() || linea.startsWith("#")) return;

        int igual = linea.indexOf('=');
        if (igual < 0) return;                                  // linea sin "=": se ignora
        String clave = linea.substring(0, igual).trim();
        String valor = linea.substring(igual + 1).trim();

        switch (clave) {
            case "nombre": nombre = valor; break;
            case "descripcion": descripcion = valor; break;
            case "bombasIniciales": bombasIniciales = Integer.parseInt(valor); break;
            case "velocidadExtra": velocidadExtra = Double.parseDouble(valor); break;
            case "escudoInicial": escudoInicial = Boolean.parseBoolean(valor); break;
            default: break;                                     // clave desconocida: se ignora
        }
    }

    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getBombasIniciales() { return bombasIniciales; }
    public double getVelocidadExtra() { return velocidadExtra; }
    public boolean isEscudoInicial() { return escudoInicial; }
    public String getError() { return error; }
}
