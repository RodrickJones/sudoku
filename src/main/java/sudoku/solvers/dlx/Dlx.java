package sudoku.solvers.dlx;

import sudoku.util.Logger;

import java.util.*;
import java.util.function.Predicate;

public class Dlx {
    private boolean[][] matrix;
    private ColumnObject h;
    private List<Object> labels;
    private boolean minimizeBranching;
    private boolean interrupt = false;
    private Collection<Predicate<Dlx>> interrupts = Collections.emptyList();
    private LinkedList<DataObject> o = new LinkedList<>();
    private List<Solution> solutions = new ArrayList<>();

    public Dlx reset() {
        matrix = null;
        h = null;
        labels = null;
        minimizeBranching = false;
        interrupt = false;
        interrupts = null;
        o.clear();
        solutions.clear();
        return this;
    }

    public Dlx matrix(boolean[][] matrix) {
        this.matrix = matrix;
        return this;
    }

    public Dlx labels(List<Object> labels) {
        this.labels = labels;
        return this;
    }

    public Dlx buildStructure() {
        ColumnObject h = new ColumnObject();
        h.L = h;
        h.R = h;
        h.U = h;
        h.D = h;
        //null C somewhere

        //create columns
        for (int c = 0; c < matrix.length; c++) {
            ColumnObject header = new ColumnObject();
            header.U = header;
            header.D = header;
            header.R = h;
            header.L = h.L;
            h.L.R = header;
            h.L = header;
            if (labels != null) {
                header.N = labels.get(c);
            }
        }

        ArrayList<DataObject> row = new ArrayList<>(matrix[0].length);
        for (int c = 0; c < matrix.length; c++) {
            ColumnObject col = (ColumnObject) h.R;
            for (int r = 0; r < matrix[c].length; r++) {
                if (matrix[c][r]) {
                    DataObject obj = new DataObject();
                    obj.C = col;
                    obj.U = col.U;
                    obj.D = col;
                    obj.L = obj;
                    obj.R = obj;

                    col.U.D = obj;
                    col.U = obj;
                    col.S++;
                    row.add(obj);
                }
                col = (ColumnObject) col.R;
            }

            if (!row.isEmpty()) {
                DataObject first = row.get(0);
                for (int i = 1; i < row.size(); i++) {
                    DataObject current = row.get(i);
                    current.L = first.L;
                    current.R = first;
                    first.L.R = current;
                    first.L = current;
                }
            }
        }
        this.h = h;
        return this;
    }

    public Dlx interrupts(Collection<Predicate<Dlx>> interrupts) {
        this.interrupts = interrupts;
        return this;
    }

    public Dlx minimizeBranching(boolean value) {
        this.minimizeBranching = value;
        return this;
    }

    public Dlx search(int k) {
        Logger.debug("Entered: " + k);
        if (interrupt) {
            Logger.debug("Interrupted");
            return this;
        }
        if (h.R == h) {
            Logger.debug("Solution found!");
            Solution s = new Solution();
            for (DataObject r : o) {
                ArrayList<Object> row = new ArrayList<>();
                DataObject current = r;
                do {
                    row.add(current.C.N);
                    current = current.R;
                } while (current != r);
                row.trimToSize();
                s.add(row);
            }
            s.trim();
            solutions.add(s);
            interrupt = interrupts != null && interrupts.stream().anyMatch(p -> p.test(this));
        } else {
            ColumnObject c = (ColumnObject) h.R;
            if (minimizeBranching) {
                int s = c.S;
                for (ColumnObject j = (ColumnObject) c.R; j != h; j = (ColumnObject) j.R) {
                    if (j.S < s) {
                        s = j.S;
                        c = j;
                    }
                }
            }
            Logger.debug("Cover " + k);
            cover(c);
            for (DataObject r = c.D; r != c; r = r.D) {
                o.addLast(r);
                for (DataObject j = r.R; j != r; j = j.R) {
                    cover(j.C);
                }
                search(k + 1);
                for (DataObject j = r.L; j != r; j = j.L) {
                    uncover(j.C);
                }
                //o.removeLast();
            }
            Logger.debug("Uncover " + k);
            uncover(c);
            Logger.debug("uncovered");
        }
        Logger.debug("Exiting " + k);

        //2 uncovers is breaking something somewhere
        return this;
    }

    public Dlx search() {
        Logger.debug("Searching matrix with Dlx, parameters are:");
        Logger.debug("Matrix dimensions: " + matrix.length + "x" + matrix[0].length);
        Logger.debug("Labels: " + labels);
        Logger.debug("Minimize branching: " + minimizeBranching);
        Logger.debug("Interrupts: " + interrupts);
        return search(0);
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    private void cover(ColumnObject c) {
        //Logger.debug("Covering " + c);
        c.R.L = c.L;
        c.L.R = c.R;
        for (DataObject i = c.D; i != c; i = i.D) {
            for (DataObject j = i.R; j != i; j = j.R) {
                j.D.U = j.U;
                j.U.D = j.D;
                j.C.S--;
            }
        }
    }

    private void uncover(ColumnObject c) {
        //Logger.debug("Uncovering " + c);
        for (DataObject i = c.U; i != c; i = i.U) {
            for (DataObject j = i.L; j != i; j = j.L) {
                j.C.S++;
                j.D.U = j;
                j.U.D = j;
            }
        }
        c.R.L = c;
        c.L.R = c;
    }

    private class DataObject {
        ColumnObject C;
        DataObject L, R, U, D;
    }

    private class ColumnObject extends DataObject {
        Object N;
        int S;

        @Override
        public String toString() {
            return N == null ? super.toString() : N.toString();
        }
    }

    public class Solution {
        private ArrayList<List<Object>> rows = new ArrayList<>();

        private void add(List<Object> row) {
            rows.add(row);
        }

        private void trim() {
            rows.trimToSize();
        }

        public List<List<Object>> getRows() {
            return rows;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            rows.forEach(r -> {
                r.forEach(v -> builder.append(v).append(' '));
                builder.append('\n');
            });
            return builder.toString();
        }
    }
}
