public class Clase1_10_08_ejercicio1{
    public static void main(String[] args) {
        int puntaje = 76;
        char nota;

        if (puntaje >= 90) {
            nota = 'A';
        } else if (puntaje >= 80) {
            nota = 'B';
        } else if (puntaje >= 70) {
            nota = 'C';
        } else if (puntaje >= 60) {
            nota = 'D';
        } else {
            nota = 'F';
        }

        System.out.println(nota);
    }
}
