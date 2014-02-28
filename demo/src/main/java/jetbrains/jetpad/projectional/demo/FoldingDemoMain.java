package jetbrains.jetpad.projectional.demo;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.projectional.util.awt.AwtComponent;
import jetbrains.jetpad.values.Color;

import static jetbrains.jetpad.cell.util.CellFactory.*;

public class FoldingDemoMain {
  public static void main(String[] args) {
    AwtComponent.showDemo(createDemo());
  }

  private static CellContainer createDemo() {
    CellContainer result = new CellContainer();
    result.root.children().add(indent(label("AAA"), space(), label("BBB"), space(), label("CCCC"), indent(true, optionsPart())));
    RootController.install(result);
    return result;
  }

  private static IndentCell optionsPart() {
    final ValueProperty<Boolean> hasFocus = new ValueProperty<>();

    final Cell newLine = newLine();
    final IndentCell result = indent(newLine);

    final TextCell placeholder = label("<...>");
    placeholder.textColor().set(Color.LIGHT_GRAY);
    result.children().add(placeholder);

    result.addTrait(new CellTrait() {
      @Override
      public void onFocusLost(Cell cell, FocusEvent event) {
        updateFocus();
        super.onFocusLost(cell, event);
      }

      @Override
      public void onFocusGained(Cell cell, FocusEvent event) {
        updateFocus();
        super.onFocusGained(cell, event);
      }

      private void updateFocus() {
        Cell focusedCell = result.cellContainer().get().focusedCell.get();
        hasFocus.set(Composites.isDescendant(result, focusedCell));
      }
    });

    newLine.visible().set(false);
    hasFocus.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        newLine.visible().set(event.getNewValue());
        placeholder.text().set(event.getNewValue() ? "Options are Here" : "<...>");
        placeholder.textColor().set(event.getNewValue() ? Color.BLACK : Color.LIGHT_GRAY);
      }
    });
    return result;
  }
}
