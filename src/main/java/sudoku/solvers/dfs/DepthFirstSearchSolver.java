package sudoku.solvers.dfs;

import sudoku.solvers.Solver;
import sudoku.util.MatrixUtil;

import java.util.stream.IntStream;

public class DepthFirstSearchSolver extends Solver {
    private final int n;
    private final int sqrtN;
    private boolean interrupt;

    public DepthFirstSearchSolver(int[][] board) {
        super(board);
        this.n = board.length;
        this.sqrtN = (int) Math.sqrt(n);
    }

    @Override
    public void findAllSolutions() {
        interrupt = false;
        solve(0, 0, false);
    }

    @Override
    public void findSingleSolution() {
        interrupt = false;
        solve(0, 0, true);
    }

    private void solve(int c, int r, boolean findSingle) {
        if (interrupt) {
            return;
        }
        if (c == n) {
            solutions.add(MatrixUtil.copyOf(board));
            if (findSingle) {
                interrupt = true;
            }
        } else if (board[c][r] != 0) {
            solve(c + r / (n - 1), (r + 1) % n, findSingle);
        } else {
            IntStream.rangeClosed(1, n)
                    .filter(i -> canSelect(c, r, i))
                    .forEach(i -> {
                        board[c][r] = i;
                        solve(c + r / (n - 1), (r + 1) % n, findSingle);
                        board[c][r] = 0;
                    });
        }
    }

    private boolean canSelect(int column, int row, int value) {
        //check column and row
        for (int i = 0; i < n; i++) {
            if (board[column][i] == value
                    || board[i][row] == value) {
                return false;
            }
        }
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
}
