package main.sortBigFile;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import main.sortBigFile.sort.SortBigFile;
import main.sortBigFile.readers.integerReader.IntegerCompareStrategy;
import main.sortBigFile.writers.integerWriters.IntegerScanner;

import java.util.List;

/**
 * Runner for sort integer array
 */
public class SortInteger {

    /**
     * Arguments for program
     *
     * @param args argument of comand line
     */
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();

        OptionSpec<Integer> maxChunkLen = parser.accepts("chk", "Max chunk size.")
                .withRequiredArg().ofType(Integer.class).required().describedAs("chk");

        OptionSpec<Integer> maxCountOfChunks = parser.accepts("ct", "Max count of chunk.")
                .withRequiredArg().ofType(Integer.class).required().describedAs("ct");

        OptionSpec<Integer> poolSize = parser.accepts("pl", "Max pool size.")
                .withRequiredArg().ofType(Integer.class).required().describedAs("pl");

        OptionSpec<String> inputFile = parser.accepts("inpf", "Name of input file.")
                .withRequiredArg().ofType(String.class).required().describedAs("inpf");

        OptionSpec<String> outputResultFile = parser.accepts("resf", "Name of result file.")
                .withRequiredArg().ofType(String.class).required().describedAs("resf");

        OptionSpec<Boolean> useParallelMerge = parser.accepts("pm", "Use parallel merge while sorting.")
                .withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE).describedAs("pm");

        OptionSet set;

        try {
            set = parser.parse(args);
        } catch (OptionException e) {
            System.err.println("ERROR: " + e.getMessage());
            System.err.println();
            return;
        }

        SortBigFile<Integer> sortBigFile =
                SortBigFile.createSortBigFile(Integer.class)
                        .setMaxChunkLen(set.valueOf(maxChunkLen))
                        .setMaxCountOfChunks(set.valueOf(maxCountOfChunks))
                        .setPoolSize(set.valueOf(poolSize))
                        .setInputFileName(set.valueOf(inputFile))
                        .setOutputFileName(set.valueOf(outputResultFile))
                        .setValueScanner(new IntegerScanner())
                        .setCompareStrategy(new IntegerCompareStrategy())
                        .userParallelMerge(set.valueOf(useParallelMerge))
                        .build();

        sortBigFile.sortResults();
    }
}
