package main.sortBigFileTest.main.MatricCalc;

import main.matrixCalc.MatrixCalc;
import main.matrixCalc.Simple;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Runner {

    public static void main(String[] args){
        long[][] p1 = new long[1024][1024];

        long[][] p2 = new long[1024][1024];
        try( Scanner scanner = new Scanner(new File("Out.txt"), StandardCharsets.UTF_8.toString())){
            for(int i=0;i<p1.length;i++){
                for(int j=0;j<p1[0].length && scanner.hasNextLong();j++){
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }


        //MatrixCalc matrixCalc = new CacheLineBound();
        MatrixCalc matrixCalc = new Simple();
        long[][] res = matrixCalc.multiplyResult(p1,p2);
    }
}
