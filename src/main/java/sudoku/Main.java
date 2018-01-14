package sudoku;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sudoku.ui.SudokuView;
import sudoku.util.Logger;

public class Main extends Application {
    private static int n = 9;

    public static void main(String[] args) {
        for (String arg : args) {
            if (arg.contains("n=")) {
                n = Integer.parseInt(arg.replace("n=", ""));
            }
        }
        Logger.setLevel(Logger.Level.DEBUG);
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sudoku");
        Scene scene = new Scene(new SudokuView(primaryStage, n));
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
    }
}
