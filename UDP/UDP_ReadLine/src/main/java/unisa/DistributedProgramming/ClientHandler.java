package unisa.DistributedProgramming;
import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Leggi il filename
            String filename = in.readLine();

            // Leggi il numero di righe
            int lineNumber = Integer.parseInt(in.readLine());

            File file = new File(filename);

            if (!file.exists()) {
                out.println("404: FILE NOT FOUND");
            } else {

                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {

                    String line;
                    int currentLine = 1;
                    boolean lineFound = false;

                    // Leggi fino al numero di riga richiesto
                    while ((line = fileReader.readLine()) != null) {
                        if (currentLine == lineNumber) {
                            out.println(line);
                            lineFound = true;
                            break;
                        }
                        currentLine++;
                    }

                    if (!lineFound) {
                        out.println("500: LINE ERROR");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Invalid line number format");
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
