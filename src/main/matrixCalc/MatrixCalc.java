package main.matrixCalc;

public abstract class MatrixCalc {

    public final long[][] multiplyResult(long[][] p1, long[][] p2) {
        checkMatrix(p1, p2);

        return calcResult(p1, p2);
    }

    public final void multiplyResult(long[][] p1, long[][] p2, long[][] res) {
        checkMatrix(p1, p2);

        calcResult(p1, p2, res);
    }

    protected abstract long[][] calcResult(long[][] p1, long[][] p2);

    protected abstract void calcResult(long[][] p1, long[][] p2, long[][] res);

    protected void checkMatrix(long[][] p1, long[][] p2, long[][] p3) {
        checkMatrix(p1, p2);

        if (p3.length == 0)
            throw new RuntimeException("Array is empty");

        if (p3.length != p1.length || p3[0].length != p2[0].length)
            throw new RuntimeException("Arrays dimensions are not equals");
    }

    protected void checkMatrix(long[][] p1, long[][] p2) {
        if (p1.length == 0 || p2.length == 0)
            throw new RuntimeException("Array is empty");

        if (p1[0].length != p2.length)
            throw new RuntimeException("Arrays dimensions are not equals");
    }
}
