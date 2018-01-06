package sudoku.solvers.dlx;

import sudoku.solvers.Solver;

public class DancingLinksSolver extends Solver {
    /**
     * https://arxiv.org/pdf/cs/0011047.pdf
     *
     * @param board
     */
    public DancingLinksSolver(int[][] board) {
        super(board);
    }

    @Override
    public void findAllSolutions() {
    }

    @Override
    public void findSingleSolution() {
    }
}
