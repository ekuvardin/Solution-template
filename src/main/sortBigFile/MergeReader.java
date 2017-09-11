package main.sortBigFile;

public interface MergeReader extends AutoCloseable {

    public int fillBuffer(int size, int pointer);

    public int readUntil(Integer s);

    public Integer first();

    public Integer Last();

    public boolean isEmpty();

    public int getSize();

}
