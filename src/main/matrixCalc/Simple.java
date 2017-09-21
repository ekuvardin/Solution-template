package main.matrixCalc;

public class Simple extends MatrixCalc {

    @Override
    public void calcResult(long[][] p1, long[][] p2, long[][] res) {
        for (int i = 0; i < p1.length; i++) {
            long colSize = p1[0].length;
            for (int j = 0; j < colSize; j++)
                for (int k = 0; k < colSize; k++)
                    res[i][j] += p1[i][k] * p2[k][j];
        }
    }

    @Override
    public long[][] calcResult(long[][] p1, long[][] p2) {
        long[][] res = new long[p1.length][p2[0].length];

        calcResult(p1, p2, res);
        return res;
    }

}
