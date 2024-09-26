import java.util.LinkedList;
import java.util.Queue;


class Pizzeria {

    public static void main(String[] args) {
        Pizzeria pizzeria = new Pizzeria();
        pizzeria.startWorking();
    }

    private static final int MAX_PIZZAS = 5;  // Numero massimo di pizze che il bancone può contenere

    private Queue<String> bancone = new LinkedList<>();  // Buffer condiviso (bancone)
    private final Object lock = new Object();  // Oggetto di lock per la sincronizzazione



    class Pizzaiolo extends Thread {

        @Override
        public void run() {
            int contaPizze = 1;
            try {
                while (true) {
                    // Produzione della pizza (questa operazione potrebbe richiedere tempo)
                    String pizza = "Pizza " + contaPizze++;
                    Thread.sleep((int) (Math.random() * 3000));  // Simulazione del tempo di preparazione della pizza

                    synchronized (lock) {
                        // Attende finché il bancone è pieno
                        while (bancone.size() == MAX_PIZZAS) {
                            System.out.println("Bancone pieno! Il pizzaiolo è in attesa...");
                            lock.wait();  // Rilascia il lock e attende fino a quando viene notificato
                        }

                        // Aggiunge la pizza al bancone
                        bancone.add(pizza);
                        System.out.println("Il pizzaiolo ha prodotto: " + pizza);

                        // Notifica il cameriere (consumatore) che una pizza è disponibile
                        lock.notify();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }



    class Cameriere extends Thread {

        @Override
        public void run() {
            try {
                while (true) {
                    synchronized (lock) {
                        // Attende finché il bancone è vuoto
                        while (bancone.isEmpty()) {
                            System.out.println("Bancone vuoto! Il cameriere è in attesa...");
                            lock.wait();  // Rilascia il lock e attende fino a quando viene notificato
                        }

                        // Prende una pizza dal bancone
                        String pizza = bancone.poll();
                        System.out.println("Il cameriere ha servito: " + pizza);

                        // Notifica il pizzaiolo (produttore) che c'è uno spazio libero
                        lock.notify();
                    }

                    // Simula il tempo di servizio della pizza
                    Thread.sleep((int) (Math.random() * 3000));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Avvia la simulazione
    public void startWorking() {
        Pizzaiolo pizzaiolo = new Pizzaiolo();
        Cameriere cameriere = new Cameriere();

        pizzaiolo.start();
        cameriere.start();
    }

}
