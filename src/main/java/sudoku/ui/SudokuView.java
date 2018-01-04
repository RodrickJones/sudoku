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

import java.io.File;
import java.util.Comparator;
import java.util.List;

public class SudokuView extends VBox implements SudokuCell.ValueChangeListener, SudokuCell.DomainChangeListener {
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
                        cell.addListener((SudokuCell.ValueChangeListener) this);
                        cell.addListener((SudokuCell.DomainChangeListener) this);
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

        MenuItem undo = new MenuItem("Undo");
        undo.disableProperty().bind(model.undoIsEmpty());
        undo.setOnAction(e -> model.undo());

        MenuItem redo = new MenuItem("Redo");
        redo.disableProperty().bind(model.redoIsEmpty());
        redo.setOnAction(e -> model.redo());

        CheckMenuItem limitSelections = new CheckMenuItem("Limit Options");
        limitSelections.selectedProperty().addListener((observable, oldValue, newValue) -> model.restrictDomains(newValue));

        MenuItem hint = new MenuItem("Hint");
        hint.setOnAction(e -> model.getMin(cell -> cell.getValue() == null || cell.getDomain().size() > 2,
                Comparator.comparingInt(cell -> cell.getDomain().size())).ifPresent(SudokuCell::setHint));

        help.getItems().addAll(undo, redo, new SeparatorMenuItem(), limitSelections, hint);

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

    @Override
    public void changed(SudokuCell cell, List<Integer> oldDomain, List<Integer> newDomain) {
        System.out.printf("%s : Previous=%s : Current=%s\n", cell, oldDomain, newDomain);
    }
}
