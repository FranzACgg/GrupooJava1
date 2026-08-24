public class main{
    public void main(String[] args) {
        int[] array = {1,2,3,4,5,6,7};
        int maximo = array[0];
        int minimo = array[0];
        for (int i = 1; i < array.length; i++){
            if (array[i] > maximo){
                maximo = array[i];
            }
            if (array[i] < minimo){
                minimo = array[i];
            }
        }
        System.out.println(maximo);
        System.out.println(minimo);
    }
}