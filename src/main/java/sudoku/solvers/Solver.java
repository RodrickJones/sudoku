package sudoku.solvers;


import sudoku.util.MatrixUtil;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Solver {
    protected final int[][] board;
    protected final ArrayList<int[][]> solutions = new ArrayList<>();

    public Solver(int[][] board) {
        this.board = MatrixUtil.copyOf(board);
    }

    public static boolean[][][] calculateDomains(int[][] board, boolean[][][] domains) {
        //Declaration and initialization
        if (domains == null) {
            domains = new boolean[9][9][10];
        }
        boolean[][] rowDomains = new boolean[9][10];
        boolean[][] colDomains = new boolean[9][10];
        boolean[][] secDomains = new boolean[9][10];
        boolean[] TRUE = new boolean[10];
        Arrays.fill(TRUE, true);
        for (boolean[][] mat : domains) {
            for (boolean[] arr : mat) {
                System.arraycopy(TRUE, 0, arr, 0, 10);
            }
        }

        for (int i = 0; i < 9; i++) {
            System.arraycopy(TRUE, 0, rowDomains[i], 0, 10);
            System.arraycopy(TRUE, 0, colDomains[i], 0, 10);
            System.arraycopy(TRUE, 0, secDomains[i], 0, 10);
        }

        //Building the row/col/sec domains
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 9; r++) {
                int value = board[c][r];
                if (value == 0) {
                    continue;
                }
                //false indicates that the value has been seen
                rowDomains[r][value] = false;
                colDomains[c][value] = false;
                secDomains[c / 3 + r / 3 * 3][value] = false;
            }
        }

        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 9; r++) {
                boolean[] domain = domains[c][r];
                boolean[] row = rowDomains[r];
                boolean[] col = colDomains[c];
                boolean[] sec = secDomains[c / 3 + r / 3 * 3];
                for (int i = 1; i < 10; i++) {
                    if (i == board[c][r]) {
                        continue;
                    }
                    domain[i] = row[i] && col[i] && sec[i];
                }
            }
        }

        return domains;
    }

    public static boolean[][][] calculateDomains(int[][] board) {
        return calculateDomains(board, null);
    }

    /**
     * Solve the given board, storing all found solutions in the solutions collection
     */
    public abstract void findAllSolutions();

    public abstract void findSingleSolution();

    public void reset(int[][] board) {
        MatrixUtil.copyOf(board, this.board);
        solutions.clear();
    }

    public int[][] getSolution() {
        return getSolution(0);
    }

    public int[][] getRandomSolution() {
        return getSolution((int) (Math.random() * solutionCount()));
    }

    public int[][] getSolution(int index) {
        if (index > solutions.size()) {
            return null;
        }
        return solutions.get(index);
    }

    public int solutionCount() {
        return solutions.size();
    }

    public boolean isSolvable() {
        return solutionCount() > 0;
    }

    public boolean isUnique() {
        return solutionCount() == 1;
    }
}
