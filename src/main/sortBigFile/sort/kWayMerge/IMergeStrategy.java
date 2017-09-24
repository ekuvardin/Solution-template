package main.sortBigFile.sort.kWayMerge;

import java.util.List;

/**
 * How to merge files
 */
public interface IMergeStrategy {

    /**
     * merge fileNames to one file
     *
     * @param fileNames      files which merged
     * @param outputFilePath path when files are placed
     * @return merged fileNames
     */
    String merge(final List<String> fileNames, String outputFilePath);

}
