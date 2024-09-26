//Reading a set of integers from a text file using buffered input

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadTextIntegers {

    public static void main(String[] args) {

        String filename = "random_integers.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    int number = Integer.parseInt(line.trim());
                    System.out.println(number);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
