package matrixCalc;

/**
 * Define matrix multiplication behavior
 */
public abstract class MatrixCalc {

    /**
     * Multiply matrix with check
     *
     * @param p1 left matrix
     * @param p2 right matrix
     * @return result
     */
    public final long[][] multiply(long[][] p1, long[][] p2) {
        checkMatrix(p1, p2);

        return calcResult(p1, p2);
    }

    /**
     * Multiply matrix with check
     *
     * @param p1  left matrix
     * @param p2  right matrix
     * @param res result
     */
    public final void multiply(long[][] p1, long[][] p2, long[][] res) {
        checkMatrix(p1, p2);

        calcResult(p1, p2, res);
    }

    /**
     * Multiply matrix without check
     *
     * @param p1 left matrix
     * @param p2 right matrix
     * @return result
     */
    protected abstract long[][] calcResult(long[][] p1, long[][] p2);

    /**
     * Multiply matrix without check
     *
     * @param p1  left matrix
     * @param p2  right matrix
     * @param res result matrix
     */
    protected abstract void calcResult(long[][] p1, long[][] p2, long[][] res);

    /**
     * Check matrix restrictions on multiply
     *
     * @param p1 left matrix
     * @param p2 right matrix
     * @param p3 result matrix
     */
    protected void checkMatrix(long[][] p1, long[][] p2, long[][] p3) {
        checkMatrix(p1, p2);

        if (p3.length == 0)
            throw new RuntimeException("Array is empty");

        if (p3.length != p1.length || p3[0].length != p2[0].length)
            throw new RuntimeException("Arrays dimensions are not equals");
    }

    /**
     * Check matrix restrictions on multiply
     *
     * @param p1 left matrix
     * @param p2 right matrix
     */
    protected void checkMatrix(long[][] p1, long[][] p2) {
        if (p1.length == 0 || p2.length == 0)
            throw new RuntimeException("Array is empty");

        if (p1[0].length != p2.length)
            throw new RuntimeException("Arrays dimensions are not equals");
    }
}
