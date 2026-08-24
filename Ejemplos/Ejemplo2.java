public class Clase1_10_08_ejercicio2 {
    public static void main(String[] args) {
        int num = 371;
        int copianum = num;
        int ultimodigito;
        double digitos = Math.floor(Math.log10(num)) + 1;
        double suma = 0;
        while (num >= 0) {
            ultimodigito = num % 10;
            suma = suma + Math.pow(ultimodigito, digitos);
            num = num / 10;
        }
        if (copianum == suma) {
            System.out.println(copianum +
                    " es un Armstrong");
        } else {
            System.out.println(copianum +
                    " no es Armstrong");
        }
    }
}
