package sudoku.solvers;


import sudoku.util.MatrixUtil;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Solver {
    protected final int[][] board;
    protected final int n;
    protected final ArrayList<int[][]> solutions = new ArrayList<>();

    public Solver(int[][] board) {
        this.board = MatrixUtil.copyOf(board);
        this.n = board.length;
    }

    public static boolean[][][] calculateDomains(final int[][] board, boolean[][][] domains, final int n) {
        //Declaration and initialization
        if (domains == null) {
            domains = new boolean[n][n][n + 1];
        }
        boolean[][] rowDomains = new boolean[n][n + 1];
        boolean[][] colDomains = new boolean[n][n + 1];
        boolean[][] secDomains = new boolean[n][n + 1];
        boolean[] TRUE = new boolean[n + 1];
        Arrays.fill(TRUE, true);
        for (boolean[][] mat : domains) {
            for (boolean[] arr : mat) {
                System.arraycopy(TRUE, 0, arr, 0, n + 1);
            }
        }

        for (int i = 0; i < n; i++) {
            System.arraycopy(TRUE, 0, rowDomains[i], 0, n + 1);
            System.arraycopy(TRUE, 0, colDomains[i], 0, n + 1);
            System.arraycopy(TRUE, 0, secDomains[i], 0, n + 1);
        }

        //Building the row/col/sec domains
        int sqrtN = (int) Math.sqrt(n);
        for (int c = 0; c < n; c++) {
            for (int r = 0; r < n; r++) {
                int value = board[c][r];
                if (value == 0) {
                    continue;
                }
                //false indicates that the value has been seen
                rowDomains[r][value] = false;
                colDomains[c][value] = false;
                secDomains[c / sqrtN + r / sqrtN * sqrtN][value] = false;
            }
        }

        for (int c = 0; c < n; c++) {
            for (int r = 0; r < n; r++) {
                boolean[] domain = domains[c][r];
                boolean[] row = rowDomains[r];
                boolean[] col = colDomains[c];
                boolean[] sec = secDomains[c / sqrtN + r / sqrtN * sqrtN];
                for (int i = 1; i < n + 1; i++) {
                    if (i == board[c][r]) {
                        continue;
                    }
                    domain[i] = row[i] && col[i] && sec[i];
                }
            }
        }

        return domains;
    }

    public static boolean canSelect(int[][] board, int column, int row, int value) {
        final int n = board.length;
        //check column and row
        for (int i = 0; i < n; i++) {
            if (board[column][i] == value
                    || board[i][row] == value) {
                return false;
            }
        }
        final int sqrtN = (int) Math.sqrt(n);
        //check sector
        int colStart = column / sqrtN * sqrtN;
        int rowStart = row / sqrtN * sqrtN;
        for (int c = colStart; c < colStart + sqrtN; c++) {
            for (int r = rowStart; r < rowStart + sqrtN; r++) {
                if (board[c][r] == value) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Solve the given board, storing all found solutions in the solutions collection
     */
    public abstract void findAllSolutions();

    public abstract void findSingleSolution();

    public int[][] getSolution() {
        return getSolution(0);
    }

    public int[][] getRandomSolution() {
        return getSolution((int) (Math.random() * solutionCount()));
    }

    public int[][] getSolution(int index) {
        return solutions.size() > index ? solutions.get(index) : null;
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
