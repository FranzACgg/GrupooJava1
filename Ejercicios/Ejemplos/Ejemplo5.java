public class main{
    public void main(String[] args){
        int [] array = {1, 2, 3, 3, 4, 4, 5, 2};

        for (int i = 0; i < array.length; i++){
            System.out.println("Índice "+i+": "+array[i]);
        }

        for (int i = 0; i < array.length; i++){
            for (int j = i + 1; j < array.length; j++){
                if ((array[i] == array[j]) && (i != j)){
                    System.out.println("Duplicado encontrado en Indice "+i+": "+array[j]);
                }
            }
        }

    }
}