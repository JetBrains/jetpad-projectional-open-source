package jetbrains.jetpad.cell.util;

import com.google.common.base.Objects;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellContainerAdapter;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public class WithPropertyIndex {
  private CellPropertySpec<?> myProp;
  private ObservableSet<Cell> myWithProperty = new ObservableHashSet<Cell>();
  private Registration myReg;

  public WithPropertyIndex(CellContainer cellContainer, CellPropertySpec<?> prop) {
    myProp = prop;
    myReg = cellContainer.addListener(new CellContainerAdapter() {
      @Override
      public void onViewPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> change) {
        if (prop != myProp) return;

        if (isNonTrivialValue(cell, myProp)) {
          myWithProperty.add(cell);
        } else {
          myWithProperty.remove(cell);
        }
      }

      @Override
      public void onChildAdded(Cell parent, CollectionItemEvent<Cell> change) {
        onAdd(change.getItem());
      }

      @Override
      public void onChildRemoved(Cell parent, CollectionItemEvent<Cell> change) {
        onRemove(change.getItem());
      }
    });
    onAdd(cellContainer.root);
  }

  private <ValueT> boolean isNonTrivialValue(Cell cell, CellPropertySpec<ValueT> spec) {
    return !Objects.equal(cell.get(spec), spec.getDefault(cell));
  }

  public ObservableSet<Cell> withProperty() {
    return myWithProperty;
  }

  private void onAdd(Cell cell) {
    if (isNonTrivialValue(cell, myProp)) {
      myWithProperty.add(cell);
    }

    for (Cell child : cell.children()) {
      onAdd(child);
    }
  }

  private void onRemove(Cell cell) {
    for (Cell child : cell.children()) {
      onRemove(child);
    }

    if (isNonTrivialValue(cell, myProp)) {
      myWithProperty.remove(cell);
    }
  }

  public void dispose() {
    myWithProperty.clear();
    myReg.remove();
  }


}
