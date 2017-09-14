package main.sortBigFile.readers;

import java.io.PrintWriter;

public interface IArrayReader {

    void mergeTillEmpty(PrintWriter out);

    void merge(PrintWriter out);
}
