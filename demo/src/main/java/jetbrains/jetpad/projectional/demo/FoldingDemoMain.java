package jetbrains.jetpad.projectional.demo;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.demo.expr.ExprDemo;
import jetbrains.jetpad.projectional.util.awt.AwtComponent;

import java.awt.*;

import static jetbrains.jetpad.cell.util.CellFactory.*;

public class FoldingDemoMain {
  public static void main(String[] args) {
    AwtComponent.showDemo(createDemo());
  }

  private static CellContainer createDemo() {
    CellContainer result = new CellContainer();
    result.root.children().add(indent(label("AAA"), space(), label("BBB"), space(), label("CCCC"), indent(true, optionsPart())));

    return result;
  }

  private static IndentCell optionsPart() {
    final ValueProperty<Boolean> hasFocus = new ValueProperty<>();

    final IndentCell result = indent();

    final TextCell placeholder = label("<...>");
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

    final IndentCell invisiblePart = indent(newLine(), label("Options Will Be Here"));
    result.children().add(invisiblePart);
    invisiblePart.visible().set(false);

    hasFocus.addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Boolean> event) {
        placeholder.visible().set(!event.getNewValue());
        invisiblePart.visible().set(event.getNewValue());
      }
    });
    return result;
  }
}
