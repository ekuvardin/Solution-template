package main.sortBigFile.writers;

import java.util.Scanner;

public interface IValueScanner<T extends Comparable<T>> {

    boolean hasNext(Scanner scanner);

    T nextValue(Scanner scanner);
}
