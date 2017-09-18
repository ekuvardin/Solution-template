package main.sortBigFile.readers;

import java.io.PrintWriter;

/**
 * Read arrays, make k-way merge and write to file
 */
public interface IMergeArrayReader {

    /**
     * Read arrays till all of tem become empty
     *
     * @param out file writer
     */
    void mergeTillEmpty(PrintWriter out);

    /**
     * Read arrays till read all values not greater than minimum from last element in all arrays
     *
     * @param out file writer
     */
    void mergeTillMinMax(PrintWriter out);
}
