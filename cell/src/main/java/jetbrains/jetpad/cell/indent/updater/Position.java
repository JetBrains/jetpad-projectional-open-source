/*
 * Copyright 2012-2014 JetBrains s.r.o
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

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.model.composite.Composites;

import java.util.Iterator;

class Position {
  private IndentUpdater<?> myUpdater;
  private Cell myCell;

  Position(IndentUpdater<?> updater, Cell cell) {
    myUpdater = updater;
    myCell = cell;
  }

  Position next() {
    Cell current = next(myCell);
    if (current == null) return null;
    return new Position(myUpdater, current);
  }

  Position prev() {
    Cell current = prev(myCell);
    if (current == null) return null;
    return new Position(myUpdater, current);
  }

  Cell get() {
    return myCell;
  }

  int deltaTo(Position to) {
    if (to.myCell == myCell) return 0;
    if (Composites.isBefore(myCell, to.myCell)) {
      int count = 0;
      for (Cell p : nextLeaves(myCell)) {
        count++;
        if (p == to.myCell) break;
      }
      return count;
    } else {
      return -to.deltaTo(this);
    }
  }

  private Cell next(Cell item) {
    Cell next = nextVisibleLeaf(item);
    if (next == null) return null;
    Cell parentCell = upperMostCell(next);
    if (parentCell != null) return parentCell;
    return next;
  }

  private Cell prev(Cell item) {
    Cell prev = prevVisibleLeaf(item);
    if (prev == null) return null;
    Cell parentCell = upperMostCell(prev);
    if (parentCell != null) return parentCell;
    return prev;
  }

  private Cell upperMostCell(Cell item) {
    Cell current = item.getParent();
    Cell upperMostCell = item;
    Cell root = myUpdater.root();
    while (current != root) {
      if (current == null) {
        throw new IllegalStateException();
      }
      if (myUpdater.isCell(current)) {
        upperMostCell = current;
      }
      current = current.getParent();
    }
    return upperMostCell;
  }

  private Iterable<Cell> nextLeaves(final Cell item) {
    return new Iterable<Cell>() {
      @Override
      public Iterator<Cell> iterator() {
        return new Iterator<Cell>() {
          private Cell myCurrentLeaf = item;
          private Cell myNextLeaf = Position.this.next(item);

          @Override
          public boolean hasNext() {
            return myNextLeaf != null;
          }

          @Override
          public Cell next() {
            myCurrentLeaf = myNextLeaf;
            myNextLeaf = Position.this.next(myCurrentLeaf);
            return myCurrentLeaf;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  private Cell nextVisibleLeaf(Cell item) {
    Cell root = myUpdater.root();
    Cell current = nextLeaf(item, root);
    while (current != null) {
      if (myUpdater.isVisible(current)) return current;
      current = nextLeaf(current, root);
    }
    return null;
  }

  private Cell prevVisibleLeaf(Cell item) {
    Cell root = myUpdater.root();
    Cell current = prevLeaf(item, root);
    while (current != null) {
      if (myUpdater.isVisible(current)) return current;
      current = prevLeaf(current, root);
    }
    return null;
  }

  private Cell firstChild(Cell p) {
    Cell current = p.firstChild();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.nextSibling();
    }
    return null;
  }

  private Cell lastChild(Cell p) {
    Cell current = p.lastChild();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.prevSibling();
    }
    return null;
  }

  private Cell prevSibling(Cell c) {
    Cell current = c.prevSibling();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.prevSibling();
    }
    return null;
  }

  private Cell nextSibling(Cell c) {
    Cell current = c.nextSibling();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      if (!myUpdater.isInitialized() && myUpdater.isVisible(current)) return null;
      current = current.nextSibling();
    }
    return null;
  }

  private Cell firstLeaf(Cell p) {
    Cell first = firstChild(p);
    if (first == null) return p;
    return firstLeaf(first);
  }

  private Cell lastLeaf(Cell p) {
    Cell last = lastChild(p);
    if (last == null) return p;
    return lastLeaf(last);
  }

  private Cell nextLeaf(Cell c, Cell within) {
    Cell current = c;
    while (true) {
      Cell nextSibling = nextSibling(current);
      if (nextSibling != null) {
        return firstLeaf(nextSibling);
      }
      Cell parent = current.getParent();
      if (parent == within) return null;
      current = parent;
    }
  }

  private Cell prevLeaf(Cell c, Cell within) {
    Cell current = c;
    while (true) {
      Cell prevSibling = prevSibling(current);
      if (prevSibling != null) {
        return lastLeaf(prevSibling);
      }

      Cell parent = current.getParent();
      if (parent == within) return null;
      current = parent;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Position)) return false;
    return myCell == ((Position) obj).myCell;
  }

  @Override
  public int hashCode() {
    return myCell.hashCode();
  }
}