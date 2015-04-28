package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.model.composite.Composites;

public class CellTraits {
  public static Registration captureTo(Cell root, final CellTrait targetTrait) {
    final Value<Registration> targetReg = new Value<>(Registration.EMPTY);

    final CellContainer container = root.cellContainer().get();
    if (container != null && container.focusedCell.get() != null && Composites.isDescendant(root, container.focusedCell.get())) {
      targetReg.set(container.focusedCell.get().addTrait(targetTrait));
    }

    final Registration rootReg = root.addTrait(new CellTrait() {
      @Override
      public void onFocusGained(Cell cell, FocusEvent event) {
        super.onFocusGained(cell, event);
        targetReg.get().remove();
        targetReg.set(event.getNewValue().addTrait(targetTrait));
      }

      @Override
      public void onFocusLost(Cell cell, FocusEvent event) {
        targetReg.get().remove();
        targetReg.set(Registration.EMPTY);
        super.onFocusLost(cell, event);
      }
    });

    return new Registration() {
      @Override
      protected void doRemove() {
        targetReg.get().remove();
        rootReg.remove();
      }
    };
  }
}
