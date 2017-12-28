package sudoku.data;

import java.io.*;

public class SudokuModel {
    private final SudokuCell[][] cells = new SudokuCell[9][9];

    public SudokuModel() {
        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 9; r++) {
                cells[c][r] = new SudokuCell(c, r);
            }
        }
    }

    public SudokuCell get(int c, int r) {
        return cells[c][r];
    }

    public void reset() {
        for (SudokuCell[] column : cells) {
            for (SudokuCell cell : column) {
                cell.reset();
            }
        }
    }

    public void save(File saveFile) {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(saveFile))) {
            for (SudokuCell[] arr : cells) {
                for (SudokuCell cell : arr) {
                    Integer val = cell.getValue();
                    out.writeByte(val == null ? 0 : val.byteValue());
                    out.writeBoolean(cell.isLocked());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(File loadFile) {
        try (DataInputStream in = new DataInputStream(new FileInputStream(loadFile))) {
            for (SudokuCell[] arr : cells) {
                for (SudokuCell cell : arr) {
                    cell.deafen();
                    int val = in.readUnsignedByte();
                    cell.setValue(val == 0 ? null : val);
                    cell.resetDomain();
                    if (in.readBoolean()) {
                        cell.lock();
                    }
                    cell.undeafen();
                }
            }
            //check for domain reducing?
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
