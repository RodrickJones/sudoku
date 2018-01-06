package sudoku.data;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SudokuCell extends ComboBox<Integer> {
    private static final String SUDOKU_CELL_CLASS = "sudoku-cell";
    private static final PseudoClass INCORRECT = PseudoClass.getPseudoClass("incorrect");
    private static final PseudoClass HINT = PseudoClass.getPseudoClass("hint");

    private final ObservableList<Integer> domain;
    private final int column;
    private final int row;
    private final int n;
    private final Collection<ValueChangeListener> valueChangeListeners = new CopyOnWriteArrayList<>();
    private final Collection<DomainChangeListener> domainChangeListeners = new CopyOnWriteArrayList<>();
    private boolean deafen;

    SudokuCell(int column, int row, int n) {
        getStyleClass().add(SUDOKU_CELL_CLASS);
        this.column = column;
        this.row = row;
        this.n = n;
        this.domain = new ObservableListWrapper<>(new ArrayList<>(n + 1));
        resetDomain();

        setItems(new SortedList<>(domain, Comparator.comparingInt(i -> i == null ? 0 : i)));
        getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!deafen) {
                valueChangeListeners.forEach(l -> l.changed(this, oldVal, newVal));
            }
            pseudoClassStateChanged(HINT, false);
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
                oldDomain.sort(Comparator.comparingInt(i -> i == null ? 0 : i));
                newDomain.sort(Comparator.comparingInt(i -> i == null ? 0 : i));
                domainChangeListeners.forEach(l -> l.changed(this, oldDomain, newDomain));
            }
            pseudoClassStateChanged(INCORRECT, isInvalid());
        });
    }

    SudokuCell(int index, int n) {
        this(index % n, index / n, n);
    }

    public void reset() {
        deafen();
        setValue(null);
        resetDomain();
        pseudoClassStateChanged(HINT, false);
        pseudoClassStateChanged(INCORRECT, false);
        undeafen();
    }

    public ObservableList<Integer> getDomain() {
        return domain;
    }

    public void resetDomain() {
        setDomain(IntStream.rangeClosed(1, n).boxed().collect(Collectors.toList()));
    }

    public void setDomain(Collection<Integer> domain) {
        Integer selection = getValue();
        this.domain.clear();
        if (!domain.contains(null)) {
            this.domain.add(null);
        }
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

    public void removeListener(ValueChangeListener listener) {
        valueChangeListeners.remove(listener);
    }

    public void addListener(DomainChangeListener listener) {
        domainChangeListeners.add(listener);
    }

    public void removeListener(DomainChangeListener listener) {
        domainChangeListeners.remove(listener);
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

    public void setLocked(boolean lock) {
        setDisabled(lock);
    }

    public boolean isLocked() {
        return isDisabled();
    }

    public void setHint(boolean active) {
        pseudoClassStateChanged(HINT, active);
    }

    public boolean isInvalid() {
        return domain.size() == 1;
    }

    @Override
    public String toString() {
        return "SudokuCell(column=" + column + ", row=" + row + ", value=" + getValue() + ", locked=" + isLocked() + ")";
    }


    public interface ValueChangeListener {
        void changed(SudokuCell cell, Integer oldValue, Integer newValue);
    }

    public interface DomainChangeListener {
        void changed(SudokuCell cell, List<Integer> oldDomain, List<Integer> newDomain);
    }
}
