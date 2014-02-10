package jetbrains.jetpad.cell.util;

import com.google.common.base.Objects;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellContainerAdapter;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

public abstract class WithPropertyIndex {
  public static WithPropertyIndex forCellProperty(final CellContainer container, final CellPropertySpec<?> prop) {
    return new WithPropertyIndex() {
      {
        init(container);
      }

      @Override
      protected boolean isNonTrivialValue(Cell cell) {
        return !Objects.equal(cell.get(prop), prop.getDefault(cell));
      }

      @Override
      protected boolean isProp(CellPropertySpec<?> spec) {
        return  prop == spec;
      }
    };
  }

  private ObservableSet<Cell> myWithProperty = new ObservableHashSet<>();
  private Registration myReg;

  protected void init(CellContainer cellContainer) {
    myReg = cellContainer.addListener(new CellContainerAdapter() {
      @Override
      public void onViewPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> change) {
        if (!isProp(prop)) return;

        if (isNonTrivialValue(cell)) {
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

  protected abstract boolean isNonTrivialValue(Cell cell);

  protected abstract boolean isProp(CellPropertySpec<?> spec);

  public ObservableSet<Cell> withProperty() {
    return myWithProperty;
  }

  private void onAdd(Cell cell) {
    if (isNonTrivialValue(cell)) {
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

    if (isNonTrivialValue(cell)) {
      myWithProperty.remove(cell);
    }
  }

  public void dispose() {
    myWithProperty.clear();
    myReg.remove();
  }
}