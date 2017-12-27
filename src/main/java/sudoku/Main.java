package sudoku;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sudoku.ui.SudokuView;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Sudoku");
        Scene scene = new Scene(new SudokuView(primaryStage));
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.getStylesheets().add(Main.class.getResource("/styles.css").toExternalForm());
    }
}
