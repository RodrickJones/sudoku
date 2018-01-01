package sudoku.data;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SudokuModel {
    private final SudokuCell.ValueChangeListener DOMAIN_RESTRICTOR = (cell, oldValue, newValue) -> {
        //get all affected cells, calculate if oldvalue can be readded to their domains, remove newvalue from their domains
    };
    private final SudokuCell[] cells = new SudokuCell[81];
    private boolean restrictDomains;
    public SudokuModel() {
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 9; r++) {
                cells[c * 9 + r] = new SudokuCell(c, r);
            }
        }
    }

    public SudokuCell get(int c, int r) {
        return cells[c * 9 + r];
    }

    public SudokuCell getMin(Comparator<SudokuCell> comparator) {
        return Stream.of(cells).min(comparator).orElse(null);
    }

    public SudokuCell getMax(Comparator<SudokuCell> comparator) {
        return Stream.of(cells).max(comparator).orElse(null);
    }

    private void applyToCells(Consumer<SudokuCell> function) {
        for (SudokuCell cell : cells) {
            function.accept(cell);
        }
    }

    public void reset() {
        applyToCells(SudokuCell::reset);
    }

    public void restrictDomains(boolean value) {
        if (value) {
            final List<Integer> FULL_SET = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
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
                    SudokuCell cell = cells[c * 9 + r];
                    Integer cellValue = cell.getValue();
                    if (cellValue != null) {
                        column.add(cell.getValue());
                        row.add(cellValue);
                        sector.add(cellValue);
                    }
                }
            }

            for (int c = 0; c < 9; c++) {
                Collection<Integer> column = columns.get(c);
                for (int r = 0; r < 9; r++) {
                    Collection<Integer> domain = new LinkedHashSet<>(FULL_SET);
                    Collection<Integer> row = rows.get(r);
                    Collection<Integer> sector = sectors.get(c / 3 + r / 3 * 3);
                    domain.removeAll(column);
                    domain.removeAll(row);
                    domain.removeAll(sector);
                    SudokuCell cell = cells[c * 9 + r];
                    cell.deafen();
                    cell.setDomain(domain);
                    cell.undeafen();
                }
            }
            Stream.of(cells).forEach(c -> c.addListener(DOMAIN_RESTRICTOR));
        } else {
            applyToCells(SudokuCell::resetDomain);
            Stream.of(cells).forEach(c -> c.removeListener(DOMAIN_RESTRICTOR));
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

}
