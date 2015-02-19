/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.cell.indent.updater;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.indent.NewLineCell;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.*;

public class IndentUpdater<TargetT> {
  private Cell myRoot;
  private Map<Cell, TargetT> myNewLineToLine = new HashMap<>();
  private Map<Cell, CellWrapper<TargetT>> myWrappers = new HashMap<>();
  private TargetT myTarget;
  private IndentUpdaterTarget<TargetT> myIndentUpdaterTarget;
  private Cell myJustBecameInvisible;
  private Map<Cell, Registration> myChildRegistrations = new HashMap<>();
  private boolean myInitialized;

  private Set<Cell> myAttached = new HashSet<>();

  public IndentUpdater(
      Cell root,
      TargetT target,
      IndentUpdaterTarget<TargetT> indentUpdaterTarget) {
    myIndentUpdaterTarget = indentUpdaterTarget;
    myRoot = root;
    myTarget = target;

    children(myTarget).add(myIndentUpdaterTarget.newLine());
  }

  public boolean isInitialized() {
    return myInitialized;
  }

  public void initialized() {
    myInitialized = true;
  }

  public void childAdded(Cell child) {
    childAdded(child, true);
  }

  private void childAdded(Cell child, boolean real) {
    onChildAdd(child);

    if (real) {
      if (myChildRegistrations.containsKey(child)) {
        throw new IllegalStateException();
      }
      myChildRegistrations.put(child, watch(child));
    }

    if (!isCell(child)) {
      List<Cell> children = child.children();
      for (Cell c : children) {
        childAdded(c, real);
      }
    }
  }

  public void childRemoved(Cell child) {
    childRemoved(child, true);
  }

  private void childRemoved(Cell child, boolean real) {
    List<Cell> children = child.children();

    if (real) {
      myChildRegistrations.remove(child).remove();
    }

    if (!isCell(child)) {
      for (int i = children.size() - 1; i >= 0; i--) {
        Cell c = children.get(i);
        childRemoved(c, real);
      }
    }
    onChildRemove(child);
  }

  public void visibilityChanged(Cell item, PropertyChangeEvent<Boolean> change) {
    if (item == root()) return;

    if (change.getNewValue()) {
      childAdded(item, false);
    } else {
      myJustBecameInvisible = item;
      try {
        childRemoved(item, false);
      } finally {
        myJustBecameInvisible = null;
      }
    }
  }

  private void onChildAdd(Cell child) {
    if (!isVisible(child)) return;

    if (isAttached(child)) {
      throw new IllegalStateException("child " + child + " is already attached");
    }

    setAttached(child, true);

    Position insertAt = new Position(this, child);
    Position prevNewLinePos = prevNewLine(insertAt.prev());

    if (isCell(child)) {
      CellWrapper<TargetT> wrapper = myIndentUpdaterTarget.wrap(child);
      myWrappers.put(child, wrapper);

      if (prevNewLinePos == null) {
        Position first = new Position(this, firstLeaf(myRoot));
        children(children(myTarget).get(0)).add(first.deltaTo(insertAt), wrapper.item());
      } else {
        TargetT targetLine = myNewLineToLine.get(prevNewLinePos.get());
        int indentDelta = indent(prevNewLinePos.get()) > 0 ? 1 : 0;
        children(targetLine).add(prevNewLinePos.deltaTo(insertAt) - 1 + indentDelta, wrapper.item());
      }
    } else if (child instanceof NewLineCell) {
      Position nextNewLinePos = nextNewLine(insertAt.next());

      TargetT newLine = myIndentUpdaterTarget.newLine();
      int indent = indent(child);

      if (indent > 0) {
        TargetT indentItem = myIndentUpdaterTarget.newIndent(indent);
        children(newLine).add(indentItem);
      }

      myNewLineToLine.put(child, newLine);

      if (prevNewLinePos == null) {
        children(myTarget).add(1, newLine);
      } else {
        TargetT prevLine = myNewLineToLine.get(prevNewLinePos.get());

        //this is optimization for the initalization case
        int prevLineIndex = children(myTarget).lastIndexOf(prevLine);
        children(myTarget).add(prevLineIndex + 1, newLine);
      }

      Iterator<Position> positions = nextNewLinePos == null ? toEnd(insertAt.next()) : range(insertAt.next(), nextNewLinePos);
      while (positions.hasNext()) {
        Cell part = positions.next().get();
        if (!isCell(part)) continue;
        CellWrapper<TargetT> wrapper = myWrappers.get(part);
        TargetT item = wrapper.item();
        removeFromParent(item);
        children(newLine).add(item);
      }
    }
  }

