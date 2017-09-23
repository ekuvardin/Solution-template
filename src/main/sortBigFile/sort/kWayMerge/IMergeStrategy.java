package main.sortBigFile.sort.kWayMerge;

import java.io.IOException;
import java.util.List;

public interface IMergeStrategy {

    public String merge(final List<String> fileNames, String outputFilePath);

}
