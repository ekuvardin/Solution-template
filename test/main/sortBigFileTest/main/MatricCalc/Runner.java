package main.sortBigFileTest.main.MatricCalc;

import main.Generator;
import main.matrixCalc.CacheLineBound;
import main.matrixCalc.MatrixCalc;
import main.matrixCalc.Simple;
import main.matrixCalc.Transpose;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Runner {

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

        try (Scanner scanner = new Scanner(new File("Out.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        //MatrixCalc matrixCalc = new CacheLineBound();
        MatrixCalc matrixCalc = new Simple();
        long[][] res = matrixCalc.multiplyResult(p1, p2);
        Arrays.equals(res, p3);
    }

    @Test
    public void testTranspose() throws IOException {
        long[][] p1 = new long[128][128];
        long[][] p2 = new long[128][128];

        Generator.main();
        try (Scanner scanner = new Scanner(new File("AvgNumbers.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        }

        MatrixCalc matrixCalcExpected = new Simple();
        MatrixCalc matrixCalcActual = new Transpose();
        Arrays.equals(matrixCalcExpected.multiplyResult(p1, p2),matrixCalcActual.multiplyResult(p1, p2));
    }

    @Test
    public void testCacheLineBound() throws IOException {
        long[][] p1 = new long[128][128];
        long[][] p2 = new long[128][128];

        Generator.main();
        try (Scanner scanner = new Scanner(new File("AvgNumbers.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        }

        MatrixCalc matrixCalcExpected = new Simple();
        MatrixCalc matrixCalcActual = new CacheLineBound(64);
        Arrays.equals(matrixCalcExpected.multiplyResult(p1, p2),matrixCalcActual.multiplyResult(p1, p2));
    }
}