  private void onChildRemove(Cell child) {
    if (!isVisible(child)) return;

    if (!isAttached(child)) {
      throw new IllegalStateException("child " + child + " is already detached");
    }

    Position removeAt = new Position(this, child);
    Position prevNewLinePos = prevNewLine(removeAt.prev());

    if (isCell(child)) {
      if (prevNewLinePos == null) {
        Position first = new Position(this, firstLeaf(myRoot));
        children(children(myTarget).get(0)).remove(first.deltaTo(removeAt));
      } else {
        TargetT line = myNewLineToLine.get(prevNewLinePos.get());
        children(line).remove(prevNewLinePos.deltaTo(removeAt) - 1 + (indent(child) > 0 ? 1 : 0));
      }
      myWrappers.remove(child).remove();
    } else if (child instanceof NewLineCell) {
      TargetT lineCell = myNewLineToLine.remove(child);

      if (lineCell == null) {
        throw new IllegalStateException();
      }

      if (indent(child) > 0) {
        children(lineCell).remove(0);
      }

      removeFromParent(lineCell);

      TargetT mergeWith;
      if (prevNewLinePos == null) {
        mergeWith = children(myTarget).get(0);
      } else {
        mergeWith = myNewLineToLine.get(prevNewLinePos.get());
      }

      for (TargetT c : new ArrayList<>(children(lineCell))) {
        removeFromParent(c);
        children(mergeWith).add(c);
      }
    }

    setAttached(child, false);
  }

  private void removeFromParent(TargetT c) {
    children(myIndentUpdaterTarget.parent(c)).remove(c);
  }

  private Position prevNewLine(Position from) {
    Position current = from;
    while (current != null) {
      if (current.get() instanceof NewLineCell) {
        return current;
      }
      current = current.prev();
    }
    return null;
  }

  private Position nextNewLine(Position from) {
    Position current = from;
    while (current != null) {
      if (current.get() instanceof NewLineCell) {
        return current;
      }
      current = current.next();
    }
    return null;
  }

  private Iterator<Position> toEnd(final Position pos) {
    return new Iterator<Position>() {
      private Position myCurrent = pos;

      @Override
      public boolean hasNext() {
        return myCurrent != null;
      }

      @Override
      public Position next() {
        Position result = myCurrent;
        myCurrent = myCurrent.next();
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private Iterator<Position> range(final Position from, final Position to) {
    return new Iterator<Position>() {
      private Position myCurrent = from;

      @Override
      public boolean hasNext() {
        return !myCurrent.equals(to);
      }

      @Override
      public Position next() {
        Position result = myCurrent;
        myCurrent = myCurrent.next();
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  private int indent(Cell part) {
    int result = 0;
    Cell current = part.getParent();
    Cell prevCurrent = part;
    while (current != myRoot) {
      Position prevNewLine = prevNewLine(new Position(this, prevCurrent));
      if (isIndented(current) && prevNewLine != null && prevNewLine.get().getParent() == current) {
        result++;
      }

      prevCurrent = current;
      current = current.getParent();
    }
    return result;
  }

  private Cell firstLeaf(Cell item) {
    if (isCell(item) || item.children().isEmpty()) return item;
    for (Cell child : item.children()) {
      if (isVisible(child)) return firstLeaf(child);
    }
    throw new IllegalStateException();
  }

  Cell root() {
    return myRoot;
  }

  boolean isVisible(Cell source) {
    Cell current = source;
    while (current != myRoot) {
      if (current == null) {
        throw new IllegalStateException("Can't find a root indent container for " + source);
      }
      if (!current.get(Cell.VISIBLE) && current != myJustBecameInvisible) return false;
      current = current.getParent();
    }
    return true;
  }

  private List<TargetT> children(TargetT target) {
    return myIndentUpdaterTarget.children(target);
  }

  boolean isAttached(Cell src) {
    return myAttached.contains(src);
  }

  private void setAttached(final Cell src, boolean value) {
    if (value) {
      myAttached.add(src);
    } else {
      myAttached.remove(src);
    }
  }

  boolean isCell(Cell source) {
    return !(source instanceof IndentCell);
  }

  private Registration watch(final Cell child) {
    if (isCell(child)) {
      return child.visible().addHandler(new EventHandler<PropertyChangeEvent<Boolean>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Boolean> event) {
          onVisibilityChanged(child, event);
        }
      });
    } else {
      return Registration.EMPTY;
    }
  }

  private boolean isIndented(Cell src) {
    if (src instanceof IndentCell) {
      return ((IndentCell) src).isIndented();
    }

    return false;
  }


  protected void onVisibilityChanged(Cell cell, PropertyChangeEvent<Boolean> event) {
  }
}