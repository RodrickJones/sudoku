package dlx;

import dlx.data.ColumnNode;
import dlx.data.DataNode;

import java.util.Stack;

public class Dlx {
    //parameters
    private byte[][] constraintsMatrix;
    private boolean minimizeBranching;


    //generated stuff
    private ColumnNode h;
    private Stack<DataNode> o;


    public Dlx constraints(byte[][] matrix) {
        this.constraintsMatrix = matrix;
        return this;
    }

    public Dlx minimizeBranching(boolean value) {
        this.minimizeBranching = value;
        return this;
    }

    public Dlx buildStructure() {
        ColumnNode root = new ColumnNode();
        return this;
    }

    public void search(int k) {
        if (h.R == h) {
            //solved
        } else {
            ColumnNode c = h.R;
            if (minimizeBranching) {
                //compare sizes
                int s = c.S;
                for (ColumnNode j = c.R; j != h; j = j.R) {
                    if (j.S < s) {
                        s = j.S;
                        c = j;
                    }
                }
            }
            cover(c);
            for (DataNode r = c.D; r != c; r = r.D) {
                o.push(r);
                for (DataNode j = r.R; j != r; j = j.R) {
                    cover(j.C);
                }
                search(k + 1);
                r = o.pop();
                c = r.C;
                for (DataNode j = r.L; j != r; j = j.L) {
                    uncover(j.C);
                }
            }
            uncover(c);
        }
    }

    void cover(ColumnNode c) {
        c.R.L = c.L;
        c.L.R = c.R;
        for (DataNode i = c.D; i != c; i = i.D) {
            for (DataNode j = i.R; j != i; j = j.R) {
                j.D.U = j.U;
                j.U.D = j.D;
                j.C.S--;
            }
        }
    }

    void uncover(ColumnNode c) {
        for (DataNode i = c.U; i != c; i = i.U) {
            for (DataNode j = i.L; j != i; j = j.L) {
                j.C.S++;
                j.D.U = j;
                j.U.D = j;
            }
        }
        c.R.L = c;
        c.L.R = c;
    }

}
