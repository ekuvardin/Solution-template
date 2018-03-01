package sortBigFile.writers.integerWriters;

import sortBigFile.writers.IValueScanner;

import java.util.Scanner;

/**
 * Define how to iterate over Integer elements
 */
public class IntegerScanner implements IValueScanner<Integer> {

    @Override
    public boolean hasNext(Scanner scanner) {
        return scanner.hasNextInt();
    }

    @Override
    public java.lang.Integer nextValue(Scanner scanner) {
        return scanner.nextInt();
    }
}
