//Reading a set of integers stored as binary using buffered input

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class ReadBinaryIntegers {

    public static void main(String[] args) {

        String filename = "random_integers_255.bin";

        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {
            int value;

            while ((value = inputStream.read()) != -1) {
                System.out.println(value);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

    }
}
