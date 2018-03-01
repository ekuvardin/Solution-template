package matrixCalc;

/**
 * Define square matrix restrictions
 */
public abstract class SquareMatrix extends MatrixCalc {

    @Override
    protected void checkMatrix(long[][] p1, long[][] p2) {
        if(!(p1.length == p1[0].length && p2.length == p2[0].length && p1.length == p2.length)){
            throw new RuntimeException("Not square matrix");
        }

        super.checkMatrix(p1,p2);
    }

}
