package main.sortBigFile;

import java.util.Scanner;

public class MergeReaderImpl implements MergeReader {

    private final Scanner scanner;
    private final Integer[] buffer;
    private int curSize;
    private int pointer;

    public MergeReaderImpl(Scanner scanner, Integer[] buffer) {
        this.scanner = scanner;
        this.buffer = buffer;
    }

    @Override
    public int fillBuffer(int size, int pointer) {
        int i = 0;
        while (scanner.hasNext() && i < size) {
            buffer[pointer + i] = scanner.nextInt();
            ++i;
        }
        this.curSize = i;
        this.pointer = pointer;
        return i;
    }

    @Override
    public int readUntil(Integer s) {
        int cnt = 0;
        /*while (!buffer.isEmpty() && s.compareTo(buffer.getFirst()) <= 0) {
            buffer.removeFirst();
            cnt++;
        }*/

        return cnt;
    }

    @Override
    public Integer first() {
        return buffer[pointer];
    }

    @Override
    public Integer Last() {
        return buffer[pointer + curSize - 1];
    }

    @Override
    public boolean isEmpty() {
        return curSize == 0 || !scanner.hasNext();
    }

    @Override
    public int getSize() {
        return curSize;
    }

    @Override
    public void close() throws Exception {
        scanner.close();
    }
}
