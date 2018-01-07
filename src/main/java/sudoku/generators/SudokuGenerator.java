package sudoku.generators;

import javafx.scene.control.Alert;
import sudoku.solvers.Solver;
import sudoku.solvers.dfs.DepthFirstSearchSolver;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SudokuGenerator {

    private SudokuGenerator() {

    }

    public static int[][] generate(int n) {
        Alert genAlert = new Alert(Alert.AlertType.INFORMATION, "Generating Sudoku Puzzle");
        genAlert.show();
        int[][] result;
        int rng = (int) ((Math.random() * 2 + 1) / 4 * n * n);
        System.out.println(rng + "/" + n * n);
        List<Integer> domain = IntStream.rangeClosed(1, n).boxed().collect(Collectors.toList());
        Solver solver;
        int iteration = 0;
        do {
            System.out.println("Generating board, iteration " + iteration);
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
            System.out.println("Checking board for solution");
            solver = new DepthFirstSearchSolver(result);
            solver.findSingleSolution();
            iteration++;
        } while (!solver.isSolvable());
        System.out.println("Board is solvable!");
        genAlert.close();
        return result;
    }
}
