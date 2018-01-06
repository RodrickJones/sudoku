package sudoku.data;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import sudoku.solvers.Solver;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SudokuModel {
    private final ObservableList<CellChange> undoStack = new ObservableListWrapper<>(new LinkedList<>());
    private final ObservableList<CellChange> redoStack = new ObservableListWrapper<>(new LinkedList<>());
    private int n;
    private SudokuCell[] cells;
    private final SudokuCell.ValueChangeListener UNDO_LISTENER =
            (cell, oldVal, newVal) -> {
                undoStack.add(0, new CellChange(cell, oldVal, newVal));
                redoStack.clear();
            };
    private final SudokuCell.ValueChangeListener DOMAIN_RESTRICTOR = (cell, oldValue, newValue) -> {
        Collection<SudokuCell> row = new ArrayList<>(n);
        Collection<SudokuCell> column = new ArrayList<>(n);
        Collection<SudokuCell> sector = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            SudokuCell rowCell = get(i, cell.getRow());
            SudokuCell columnCell = get(cell.getColumn(), i);
            row.add(rowCell);
            column.add(columnCell);
            if (newValue != null) {
                if (rowCell != cell) {
                    rowCell.getDomain().remove(newValue);
                }
                if (columnCell != cell) {
                    columnCell.getDomain().remove(newValue);
                }
            }
        }

        int sqrtN = (int) Math.sqrt(n);
        int sectorC = cell.getColumn() / sqrtN * sqrtN;
        int sectorR = cell.getRow() / sqrtN * sqrtN;
        for (int c = sectorC; c < sectorC + sqrtN; c++) {
            for (int r = sectorR; r < sectorR + sqrtN; r++) {
                SudokuCell sectorCell = get(c, r);
                sector.add(sectorCell);
                if (newValue != null && sectorCell != cell) {
                    sectorCell.getDomain().remove(newValue);
                }
            }
        }

        if (oldValue != null) {
            if (row.stream().noneMatch(c -> oldValue.equals(c.getValue()))) {
                row.stream().filter(c -> !c.getDomain().contains(oldValue)).forEach(c -> c.getDomain().add(oldValue));
            }
            if (column.stream().noneMatch(c -> oldValue.equals(c.getValue()))) {
                column.stream().filter(c -> !c.getDomain().contains(oldValue)).forEach(c -> c.getDomain().add(oldValue));
            }
            if (sector.stream().noneMatch(c -> oldValue.equals(c.getValue()))) {
                sector.stream().filter(c -> !c.getDomain().contains(oldValue)).forEach(c -> c.getDomain().add(oldValue));
            }
        }
    };
    private boolean restrictDomains;

    public SudokuModel(int n) {
        this.n = n;
        init();
    }

    private void init() {
        int sqrtN = (int) Math.sqrt(n);
        if (sqrtN * sqrtN != n) {
            throw new IllegalArgumentException("n must be a square integer");
        }
        this.cells = new SudokuCell[n * n];
        for (int i = 0; i < cells.length; i++) {
            SudokuCell cell = new SudokuCell(i, n);
            cell.addListener(UNDO_LISTENER);
            cells[i] = cell;
        }
    }

    public int getN() {
        return n;
    }

    public SudokuCell get(int c, int r) {
        return cells[r * n + c];
    }

    public Optional<SudokuCell> getMin(Predicate<SudokuCell> filter, Comparator<SudokuCell> comparator) {
        return Stream.of(cells).filter(cell -> filter == null || filter.test(cell)).min(comparator);
    }

    public Optional<SudokuCell> getMin(Comparator<SudokuCell> comparator) {
        return getMin(null, comparator);
    }

    public Optional<SudokuCell> getMax(Comparator<SudokuCell> comparator) {
        return Stream.of(cells).max(comparator);
    }

    private void applyToCells(Consumer<SudokuCell> function) {
        Stream.of(cells).forEach(function);
    }

    public void reset(int n) {
        this.n = n;
        undoStack.clear();
        redoStack.clear();
        init();
    }

    public void reset() {
        undoStack.clear();
        redoStack.clear();
        applyToCells(SudokuCell::reset);
    }

    public BooleanBinding undoIsEmpty() {
        return Bindings.isEmpty(undoStack);
    }

    public void undo() {
        CellChange change = undoStack.remove(0);
        redoStack.add(0, change);
        change.undo();
    }

    public BooleanBinding redoIsEmpty() {
        return Bindings.isEmpty(redoStack);
    }

    public void redo() {
        CellChange change = redoStack.remove(0);
        undoStack.add(0, change);
        change.redo();
    }


    public boolean isComplete() {
        List<Collection<Integer>> rows = new ArrayList<>(n);
        List<Collection<Integer>> columns = new ArrayList<>(n);
        List<Collection<Integer>> sectors = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            rows.add(new LinkedHashSet<>(n));
            columns.add(new LinkedHashSet<>(n));
            sectors.add(new LinkedHashSet<>(n));
        }

        final int sqrtN = (int) Math.sqrt(n);

        for (int c = 0; c < n; c++) {
            Collection<Integer> column = columns.get(c);
            for (int r = 0; r < n; r++) {
                Collection<Integer> row = rows.get(r);
                Collection<Integer> sector = sectors.get(c / sqrtN + r / sqrtN * sqrtN);
                SudokuCell cell = get(c, r);
                Integer cellValue = cell.getValue();
                if (cellValue == null ||
                        !column.add(cellValue) ||
                        !row.add(cellValue) ||
                        !sector.add(cellValue)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void restrictDomains(boolean value) {
        if (value) {
            boolean[][][] domains = Solver.calculateDomains(toMatrix(), null, n);
            applyToCells(cell -> {
                cell.deafen();
                boolean[] domain = domains[cell.getColumn()][cell.getRow()];
                cell.setDomain(IntStream.rangeClosed(1, n).filter(i -> domain[i]).boxed().collect(Collectors.toList()));
                cell.undeafen();
                cell.addListener(DOMAIN_RESTRICTOR);
            });
        } else {
            applyToCells(cell -> {
                cell.removeListener(DOMAIN_RESTRICTOR);
                cell.deafen();
                cell.resetDomain();
                cell.undeafen();
            });
        }
        this.restrictDomains = value;
    }

    public void save(File saveFile) {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(saveFile))) {
            //write n
            writer.println(n);
            //write value table
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    Integer val = get(c, r).getValue();
                    writer.print(val == null ? 0 : val);
                    writer.print(' ');
                }
                writer.println();
            }
            //write blank line
            writer.println();
            //write locked table
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n; c++) {
                    writer.print(get(c, r).isLocked() ? 1 : 0);
                    writer.print(' ');
                }
                writer.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(File loadFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(loadFile))) {
            //read n count
            String line = reader.readLine();
            int n = Integer.parseInt(line);
            this.n = n;
            this.init();
            String[] split;
            //read value table
            for (int r = 0; r < n; r++) {
                split = reader.readLine().split(" ");
                for (int c = 0; c < n; c++) {
                    int value = Integer.parseInt(split[c]);
                    SudokuCell cell = get(c, r);
                    cell.deafen();
                    cell.setValue(value == 0 ? null : value);
                    cell.undeafen();
                }
            }
            //read blank line
            reader.readLine();
            //read locked table
            for (int r = 0; r < n; r++) {
                split = reader.readLine().split(" ");
                for (int c = 0; c < n; c++) {
                    get(c, r).setLocked(Integer.parseInt(split[c]) == 1);
                }
            }
            restrictDomains(restrictDomains);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fromMatrix(int[][] matrix) {
        if (matrix == null) {
            System.out.println("null matrix");
            return;
        }
        reset();
        applyToCells(cell -> {
            int value = matrix[cell.getColumn()][cell.getRow()];
            cell.deafen();
            cell.setValue(value == 0 ? null : value);
            cell.undeafen();
        });
    }

    public int[][] toMatrix() {
        int[][] matrix = new int[n][n];
        for (int c = 0; c < n; c++) {
            for (int r = 0; r < n; r++) {
                Integer cellValue = get(c, r).getValue();
                matrix[c][r] = cellValue == null ? 0 : cellValue;
            }
        }
        return matrix;
    }

    private class CellChange {
        private final SudokuCell cell;
        private final Integer oldValue;
        private final Integer newValue;

        private CellChange(SudokuCell cell, Integer oldValue, Integer newValue) {
            this.cell = cell;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        private void undo() {
            cell.deafen();
            cell.setValue(oldValue);
            if (restrictDomains) {
                DOMAIN_RESTRICTOR.changed(cell, newValue, oldValue);
            }
            cell.undeafen();
        }

        private void redo() {
            cell.deafen();
            cell.setValue(newValue);
            if (restrictDomains) {
                DOMAIN_RESTRICTOR.changed(cell, oldValue, newValue);
            }
            cell.undeafen();
        }

    }

}
