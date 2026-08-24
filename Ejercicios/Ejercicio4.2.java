public class main{
    public void main(String[] args) {
        int numero = 18;
        int n = numero;

        int mcd = 1;
        int mcm = n;

        for (int i = 1; i <= n; i++) {
            if (n % i == 0) {
                mcd = i;
            }
        }

        for (int i = n; i <= n; i++) {
            if (i % n == 0) {
                mcm = i;
                break;
            }
        }

        System.out.println("El MCD es: " + mcd);
        System.out.println("El MCM es: " + mcm);

    }
}