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
import sudoku.data.SudokuModel;

import java.io.*;
import java.util.Comparator;

public class SudokuView extends VBox implements SudokuCell.ValueChangeListener {
    private SudokuModel model = new SudokuModel();
    private final Stage stage;
    private FileChooser chooser;
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
                        SudokuCell cell = model.get(c * 3 + ic, r * 3 + ir);
                        pane.add(cell, ic, ir);
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
        reset.setOnAction(e -> model.reset());

        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> System.exit(0));

        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> {
            FileChooser chooser = getChooser();
            File saveFile = chooser.showSaveDialog(stage);
            if (saveFile != null) {
                model.save(saveFile);
            }
        });

        MenuItem load = new MenuItem("Load");
        load.setOnAction(e -> {
            FileChooser chooser = getChooser();
            File loadFile = chooser.showOpenDialog(stage);
            if (loadFile != null) {
                model.load(loadFile);
            }
        });

        fileMenu.getItems().addAll(reset, save, load, new SeparatorMenuItem(), exit);

        Menu help = new Menu("Help");
        CheckMenuItem limitSelections = new CheckMenuItem("Limit Options");
        limitSelections.selectedProperty().addListener((observable, oldValue, newValue) -> model.restrictDomains(newValue));

        MenuItem hint = new MenuItem("Hint");
        hint.setOnAction(e -> {
            SudokuCell hintCell = model.getMin(Comparator.comparingInt(cell -> cell.getDomain().size()));
            hintCell.setHint();
        });
        help.getItems().addAll(limitSelections, hint);

        bar.getMenus().addAll(fileMenu, help);
        getChildren().addAll(bar);
    }

    public synchronized FileChooser getChooser() {
        if (chooser == null) {
            chooser = new FileChooser();
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Sudoku files", "*.sudoku", "*.sdk"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
        }
        return chooser;
    }

    @Override
    public void changed(SudokuCell cell, Integer oldValue, Integer newValue) {
        System.out.printf("%s : Previous=%s : Current=%s\n", cell, oldValue, newValue);
    }
}
