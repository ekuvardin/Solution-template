package matrixCalc.impl;

import matrixCalc.MatrixCalc;

/*
 * Matrix multiplication with transformation
*/
public class Transpose extends MatrixCalc {

    @Override
    public void calcResult(long[][] p1, long[][] p2, long[][] res) {
        long[][] tmp = new long[p2[0].length][p2.length];

        for (int i = 0; i < tmp.length; i++)
            for (int j = 0; j < tmp[0].length; j++)
                tmp[i][j] = p2[j][i];

        for (int i = 0, colSize = p1[0].length; i < p1.length; i++) {
            for (int j = 0; j < colSize; j++) {
                long acc = 0;
                for (int k = 0; k < colSize; k++)
                    acc += p1[i][k] * tmp[j][k];
                res[i][j] = acc;
            }
        }
    }

    @Override
    public long[][] calcResult(long[][] p1, long[][] p2){
        long[][] res = new long[p1.length][p2[0].length];
        calcResult(p1, p2, res);
        return res;
    }

}
