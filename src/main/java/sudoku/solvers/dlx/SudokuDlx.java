package sudoku.solvers.dlx;

import sudoku.solvers.Solver;
import sudoku.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SudokuDlx extends Solver {

    private final Dlx dlx;

    public SudokuDlx(int[][] board) {
        super(board);
        this.dlx = new Dlx();
    }

    @Override
    public void findAllSolutions() {
        dlx.reset().labels(generateLabels(n))
                .matrix(buildConstraintSets(board))
                .interrupts(null)
                .buildStructure()
                .minimizeBranching(false)
                .search();
        decode();
    }

    @Override
    public void findSingleSolution() {
        dlx.reset().labels(generateLabels(n))
                .matrix(buildConstraintSets(board))
                .interrupts(Collections.singletonList(dlx -> !dlx.getSolutions().isEmpty()))
                .buildStructure()
                .minimizeBranching(false)
                .search();
        decode();
    }

    private void decode() {
        Logger.debug(dlx.getSolutions().size() + " solutions found");
        for (Dlx.Solution solution : dlx.getSolutions()) {
            int[][] decoded = new int[n][n];
            for (List<Object> row : solution.getRows()) {
                int c = -1;
                int r = -1;
                int v = -1;
                for (Object o : row) {
                    if (o instanceof CoordinateLabel) {
                        c = ((CoordinateLabel) o).column;
                        r = ((CoordinateLabel) o).row;
                    } else if (o instanceof ValueLabel) {
                        v = ((ValueLabel) o).value;
                    }
                }
                if (c != -1 && r != -1 && v != -1) {
                    decoded[c][r] = v;
                }
            }
            Logger.debug(Arrays.deepToString(decoded));
            solutions.add(decoded);
        }
    }

    List<Object> generateLabels(final int n) {
        List<Object> labels = new ArrayList<>(n * n * 4);
        for (int c = 0; c < n; c++) {
            for (int r = 0; r < n; r++) {
                labels.add(new CoordinateLabel(c, r));
            }
        }

        for (int i = 0; i < n; i++) {
            for (int c = 0; c < n; c++) {
                labels.add(new ColumnLabel(c + 1));
            }
        }

        for (int i = 0; i < n; i++) {
            for (int r = 0; r < n; r++) {
                labels.add(new RowLabel(r + 1));
            }
        }

        for (int i = 0; i < n; i++) {
            for (int s = 0; s < n; s++) {
                labels.add(new SectorLabel(s + 1));
            }
        }
        return labels;
    }

    boolean[][] buildConstraintSets(int[][] puzzle) {
        final int n = puzzle.length;
        final List<boolean[]> sets = new ArrayList<>(n * n * n);
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                int current = puzzle[c][r];
                if (current != 0) {
                    sets.add(buildConstraintSet(c, r, n, current - 1));
                } else {
                    for (int v = 1; v <= 9; v++) {
                        if (Solver.canSelect(puzzle, c, r, v)) {
                            sets.add(buildConstraintSet(c, r, n, v - 1));
                        }
                    }
                }
            }
        }
        return sets.toArray(new boolean[0][]);
    }

    boolean[] buildConstraintSet(int c, int r, int n, int v) {
        boolean[] set = new boolean[n * n * 4 + 1];
        int sqrtN = (int) Math.sqrt(n);
        //occupied
        set[r * n + c] = true;
        //column contains v
        set[n * n + c * n + v] = true;
        //row contains v
        set[n * n * 2 + r * n + v] = true;
        //sector contains v
        int sector = c / sqrtN + r / sqrtN * sqrtN;
        set[n * n * 3 + sector * n + v] = true;
        return set;
    }


    private abstract class Label {
    }

    private class CoordinateLabel extends Label {
        int column;
        int row;

        CoordinateLabel(int column, int row) {
            this.column = column;
            this.row = row;
        }

        @Override
        public String toString() {
            return "Coordinate(C=" + column + ", R=" + row + ")";
        }
    }

    private class ValueLabel extends Label {
        int value;

        ValueLabel(int value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "(V=" + value + ")";
        }
    }

    private class RowLabel extends ValueLabel {
        RowLabel(int value) {
            super(value);
        }
    }

    private class ColumnLabel extends ValueLabel {
        ColumnLabel(int value) {
            super(value);
        }
    }

    private class SectorLabel extends ValueLabel {
        SectorLabel(int value) {
            super(value);
        }
    }
}
