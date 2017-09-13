package main.sortBigFile;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.buffers.SectionWriters;
import main.sortBigFile.readers.ArrayReaderImpl;
import main.sortBigFile.readers.IArrayReader;
import main.sortBigFile.writers.ArrayWriterImpl;
import main.sortBigFile.writers.IArrayWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MergeFiles {

    public void merge(int start, int end, String outputFileName, Integer[] array, String newName) throws IOException {
        List<Scanner> scanners = createScanners(start, end, outputFileName);

        if (scanners.size() == 0) {
            return;
        }

        CyclicBufferHolder cyclicBufferHolder = new CyclicBufferHolder(array, scanners.size());
        SectionWriters sectionWriters = new SectionWriters(cyclicBufferHolder, scanners);
        IArrayWriter arrayWriter = new ArrayWriterImpl(sectionWriters);
        IArrayReader arrayReader = new ArrayReaderImpl(sectionWriters);

        try (FileWriter fw = new FileWriter(newName, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)
        ) {
            do {
                arrayWriter.fillBuffer();
                arrayReader.merge(out);
                sectionWriters.tryFreeMemory();
            } while (sectionWriters.getUsedScanners().size() > 0);

            arrayReader.mergeTillEnd(out);
        }
    }

    private List<Scanner> createScanners(int start, int end, String outputFileName) throws FileNotFoundException {
        List<Scanner> scanners = new ArrayList<>(end - start + 1);
        for (int fileIndex = start; fileIndex <= end; fileIndex++) {
            File file = new File(outputFileName + fileIndex);
            if (file.exists()) {
                scanners.add(new Scanner(file, StandardCharsets.UTF_8.toString()));
            }
        }

        return scanners;
    }

}
