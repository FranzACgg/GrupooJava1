public class main{
    public void main(String[] args){
        int numero1 = 220;
        int numero2 = 284;
        int suma1 = 0;
        int suma2 = 0;
        for (int i = 1; i <= (numero1 / 2); i++){
            if (numero1 % i == 0){
                suma1 = suma1 + i;
            }
        }
        for (int i = 1; i <= (numero2 / 2); i++){
            if (numero2 % i == 0){
                suma2 = suma2 + i;
            }
        }
        if ((suma2 == numero1) && (suma1 == numero2)){
            System.out.println(numero1 + " y "+ numero2 + " son amigos.");
        } else {
            System.out.println(numero1 + " y " + numero2 + " no son amigos.");
        }

    }
}