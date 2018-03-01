package sortBigFile.readers.integerReader;

import sortBigFile.readers.ICompareStrategy;

/**
 * How to compare Integer
 */
public class IntegerCompareStrategy implements ICompareStrategy<Integer> {

    @Override
    public int compareTo(Integer p1, Integer p2){
        return p1.compareTo(p2);
    }
}
