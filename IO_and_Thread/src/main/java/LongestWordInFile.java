//Find the longest word in a text file

import java.io.*;
import java.util.Scanner;

public class LongestWordInFile {

    public static void main(String[] args) {

        String filename = "textfile.txt";
        String longestWord = "";

        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNext()) {
                String word = scanner.next();
                if (word.length() > longestWord.length()) {
                    longestWord = word;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + filename);
        }

        System.out.println("Parola più lunga: " + longestWord);
    }

}
