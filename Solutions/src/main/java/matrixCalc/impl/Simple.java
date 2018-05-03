package matrixCalc.impl;

import matrixCalc.MatrixCalc;

/*
 * Simple matrix multiplication
*/
public class Simple extends MatrixCalc {

    @Override
    public void calcResult(long[][] p1, long[][] p2, long[][] res) {
        for (int i = 0, colSize = p1[0].length; i < p1.length; i++) {
            for (int j = 0; j < colSize; j++) {
                long acc = 0;
                for (int k = 0; k < colSize; k++)
                    acc += p1[i][k] * p2[k][j];
                res[i][j] = acc;
            }
        }
    }

    @Override
    public long[][] calcResult(long[][] p1, long[][] p2) {
        long[][] res = new long[p1.length][p2[0].length];

        calcResult(p1, p2, res);
        return res;
    }

}
