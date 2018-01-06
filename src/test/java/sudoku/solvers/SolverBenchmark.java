package sudoku.solvers;

import javafx.application.Application;
import javafx.stage.Stage;
import sudoku.data.SudokuModel;
import sudoku.solvers.dfs.DepthFirstSearchSolver;
import sudoku.solvers.dlx.DancingLinksSolver;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class SolverBenchmark extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SudokuModel model = new SudokuModel();
        File f = new File("C:\\Users\\Taffy\\IdeaProjects\\sudoku\\src\\test\\java\\sudoku\\solvers\\half_complete.sudoku");
        model.load(f);
        int[][] matrix = model.toMatrix();
        List<Class<? extends Solver>> classes = Arrays.asList(DepthFirstSearchSolver.class,
                DancingLinksSolver.class);
        for (Class<? extends Solver> clazz : classes) {
            Constructor<? extends Solver> constructor = clazz.getConstructor(int[][].class);
            //warmup
            System.out.println("Warming up: " + clazz.getName());
            Solver solver = constructor.newInstance((Object) matrix);
            for (int i = 0; i < 100; i++) {
                solver.reset(matrix);
                solver.findSingleSolution();
            }
            System.out.println("Benchmarking: " + clazz.getName());
            //benchmark
            IntStream.range(0, 100).mapToLong(s -> {
                solver.reset(matrix);
                long start = System.nanoTime();
                solver.findSingleSolution();
                return System.nanoTime() - start;
            }).average().ifPresent(l -> System.out.println(l + "ns"));
        }
        System.exit(0);
    }
}
