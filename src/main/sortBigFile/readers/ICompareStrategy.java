package main.sortBigFile.readers;

/**
 * How to compare elements
 *
 * @param <T> type of sorting elements
 */
public interface ICompareStrategy<T> {

    /**
     * Compare elements
     *
     * @param p1 first element in compare
     * @param p2 second element in compare
     * @return 1    :   p1>p2
             * 0    :   p1 ==p2
             * -1   :   p1<p2
     */
    public int compareTo(T p1, T p2);
}
