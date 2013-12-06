/*
 * Copyright 2012-2013 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.projectional.cell.indent.test;

import com.google.common.base.Strings;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.cell.Cell;
import jetbrains.jetpad.projectional.cell.HorizontalCell;
import jetbrains.jetpad.projectional.cell.TextCell;
import jetbrains.jetpad.projectional.cell.VerticalCell;
import jetbrains.jetpad.projectional.cell.indent.updater.CellWrapper;
import jetbrains.jetpad.projectional.cell.indent.updater.IndentUpdater;
import jetbrains.jetpad.projectional.cell.indent.updater.IndentUpdaterSource;
import jetbrains.jetpad.projectional.cell.indent.updater.IndentUpdaterTarget;

import java.util.List;

public class IndentCell extends VerticalCell {
  private RootIndentPart myRootPart = new RootIndentPart(this);
  private IndentUpdater<IndentPart, Cell> myUpdater;

  public IndentCell() {
    myRootPart.setAttached(true);
    myUpdater = new IndentUpdater<IndentPart, Cell>(myRootPart, this, indentUpdaterSourceForIndentPart(), indentUpdaterTargetForCell());
  }

  private IndentUpdaterSource<IndentPart> indentUpdaterSourceForIndentPart() {
    return new IndentUpdaterSource<IndentPart>() {
      @Override
      public boolean isNewLine(IndentPart src) {
        return src instanceof NewLinePart;
      }

      @Override
      public boolean isIndented(IndentPart src) {
        return src.isIndented();
      }

      @Override
      public boolean isCell(IndentPart src) {
        return src instanceof CellPart;
      }

      @Override
      public boolean isVisible(IndentPart src) {
        return src.isVisible();
      }

      @Override
      public Cell getCell(IndentPart src) {
        return ((CellPart) src).cell();
      }

      @Override
      public boolean isAttached(IndentPart src) {
        return src.isAttached();
      }

      @Override
      public void setAttached(IndentPart src, boolean value) {
        src.setAttached(value);
      }
    };
  }

  private IndentUpdaterTarget<Cell> indentUpdaterTargetForCell() {
    return new IndentUpdaterTarget<Cell>() {
      @Override
      public Cell newLine() {
        return new HorizontalCell();
      }

      @Override
      public Cell newIndent(int size) {
        return new TextCell(Strings.repeat(" ", size * 2));
      }

      @Override
      public CellWrapper<Cell> wrap(final Cell cell) {
        return new CellWrapper<Cell>() {
          @Override
          public Cell item() {
            return cell;
          }

          @Override
          public void remove() {
          }
        };
      }

      @Override
      public List<Cell> children(Cell item) {
        return item.children();
      }

      @Override
      public Cell parent(Cell item) {
        return item.parent().get();
      }
    };
  }

  void childAdded(IndentPart child) {
    if (isCellDescendant(child)) return;
    myUpdater.childAdded(child);
  }

  void childRemoved(IndentPart child) {
    if (isCellDescendant(child)) return;
    myUpdater.childRemoved(child);
  }

  void visibilityChanged(IndentPart child, PropertyChangeEvent<Boolean> event) {
    if (isCellDescendant(child)) return;
    myUpdater.visibilityChanged(child, event);
  }

  private boolean isCellDescendant(IndentPart child) {
    IndentPart parent = child.parent().get();
    if (parent instanceof CellPart) return true;
    if (parent instanceof RootIndentPart) return false;
    return isCellDescendant(parent);
  }

  public IndentPart root() {
    return myRootPart;
  }
}