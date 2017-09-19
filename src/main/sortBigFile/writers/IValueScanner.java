package main.sortBigFile.writers;

import java.util.Scanner;

/**
 * Define how to iterate over <T> elements
 *
 * @param <T> type of reading elements
 */
public interface IValueScanner<T> {

    /**
     * Check have we next element in scanner
     *
     * @param scanner file reader
     * @return have we next element in scanner
     */
    boolean hasNext(Scanner scanner);

    /**
     * Read next value from scanner
     *
     * @param scanner file reader
     * @return next value from scanner
     */
    T nextValue(Scanner scanner);
}
