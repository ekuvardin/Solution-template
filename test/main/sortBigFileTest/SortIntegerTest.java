package main.sortBigFileTest;

import main.Generator;
import main.sortBigFile.sort.SortBigFile;
import main.sortBigFile.readers.integerReader.IntegerCompareStrategy;
import main.sortBigFile.writers.integerWriters.IntegerScanner;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Integration tests for sorting Integer array
 */
public class SortIntegerTest {

    @Test
    public void sortIntegerParallelTest() throws IOException {
        Generator.main("-res AvgNumbers.txt".split(" "));
        // Run example  SortInteger.main() with this arguments
        // -chk 32768 -ct 40 -pl 4 -inpf AvgNumbers.txt -resf Out.txt -pm true
        SortBigFile sortBigFile =
                SortBigFile.createSortBigFile(Integer.class)
                        .setMaxChunkLen(32768)
                        .setMaxCountOfChunks(40)
                        .setPoolSize(4)
                        .setInputFileName("AvgNumbers.txt")
                        .setOutputFileName("Out.txt")
                        .setValueScanner(new IntegerScanner())
                        .setCompareStrategy(new IntegerCompareStrategy())
                        .build();

        sortBigFile.mergeParallel(40 / 4 ,sortBigFile.sortResults());
        Assert.assertTrue(testSortCorrectness(new File("Out.txt")));
    }

    @Test
    public void sortIntegerTest() throws IOException {
        Generator.main("-res AvgNumbers.txt".split(" "));
        // Run example  SortInteger.main() with this arguments
        // -chk 32768 -ct 40 -pl 4 -inpf AvgNumbers.txt -resf Out.txt
        SortBigFile sortBigFile =
                SortBigFile.createSortBigFile(Integer.class)
                        .setMaxChunkLen(32768)
                        .setMaxCountOfChunks(40)
                        .setPoolSize(4)
                        .setInputFileName("AvgNumbers.txt")
                        .setOutputFileName("Out.txt")
                        .setValueScanner(new IntegerScanner())
                        .setCompareStrategy(new IntegerCompareStrategy())
                        .build();

        sortBigFile.merge(sortBigFile.sortResults());
        Assert.assertTrue(testSortCorrectness(new File("Out.txt")));
    }

    private boolean testSortCorrectness(File file) throws IOException {
        try (
                Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.toString())) {
            int prevValue = scanner.hasNextInt() ? scanner.nextInt() : 0;
            while (scanner.hasNextInt()) {
                int currentValue = scanner.nextInt();
                if (prevValue <= currentValue) {
                    prevValue = currentValue;
                } else {
                    System.err.println(String.format("Array was sorted incorrectly. For ex: prev:%d, current:%d", prevValue, currentValue));
                    return false;
                }
            }

        }
        return true;
    }


}
