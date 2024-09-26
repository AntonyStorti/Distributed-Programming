import java.util.LinkedList;
import java.util.Queue;

class ParkingLot {

    private static final int MAX_PARKING_SPACES = 5;  // Numero massimo di posti nel parcheggio
    private Queue<Integer> parkingSpaces = new LinkedList<>();  // Buffer condiviso (parcheggio)
    private final Object lock = new Object();  // Oggetto di lock per la sincronizzazione

    // Classe Car
    class Car extends Thread {
        private int carId;

        Car(int id) {
            this.carId = id;
        }

        @Override
        public void run() {
            try {
                // Tentativo di parcheggio
                park();

                // Tempo di parcheggio (fino a 10 secondi)
                Thread.sleep((int) (Math.random() * 10000));  // Tempo di parcheggio variabile tra 0 e 10 secondi

                // Uscita dal parcheggio
                leave();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void park() throws InterruptedException {
            synchronized (lock) {
                // Attende finché non ci sono posti disponibili
                while (parkingSpaces.size() == MAX_PARKING_SPACES) {
                    System.out.println("Macchina " + carId + " in attesa per parcheggiare...");
                    lock.wait();  // Rilascia il lock e attende fino a quando viene notificato
                }

                // Parcheggia la macchina
                parkingSpaces.add(carId);
                System.out.println("Macchina " + carId + " parcheggiata.");

                // Notifica altre macchine che potrebbero essere in attesa
                lock.notifyAll();
            }
        }

        private void leave() throws InterruptedException {
            synchronized (lock) {
                // Rimuove la macchina dal parcheggio
                parkingSpaces.remove(carId);
                System.out.println("Macchina " + carId + " ha lasciato il parcheggio.");

                // Notifica altre macchine che potrebbero essere in attesa
                lock.notifyAll();
            }
        }
    }

    // Avvia la simulazione
    public void startSimulation() throws InterruptedException {
        // Crea e avvia 10 macchine
        for (int i = 1; i <= 10; i++) {
            Car car = new Car(i);
            car.start();

            // Le macchine cercano di parcheggiare con una certa attesa (fino a 20 secondi)
            Thread.sleep((int) (Math.random() * 20000));
        }
    }

    public static void main(String[] args) {
        ParkingLot parkingLot = new ParkingLot();
        try {
            parkingLot.startSimulation();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
