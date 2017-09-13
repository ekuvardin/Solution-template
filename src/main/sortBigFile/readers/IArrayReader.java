package main.sortBigFile.readers;

import java.io.PrintWriter;

public interface IArrayReader {

    void mergeTillEnd(PrintWriter out);

    void merge(PrintWriter out);
}
