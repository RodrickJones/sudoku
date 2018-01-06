package sudoku.solvers.dlx;

public class DlxImpl {
    public DlxImpl() {

    }

    //column
    //row
    void create(byte[][] matrix) {
        //Root node
        ColumnObject root = new ColumnObject();
        root.U = root;
        root.D = root;
        root.L = root;
        root.R = root;

        //Column headers
        for (int i = 0; i < matrix.length; i++) {

        }
    }
}
