package sudoku.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sudoku.data.SudokuCell;
import sudoku.data.SudokuModel;
import sudoku.generators.SudokuGenerator;
import sudoku.solvers.dfs.DepthFirstSearchSolver;

import java.io.File;
import java.util.Comparator;
import java.util.Optional;

public class SudokuView extends VBox {
    private SudokuCell lastHint;

    private final SudokuModel model;
    private final Stage stage;
    private final ScrollPane boardPane;
    private FileChooser chooser;

    public SudokuView(Stage stage, int n) {
        this.model = new SudokuModel(n);
        this.stage = stage;
        setAlignment(Pos.TOP_CENTER);
        setSpacing(5);

        createMenus();

        Label heading = new Label("Sudoku");
        heading.setFont(new Font(24));
        getChildren().add(heading);

        boardPane = new ScrollPane();
        setFillWidth(true);
        setVgrow(boardPane, Priority.ALWAYS);
        getChildren().add(boardPane);
        display(model.fromMatrix(SudokuGenerator.generate(n), true));
    }

    private void display(SudokuModel model) {
        GridPane parentPane = new GridPane();
        parentPane.setPadding(new Insets(5, 5, 5, 5));
        parentPane.setHgap(5);
        parentPane.setVgap(5);
        int sqrtN = (int) Math.sqrt(model.getN());
        for (int c = 0; c < sqrtN; c++) {
            for (int r = 0; r < sqrtN; r++) {
                GridPane pane = new GridPane();
                pane.setAlignment(Pos.TOP_CENTER);
                pane.setHgap(1);
                pane.setVgap(1);
                parentPane.add(pane, c, r);
                for (int ic = 0; ic < sqrtN; ic++) {
                    for (int ir = 0; ir < sqrtN; ir++) {
                        SudokuCell cell = model.get(c * sqrtN + ic, r * sqrtN + ir);
                        pane.add(cell, ic, ir);
                    }
                }
            }
        }
        parentPane.setAlignment(Pos.TOP_CENTER);
        boardPane.setFitToWidth(true);
        boardPane.setFitToHeight(true);
        boardPane.setContent(parentPane);
    }

    private void createMenus() {
        MenuBar bar = new MenuBar();
        bar.getMenus().addAll(createFileMenu(), createHelpMenu());
        getChildren().addAll(bar);
    }

    private Menu createFileMenu() {
        Menu fileMenu = new Menu("File");

        MenuItem newGame = new MenuItem("New");
        newGame.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog("9");
            dialog.setContentText("Side length:");
            Optional<String> res;
            int side = -1;
            while (side == -1) {
                res = dialog.showAndWait();
                if (res.isPresent()) {
                    try {
                        side = Integer.parseInt(res.get());
                    } catch (NumberFormatException e) {
                        side = -1;
                    }
                } else {
                    return;
                }
            }
            display(model.fromMatrix(SudokuGenerator.generate(side), true));
        });

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
                display(model.load(loadFile));
            }
        });

        fileMenu.getItems().addAll(newGame, reset, save, load, new SeparatorMenuItem(), exit);
        return fileMenu;
    }

    private Menu createHelpMenu() {
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
                Comparator.comparingInt(cell -> cell.getDomain().size()))
                .ifPresent(c -> {
                    if (lastHint != null) {
                        lastHint.setHint(false);
                    }
                    c.setHint(true);
                    lastHint = c;
                }));


        MenuItem check = new MenuItem("Check");
        check.setOnAction(e -> {
            Dialog dialog;
            if (model.isComplete()) {
                dialog = new Alert(Alert.AlertType.CONFIRMATION, "Game is complete and valid!");
            } else {
                dialog = new Alert(Alert.AlertType.ERROR, "Game is incomplete or invalid!");
            }
            dialog.show();
        });

        MenuItem solve = new MenuItem("Solve");
        solve.setOnAction(e -> {
            DepthFirstSearchSolver s = new DepthFirstSearchSolver(model.toMatrix());
            s.findSingleSolution();
            display(model.fromMatrix(s.getSolution(), false));
        });

        help.getItems().addAll(undo, redo, new SeparatorMenuItem(), limitSelections, hint, check, solve);
        return help;
    }

    private synchronized FileChooser getChooser() {
        if (chooser == null) {
            chooser = new FileChooser();
            chooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Sudoku files", "*.sudoku", "*.sdk"),
                    new FileChooser.ExtensionFilter("All files", "*.*")
            );
        }
        return chooser;
    }
}
