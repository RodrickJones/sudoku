package sudoku.util;

import java.util.Arrays;

public class MatrixUtil {
    public static int[][] copyOf(int[][] source, int[][] dest) {
        if (dest == null) {
            dest = new int[source.length][];
        }
        for (int i = 0; i < source.length; i++) {
            dest[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return dest;
    }

    public static int[][] copyOf(int[][] source) {
        return copyOf(source, null);
    }
}
