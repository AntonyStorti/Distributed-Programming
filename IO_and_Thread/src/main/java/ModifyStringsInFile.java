//Allow modification of strings in the file

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ModifyStringsInFile {

    public static void main(String[] args) {

        String filename = "strings.txt";
        ArrayList<String> strings = new ArrayList<>();

        // Reading strings from file
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                strings.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        }

        // Modify strings as desired (example: append " modified" to each string)
        strings.replaceAll(s -> s + " modified");

        // Writing modified strings back to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String str : strings) {
                writer.write(str);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        System.out.println("Modified strings saved to file.");
    }

}
