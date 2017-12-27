package sudoku.data;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

public class SudokuCell extends ComboBox<Integer> {
    private final ObservableList<Integer> domain = new ObservableListWrapper<>(new ArrayList<Integer>(10));
    private final int column, row;
    private Collection<ValueChangeListener> valueChangeListeners = new LinkedList<>();
    boolean deafen;
    public SudokuCell(int column, int row) {
        setPrefSize(50, 50);
        this.column = column;
        this.row = row;
        domain.setAll(null, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        setItems(domain);
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!deafen) {
                valueChangeListeners.forEach(l -> l.changed(this, oldVal, newVal));
            }
        });
        setCenterShape(true);
    }

    public void reset() {
        deafen = true;
        setValue(null);
        domain.setAll(null, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        deafen = false;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void addListener(ValueChangeListener listener) {
        valueChangeListeners.add(listener);
    }

    public interface ValueChangeListener {
        void changed(SudokuCell cell, Integer oldValue, Integer newValue);
    }

    @Override
    public String toString() {
        return "SudokuCell(" + column + ", " + row + ")";
    }
}
