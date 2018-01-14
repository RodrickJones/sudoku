package sudoku.solvers.dfs;

import sudoku.solvers.Solver;
import sudoku.util.MatrixUtil;

import java.util.stream.IntStream;

public class DepthFirstSearchSolver extends Solver {
    private boolean interrupt;

    public DepthFirstSearchSolver(int[][] board) {
        super(board);
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
                    .filter(i -> canSelect(board, c, r, i))
                    .forEach(i -> {
                        board[c][r] = i;
                        solve(c + r / (n - 1), (r + 1) % n, findSingle);
                        board[c][r] = 0;
                    });
        }
    }
}
