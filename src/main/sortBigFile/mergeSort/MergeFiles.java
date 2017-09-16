package main.sortBigFile.mergeSort;

import main.sortBigFile.readers.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.buffers.SectionWriters;
import main.sortBigFile.readers.ArrayReaderImpl;
import main.sortBigFile.readers.IArrayReader;
import main.sortBigFile.writers.ArrayWriter;
import main.sortBigFile.writers.IArrayWriter;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MergeFiles<T extends Comparable<T>> {

    private CyclicBufferHolder<T> cyclicBufferHolder;
    private FileNamesHolder holder;
    private IValueScanner<T> valueScanner;

    public MergeFiles(CyclicBufferHolder<T> cyclicBufferHolder, FileNamesHolder holder, IValueScanner<T> valueScanner){
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.holder = holder;
        this.valueScanner = valueScanner;
    }

    public void merge(int size, String outputFileName) throws Exception {
        List<Scanner> scanners = createScanners(size);

        if (scanners.size() == 0) {
            return;
        }

        String newName = holder.getNewUniqueName(outputFileName);
        try (FileWriter fw = new FileWriter(newName, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             SectionWriters<T> sectionWriters = new SectionWriters<>(cyclicBufferHolder, scanners);
        ) {
            IArrayWriter arrayWriter = new ArrayWriter<>(sectionWriters, valueScanner);
            IArrayReader arrayReader = new ArrayReaderImpl<>(sectionWriters);

            do {
                arrayWriter.fillBuffer();
                arrayReader.merge(out);
                sectionWriters.tryFreeMemory();
            } while (sectionWriters.getUsedScanners().size() > 0);

            arrayReader.mergeTillEmpty(out);
        } finally{
            holder.pull(newName);
        }
    }

    private List<Scanner> createScanners(int size) throws FileNotFoundException {
        List<Scanner> scanners = new ArrayList<>(size);
        List<String> fileNames = holder.get(size);
        for (String name : fileNames) {
            File file = new File(name);
            if (file.exists()) {
                scanners.add(new Scanner(file, StandardCharsets.UTF_8.toString()));
            }
        }

        return scanners;
    }

}