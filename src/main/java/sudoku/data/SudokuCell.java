package sudoku.data;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class SudokuCell extends ComboBox<Integer> {
    private static final String SUDOKU_CELL_CLASS = "sudoku-cell";
    private static final PseudoClass INCORRECT = PseudoClass.getPseudoClass("incorrect");
    private static final PseudoClass HINT = PseudoClass.getPseudoClass("hint");

    private final ObservableList<Integer> domain = new ObservableListWrapper<>(new ArrayList<Integer>(10));
    private final int column, row;
    private final Collection<ValueChangeListener> valueChangeListeners = new LinkedList<>();
    private final Collection<DomainChangeListener> domainChangeListeners = new LinkedList<>();
    private boolean deafen;
    SudokuCell(int column, int row) {
        getStyleClass().add(SUDOKU_CELL_CLASS);
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

        domain.addListener((ListChangeListener<Integer>) c -> {
            c.next();
            if (!deafen) {
                List<Integer> newDomain = new ArrayList<>(c.getList());
                List<Integer> oldDomain = new ArrayList<>(newDomain);
                if (c.wasAdded()) {
                    oldDomain.removeAll(c.getAddedSubList());
                } else if (c.wasRemoved()) {
                    oldDomain.addAll(c.getRemoved());
                }
                domainChangeListeners.forEach(l -> l.changed(this, oldDomain, newDomain));
            }
        });
        setCenterShape(true);
    }

    public void reset() {
        deafen();
        setValue(null);
        resetDomain();
        undeafen();
    }

    public ObservableList<Integer> getDomain() {
        return domain;
    }

    public void resetDomain() {
        domain.setAll(null, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    public void setDomain(Collection<Integer> domain) {
        Integer selection = getValue();
        this.domain.clear();
        this.domain.add(null);
        this.domain.addAll(domain);
        setValue(selection);
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

    public void addListener(DomainChangeListener listener) {
        domainChangeListeners.add(listener);
    }

    public void deafen() {
        this.deafen = true;
    }

    public void undeafen() {
        this.deafen = false;
    }

    public boolean isDeafened() {
        return deafen;
    }

    public void lock() {
        setDisabled(true);
    }

    public void unlock() {
        setDisabled(false);
    }

    public boolean isLocked() {
        return isDisabled();
    }

    public void setHint() {
        pseudoClassStateChanged(HINT, true);
    }

    @Override
    public String toString() {
        return "SudokuCell(" + column + ", " + row + ")";
    }


    public interface ValueChangeListener {
        void changed(SudokuCell cell, Integer oldValue, Integer newValue);
    }

    public interface DomainChangeListener {
        void changed(SudokuCell cell, List<Integer> oldDomain, List<Integer> newDomain);
    }
}
