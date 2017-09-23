package main.matrixCalc.impl;

/*
 * Matrix multiplication with transformation and line cache chunk
*/
public class TransposeCacheLine extends CacheLineBound {

   public TransposeCacheLine(){
       super();
   }

   public TransposeCacheLine(int cacheLineSize){
       super(cacheLineSize);
   }

    @Override
    public void calcResult(long[][] p1, long[][] p2, long[][] res) {
        final int step = cacheLineSize/LONG_SIZE;

        long[][] tmp = new long[p2[0].length][p2.length];

        for (int i = 0; i < tmp.length; i++)
            for (int j = 0; j < tmp[0].length; j++)
                tmp[i][j] = p2[j][i];

        int colSize = p1[0].length;
        for (int i = 0; i < p1.length; i += step)
            for (int j = 0; j < colSize; j += step)
                for (int k = 0; k < colSize; k += step)
                    for (int i1 = 0; i1 < step; i1++)
                        for (int j1 = 0; j1 < step; j1++)
                            for (int k1 = 0; k1 < step; k1++)
                                res[i1+i][j1+j] += p1[i1+i][k1 + k] * tmp[j1+j][k1 + k];
    }
}
