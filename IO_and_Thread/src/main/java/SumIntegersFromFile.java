// Print the sum of a list of integers, handle exceptions, and print partial sum
// if exceptions occur//

import java.io.*;
import java.util.Scanner;

public class SumIntegersFromFile {

    public static void main(String[] args) {

        String filename = "random_integers.txt";
        int sum = 0;

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                try {
                    int num = Integer.parseInt(line.trim());
                    sum += num;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format in line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File Error: " + filename);
        }

        System.out.println("Somma: " + sum);
    }

}
