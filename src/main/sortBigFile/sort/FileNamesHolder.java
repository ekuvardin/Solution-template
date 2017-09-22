package main.sortBigFile.sort;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds all files which system generate.
 */
public final class FileNamesHolder {

    private static AtomicInteger lastGen = new AtomicInteger();

    /**
     * Generate new unique name for file based on prefix
     *
     * @param name common file prefix
     * @return new unique name for file
     */
    public static String getNewUniqueName(String name) {
        return name + lastGen.incrementAndGet();
    }
}