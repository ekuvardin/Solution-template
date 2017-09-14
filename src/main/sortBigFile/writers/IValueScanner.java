package main.sortBigFile.writers;

import java.util.Scanner;

public interface IValueScanner<T extends Comparable<T>> {

    public boolean hasNext(Scanner scanner);

    public T nextValue(Scanner scanner);
}
