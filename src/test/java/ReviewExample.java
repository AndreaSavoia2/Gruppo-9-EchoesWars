public class ReviewExample {

    /**
     * Verifica se un utente è maggiorenne.
     *
     * Requisito: un utente è maggiorenne
     * a partire dai 18 anni compiuti.
     *
     * @param age età dell'utente
     * @return true se l'utente è maggiorenne
     */
    public static boolean isAdult(int age) {
        return age > 18;
    }

    public static void main(String[] args) {
        System.out.println(isAdult(17));
        System.out.println(isAdult(18));
        System.out.println(isAdult(19));
    }
}
