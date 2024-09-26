//Read strings from a file into an ArrayList

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class StringsFromFile {

    public static void main(String[] args) {

        String filename = "strings.txt";
        ArrayList<String> strings = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                strings.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        }

        System.out.println("Strings read from file: " + strings);
    }

}
