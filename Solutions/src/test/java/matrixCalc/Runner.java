package matrixCalc;


import matrixCalc.MatrixCalc;
import matrixCalc.impl.CacheLineBound;
import matrixCalc.impl.Simple;
import matrixCalc.impl.Transpose;
import matrixCalc.impl.TransposeCacheLine;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import generator.Generator;

/**
 * Integration tests for matrix multiply
 */
public class Runner {

    @BeforeClass
    public static void beforeTests() throws FileNotFoundException, UnsupportedEncodingException {
        Generator.main("-res AvgNumbers.txt".split(" "));
    }

    @Test
    public void testSimple() {
        long[][] p1 = new long[][]{
                {1, 2, 3, 21}, {4, 5, 6, 4}, {7, 7, 9, 2}, {4, 4, 4, 4}
        };

        long[][] p2 = new long[][]{
                {234, 3, 3, 1}, {34, 34, 3, 4}, {3, 3, 3, 1}, {21, 1, 1, 1}
        };

        long[][] p3 = new long[][]{
                {752, 101, 39, 33}, {1208, 204, 49, 34}, {1945, 288, 71, 46}, {1168, 164, 40, 28}
        };

        //MatrixCalc matrixCalc = new CacheLineBound();
        MatrixCalc matrixCalc = new Simple();
        long[][] res = matrixCalc.multiply(p1, p2);
        Assert.assertArrayEquals(res, p3);
    }

    @Test
    public void testTranspose() throws IOException {
        long[][] p1 = new long[128][128];
        long[][] p2 = new long[128][128];

        try (Scanner scanner = new Scanner(new File("AvgNumbers.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        }

        MatrixCalc matrixCalcExpected = new Simple();
        MatrixCalc matrixCalcActual = new Transpose();
        Assert.assertArrayEquals(matrixCalcExpected.multiply(p1, p2),matrixCalcActual.multiply(p1, p2));
    }

    @Test
    public void testCacheLineBound() throws IOException {
        long[][] p1 = new long[128][128];
        long[][] p2 = new long[128][128];

        try (Scanner scanner = new Scanner(new File("AvgNumbers.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        }

        MatrixCalc matrixCalcExpected = new Simple();
        MatrixCalc matrixCalcActual = new CacheLineBound(64);
        Assert.assertArrayEquals(matrixCalcExpected.multiply(p1, p2),matrixCalcActual.multiply(p1, p2));
    }

    @Test
    public void testTransposeCacheLineBound() throws IOException {
        long[][] p1 = new long[128][128];
        long[][] p2 = new long[128][128];

        try (Scanner scanner = new Scanner(new File("AvgNumbers.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        }

        MatrixCalc matrixCalcExpected = new Simple();
        MatrixCalc matrixCalcActual = new TransposeCacheLine(64);
        Arrays.equals(matrixCalcExpected.multiply(p1, p2),matrixCalcActual.multiply(p1, p2));
    }
}
