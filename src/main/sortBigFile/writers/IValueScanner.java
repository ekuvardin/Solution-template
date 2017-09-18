package main.sortBigFile.writers;

import java.util.Scanner;

/**
 * Interface describing behavior with reading elements
 *
 * @param <T> type of reading elements
 */
public interface IValueScanner<T extends Comparable<T>> {

    /**
     * Check have we next element in scanner
     *
     * @param scanner file reader
     * @return have we next element in scanner
     */
    boolean hasNext(Scanner scanner);

    /**
     * Read next value from reader
     *
     * @param scanner file reader
     * @return next value from reader
     */
    T nextValue(Scanner scanner);
}
