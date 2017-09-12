package main.sortBigFile;

import java.io.*;
import java.util.*;

public class MergeFiles {

    public void merge(int start, int end, String outputFileName, Integer[] array, String newName) throws IOException {
        List<Scanner> scanners = new ArrayList<>(end - start + 1);
        for (int fileIndex = start; fileIndex <= end; fileIndex++) {
            File file = new File(outputFileName + fileIndex);
            if (file.exists()) {
                scanners.add(new Scanner(file));
            }
        }

        if(scanners.size()==0){
            return;
        }
        CyclicBufferHolder cyclicBufferArray = new CyclicBufferHolder(array, scanners.size());

        Sections section = new Sections(scanners, cyclicBufferArray);


        MergeArrayToFiles mergeArrayToFiles = new MergeArrayToFiles(section);

        try (FileWriter fw = new FileWriter(newName, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             MultipleArrayWriter multipleArrayWriter = new MultipleArrayWriter(section);
        ) {
            do {
                multipleArrayWriter.fillBuffer();
                mergeArrayToFiles.merge(out);
            } while (section.getUsedSections().size() > 0);

            mergeArrayToFiles.mergeTillEnd(out);
        }


    }




}
