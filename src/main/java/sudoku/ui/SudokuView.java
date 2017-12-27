package sudoku.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sudoku.data.SudokuCell;

public class SudokuView extends VBox implements SudokuCell.ValueChangeListener {
    private SudokuCell[][] cells = new SudokuCell[9][9];
    private final Stage stage;
    public SudokuView(Stage stage) {
        this.stage = stage;
        setAlignment(Pos.TOP_CENTER);
        setSpacing(5);

        createMenus();

        Label heading = new Label("Sudoku");
        heading.setFont(new Font(24));
        getChildren().add(heading);

        GridPane parentPane = new GridPane();
        parentPane.setPadding(new Insets(5, 5, 5, 5));
        getChildren().add(parentPane);
        parentPane.setHgap(5);
        parentPane.setVgap(5);
        for (int c = 0; c < 3; c++) {
            for (int r = 0; r < 3; r++) {
                GridPane pane = new GridPane();
                pane.setAlignment(Pos.TOP_CENTER);
                pane.setHgap(1);
                pane.setVgap(1);
                parentPane.add(pane, c, r);
                for (int ic = 0; ic < 3; ic++) {
                    for (int ir = 0; ir < 3; ir++) {
                        SudokuCell cell = new SudokuCell(c * 3 + ic, r * 3 + ir);
                        pane.add(cell, ic, ir);
                        cells[c * 3 + ic][r * 3 + ir] = cell;
                        cell.addListener(this);
                    }
                }
            }
        }
        autosize();
    }

    private void createMenus() {
        MenuBar bar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem reset = new MenuItem("Reset");
        reset.setOnAction(e -> {
            for (SudokuCell[] arr : cells) {
                for (SudokuCell cell : arr) {
                    cell.reset();
                }
            }
        });

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> System.exit(0));

        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> System.out.println("Not implemented yet"));

        MenuItem load = new MenuItem("Load");
        load.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.showOpenDialog(stage);
        });

        fileMenu.getItems().addAll(reset, save, load, new SeparatorMenuItem(), exit);

        Menu help = new Menu("Help");
        CheckMenuItem limitSelections = new CheckMenuItem("Limit Options");
        help.getItems().addAll(limitSelections);

        bar.getMenus().addAll(fileMenu, help);
        getChildren().addAll(bar);
    }

    @Override
    public void changed(SudokuCell cell, Integer oldValue, Integer newValue) {
        System.out.println(cell + " : " + oldValue + " : " + newValue);
    }
}
