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
        try {
            genAlert.show();
            return generate(n, 0);
        } finally {
            genAlert.close();
        }
    }

    private static int[][] generate(int n, int depth) {
        int[][] result = new int[n][n];
        System.out.println("Depth=" + depth++);
        int rng = (int) ((Math.random() * 2 + 1) / 4 * n * n);
        System.out.println(rng + "/" + n * n);
        List<Integer> domain = IntStream.rangeClosed(1, n).boxed().collect(Collectors.toList());
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
        Solver solver = new DepthFirstSearchSolver(result);
        System.out.println("Checking solution");
        solver.findSingleSolution();
        System.out.println(solver.isSolvable());
        return solver.isSolvable() ? result : generate(n, depth);
    }
}
