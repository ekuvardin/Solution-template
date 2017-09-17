package main.sortBigFileTest;

import main.sortBigFile.Generator;
import main.sortBigFile.SortInteger;
import main.sortBigFile.mergeSort.SortBigFile;
import main.sortBigFile.writers.IntegerScanner;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class SortIntegerTest {

    @Test
    public void sortIntegerParallelTest() throws IOException {
        Generator.main();
        // Run example  SortInteger.main() with this arguments
        // -chk 32768 -ct 40 -pl 4 -inpf AvgNumbers.txt -resf Out.txt -pm true
        SortBigFile sortBigFile = new SortBigFile<>(
                32768,
                40,
                4,
                "AvgNumbers.txt",
                "Out.txt",
                Integer.class,
                new IntegerScanner());

        sortBigFile.sortResults();
        sortBigFile.mergeParallel(40 / 4);
        Assert.assertTrue(testSortCorrectness(new File("Out.txt")));
    }

    @Test
    public void sortIntegerTest() throws IOException {
        Generator.main();
        // Run example  SortInteger.main() with this arguments
        // -chk 32768 -ct 40 -pl 4 -inpf AvgNumbers.txt -resf Out.txt
        SortBigFile sortBigFile = new SortBigFile<>(
                32768,
                40,
                4,
                "AvgNumbers.txt",
                "Out.txt",
                Integer.class,
                new IntegerScanner());

        sortBigFile.sortResults();
        sortBigFile.merge();
        Assert.assertTrue(testSortCorrectness(new File("Out.txt")));
    }

    private boolean testSortCorrectness(File file) throws IOException {
        try (
                Scanner scanner = new Scanner(file, StandardCharsets.UTF_8.toString())) {
            int prevValue = scanner.hasNextInt() ? scanner.nextInt() : 0;
            while (scanner.hasNextInt()) {
                int currentValue = scanner.nextInt();
                if(prevValue <= currentValue){
                    prevValue = currentValue;
                } else {
                    System.err.println(String.format("Array was sorted incorrectly. For ex: prev:%d, current:%d",prevValue,currentValue));
                    return false;
                }
            }

        }
        return true;
    }


}
