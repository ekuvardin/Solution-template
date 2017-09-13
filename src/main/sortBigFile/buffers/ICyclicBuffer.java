package main.sortBigFile.buffers;

public interface ICyclicBuffer {
    Integer getFirst();

    Integer getLast();

    int getSize();

    int getCapacity();

    void put(Integer value);

    Integer pull();
}
