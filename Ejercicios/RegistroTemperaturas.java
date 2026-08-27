import java.util.Arrays;

public class RegistroTemperaturas {
    double[] temperaturas;
    public RegistroTemperaturas(double[] temperaturas) {
        this.temperaturas = temperaturas;

    }
    public double temperaturaMaxima() {
        double maxima = temperaturas[0];
        for (int i = 0; i < temperaturas.length; i++) {
            if (temperaturas[i] >= maxima) {
                maxima = temperaturas[i];
            }
        }
        return maxima;
    }
    public double temperaturaMinima(){
        double minima = temperaturas[0];
        for(int i = 1; i < temperaturas.length;i++) {
            if (temperaturas[i] < minima){
                minima = temperaturas[i];
            }
        }
        return minima;
    }
    public double promedioTemperaturas(){
        double total = 0;
        double promedio;
        for(int i = 0; i < temperaturas.length;i++){
            total += temperaturas[i];
        }
        promedio = total / temperaturas.length;
        return promedio;
    }
    public void recorrerArreglo(){
        String[] diasSemanal = {"Lunes","Martes","Miercoles","Jueves","Viernes","Sabado","Domingo"};
        for(int i = 0; i < temperaturas.length;i++){
            System.out.println("El dia "+diasSemanal[i]+" hizo "+temperaturas[i]+" Grados");
        }
    }
    public void diasSuperaronUmbral() {
        int diasSuperaron = 0;
        int contador = 0;
        while (temperaturas.length != contador) {
            if (temperaturas[contador] > 30) {
                diasSuperaron++;
            }
            contador++;
        }
        System.out.println("La cantidad de dias que superaron el umbral son: "+diasSuperaron+" Dias");
    }
    public static void main(String[] args){
        double[] temperaturas = {23.4,45.3,34.2,12.45,34.23,2.43,22.33};
        RegistroTemperaturas registroSemanal = new RegistroTemperaturas(temperaturas);

        double promedio = registroSemanal.promedioTemperaturas();
        double maxima = registroSemanal.temperaturaMaxima();
        double minima = registroSemanal.temperaturaMinima();

        System.out.println("El promedio de las temperaturas es de: "+promedio);
        System.out.println("La maxima temperatura es de: "+maxima);
        System.out.println("La minima temperatura es de: "+minima);

        registroSemanal.diasSuperaronUmbral();
        registroSemanal.recorrerArreglo();
    }
}

