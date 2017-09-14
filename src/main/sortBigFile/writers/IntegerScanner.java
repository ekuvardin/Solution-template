package main.sortBigFile.writers;

import java.util.Scanner;

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
