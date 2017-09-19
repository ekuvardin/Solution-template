package main.sortBigFile.readers;

public interface ICompareStrategy<T> {

    public int compareTo(T p1, T p2);
}
