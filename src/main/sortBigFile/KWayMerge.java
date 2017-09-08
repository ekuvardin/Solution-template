package main.sortBigFile;

import java.util.*;


import static sun.java2d.cmm.ColorTransform.In;

public class KWayMerge<T extends Comparable> {

    final int[] pointers;
    final T[] res;

    public KWayMerge(T[]... array) {
        res = array[array.length - 1];

        pointers = new int[array.length - 1];
        Arrays.fill(pointers, 0);
    }

    public void fill(T[]... array) {
        T minMax = findMinElementFromMax(array);

        T min = findMinElement(array);

        int pointer = 0;
        while (min.compareTo(minMax) <= 0 && pointer < res.length) {
            for (int i = 0; i < pointers.length && pointer < res.length; i++) {
                for (int j = 1; j < array.length - 1 && pointer < res.length; j++) {
                    if (array[j][pointers[i]].compareTo(min) == 0) {
                        res[pointer] = array[j][pointers[i]];
                        pointer++;
                        pointers[i] = pointers[i] + 1;
                    }
                }
            }
        }
    }

    private T findMinElement(T[]... array) {
        T min = array[0][pointers[0]];

        for (int i = 1; i < array.length - 1; i++) {
            min = min.compareTo(array[i][pointers[i]]) < 0 ? array[i][pointers[i]] : min;
        }

        return min;
    }

    private T findMinElementFromMax(T[]... array) {
        T min = array[0][array.length];

        for (int i = 1; i < array.length - 1; i++) {
            min = min.compareTo(array[i][array.length]) < 0 ? array[i][array.length] : min;
        }

        return min;
    }
}
