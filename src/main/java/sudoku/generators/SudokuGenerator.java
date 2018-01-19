package sudoku.generators;

import javafx.scene.control.Alert;
import sudoku.solvers.Solver;
import sudoku.solvers.dfs.DepthFirstSearchSolver;
import sudoku.util.Logger;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SudokuGenerator {
    private SudokuGenerator() {
    }

    public static int[][] generate(int n) {
        Alert genAlert = new Alert(Alert.AlertType.INFORMATION, "Generating Sudoku Puzzle");
        long genStart = System.currentTimeMillis();
        genAlert.show();
        int[][] result;
        int rng = (int) ((Math.random() * 5 + 3) / 10 * n * n);
        Logger.debug(rng + "/" + n * n);
        List<Integer> domain = IntStream.rangeClosed(1, n).boxed().collect(Collectors.toList());
        Solver solver;
        int iteration = 0;
        do {
            Logger.debug("Generating board, iteration=" + iteration);
            result = new int[n][n];
            for (int i = 0; i < rng; i++) {
                int c = (int) (Math.random() * n);
                int r = (int) (Math.random() * n);
                Collections.shuffle(domain);
                for (int v : domain) {
                    if (Solver.canSelect(result, c, r, v)) {
                        result[c][r] = v;
                        break;
                    }
                }
            }
            Logger.debug("Checking board for solution");
            solver = new DepthFirstSearchSolver(result);
            solver.findSingleSolution();
            iteration++;
        } while (!solver.isSolvable());
        Logger.debug("Board is solvable!");
        Logger.debug("Generation took " + (System.currentTimeMillis() - genStart) + "ms");
        long start = System.currentTimeMillis();
        //Solver s = new SudokuDlx(result);
        //s.findSingleSolution();
        //Logger.debug("Dlx " + (System.currentTimeMillis() - start));
        genAlert.close();
        return result;
    }
}
