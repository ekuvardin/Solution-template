package main.sortBigFile.merges;

import main.sortBigFile.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.buffers.SectionWriters;
import main.sortBigFile.readers.ArrayReaderImpl;
import main.sortBigFile.readers.IArrayReader;
import main.sortBigFile.writers.ArrayWriter;
import main.sortBigFile.writers.IArrayWriter;
import main.sortBigFile.writers.IntegerScanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MergeFiles {

    private CyclicBufferHolder cyclicBufferHolder;
    private FileNamesHolder holder;

    public MergeFiles(CyclicBufferHolder cyclicBufferHolder, FileNamesHolder holder){
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.holder = holder;
    }

    public void merge(int size, String outputFileName) throws IOException {
        List<Scanner> scanners = createScanners(size);

        if (scanners.size() == 0) {
            return;
        }

        SectionWriters sectionWriters = new SectionWriters(cyclicBufferHolder, scanners);
        IArrayWriter arrayWriter = new ArrayWriter(sectionWriters, new IntegerScanner());
        IArrayReader arrayReader = new ArrayReaderImpl(sectionWriters);

        String newName = holder.getNewUniqueName(outputFileName);
        try (FileWriter fw = new FileWriter(newName, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)
        ) {
            do {
                arrayWriter.fillBuffer();
                arrayReader.merge(out);
                sectionWriters.tryFreeMemory();
            } while (sectionWriters.getUsedScanners().size() > 0);

            arrayReader.mergeTillEmpty(out);
        } finally{
            sectionWriters.tryFreeMemory();
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
