package main.sortBigFile;

import java.io.*;
import java.util.Random;

/*
  I'm too lazy that way I steal code generator from user
  https://stackoverflow.com/users/57695/peter-lawrey
  from his answer
  https://stackoverflow.com/questions/24959247/java-create-large-text-file-with-random-numbers
 */
public class Generator {
    public static void main(String... ignored) throws FileNotFoundException, UnsupportedEncodingException {
        //Size in Gbs of my file that I want
        double wantedSize = Double.parseDouble(System.getProperty("size", "0.01"));

        Random random = new Random();
        File file = new File("AvgNumbers.txt");
        long start = System.currentTimeMillis();
        PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")), false);
        int counter = 0;
        while (true) {
            String sep = "";
            for (int i = 0; i < 100; i++) {
                int number = random.nextInt(1000000) + 1;
                writer.print(sep);
                writer.print(number);
                sep = " ";
            }
            writer.println();
            //Check to see if the current size is what we want it to be
            if (++counter == 20000) {
                System.out.printf("Size: %.3f GB%n", file.length() / 1e9);
                if (file.length() >= wantedSize * 1e9) {
                    writer.close();
                    break;
                } else {
                    counter = 0;
                }
            }
        }
        long time = System.currentTimeMillis() - start;
        System.out.printf("Took %.1f seconds to create a file of %.3f GB", time / 1e3, file.length() / 1e9);
    }
}
