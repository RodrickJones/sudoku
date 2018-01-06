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
    private final SudokuCell.ValueChangeListener DOMAIN_RESTRICTOR = (cell, oldValue, newValue) -> {
        //TODO: Try to extract to solver
        Collection<SudokuCell> row = new ArrayList<>(9);
        Collection<SudokuCell> column = new ArrayList<>(9);
        Collection<SudokuCell> sector = new ArrayList<>(9);

        for (int i = 0; i < 9; i++) {
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

        int sectorC = cell.getColumn() / 3 * 3;
        int sectorR = cell.getRow() / 3 * 3;
        for (int c = sectorC; c < sectorC + 3; c++) {
            for (int r = sectorR; r < sectorR + 3; r++) {
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

    private final ObservableList<CellChange> undoStack = new ObservableListWrapper<>(new LinkedList<>());
    private final ObservableList<CellChange> redoStack = new ObservableListWrapper<>(new LinkedList<>());
    private final SudokuCell.ValueChangeListener UNDO_LISTENER =
            (cell, oldVal, newVal) -> {
                undoStack.add(0, new CellChange(cell, oldVal, newVal));
                redoStack.clear();
            };
    private final SudokuCell[] cells = new SudokuCell[81];
    private boolean restrictDomains;

    //TODO: Allow for n-Sudoku
    public SudokuModel() {
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 9; r++) {
                SudokuCell cell = new SudokuCell(c, r);
                cell.addListener(UNDO_LISTENER);
                cells[r * 9 + c] = cell;
            }
        }
    }

    public SudokuCell get(int c, int r) {
        return cells[r * 9 + c];
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
        List<Collection<Integer>> rows = new ArrayList<>(9);
        List<Collection<Integer>> columns = new ArrayList<>(9);
        List<Collection<Integer>> sectors = new ArrayList<>(9);

        for (int i = 0; i < 9; i++) {
            rows.add(new LinkedHashSet<>(9));
            columns.add(new LinkedHashSet<>(9));
            sectors.add(new LinkedHashSet<>(9));
        }

        for (int c = 0; c < 9; c++) {
            Collection<Integer> column = columns.get(c);
            for (int r = 0; r < 9; r++) {
                Collection<Integer> row = rows.get(r);
                Collection<Integer> sector = sectors.get(c / 3 + r / 3 * 3);
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
            boolean[][][] domains = Solver.calculateDomains(toMatrix());
            applyToCells(cell -> {
                cell.deafen();
                boolean[] domain = domains[cell.getColumn()][cell.getRow()];
                cell.setDomain(IntStream.range(1, 10).filter(i -> domain[i]).boxed().collect(Collectors.toList()));
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
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(saveFile))) {
            applyToCells(cell -> {
                try {
                    Integer val = cell.getValue();
                    out.writeByte(val == null ? 0 : val.byteValue());
                    out.writeBoolean(cell.isLocked());
                } catch (IOException e) {
                    System.err.println("IOException occurred while writing data for " + cell);
                    e.printStackTrace();
                }
            });
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
        int[][] matrix = new int[9][9];
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 9; r++) {
                Integer cellValue = get(c, r).getValue();
                matrix[c][r] = cellValue == null ? 0 : cellValue;
            }
        }
        return matrix;
    }

    public void load(File loadFile) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(loadFile))) {
            applyToCells(cell -> {
                try {
                    cell.deafen();
                    int val = in.readUnsignedByte();
                    cell.setValue(val == 0 ? null : val);
                    cell.resetDomain();
                    if (in.readBoolean()) {
                        cell.lock();
                    }
                    cell.undeafen();
                } catch (IOException e) {
                    System.err.println("IOException thrown reading data for " + cell);
                    e.printStackTrace();
                    cell.reset();
                }
            });
            restrictDomains(restrictDomains);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
