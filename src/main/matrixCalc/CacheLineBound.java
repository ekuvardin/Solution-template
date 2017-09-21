package main.matrixCalc;

public class CacheLineBound extends SquareMatrix {

    private final int cacheLineSize;

    public CacheLineBound() {
        this.cacheLineSize = 64;
    }

    public CacheLineBound(int cacheLineSize) {
        if ((cacheLineSize & -cacheLineSize) != cacheLineSize)
            throw new RuntimeException("cacheLineSize must be power of 2");

        this.cacheLineSize = cacheLineSize;
    }

    @Override
    public void calcResult(long[][] p1, long[][] p2, long[][] res) {
        int step = cacheLineSize / 4;

        int colSize = p1[0].length;
        for (int i = 0; i < p1.length; i += step)
            for (int j = 0; j < colSize; j += step)
                for (int k = 0; k < colSize; k += step)
                    for (int i1 = 0; i1 < step; i1++)
                        for (int j1 = 0; j1 < step; j1++)
                            for (int k1 = 0; k1 < step; k1++)
                                res[i1 + i][j1 + j] += p1[i1 + i][k1 + k] * p2[k1 + k][j1 + j];
    }

    @Override
    public long[][] calcResult(long[][] p1, long[][] p2) {
        long[][] res = new long[p1.length][p2[0].length];

        calcResult(p1, p2, res);
        return res;
    }

    @Override
    protected void checkMatrix(long[][] p1, long[][] p2) {
        super.checkMatrix(p1, p2);

        if (p1.length % this.cacheLineSize / 4 != 0 || p2.length % this.cacheLineSize / 4 != 0 || p1[0].length % this.cacheLineSize / 4 != 0) {
            throw new RuntimeException("Array size doesn't multiple by cache line size");
        }
    }
}
