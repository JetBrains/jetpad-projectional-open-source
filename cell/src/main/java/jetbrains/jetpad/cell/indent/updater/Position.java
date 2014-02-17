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

import jetbrains.jetpad.model.composite.Composite;
import jetbrains.jetpad.model.composite.Composites;

import java.util.Iterator;

class Position<SourceCT extends Composite<SourceCT>> {
  //todo position can be substantially optimized by storing a list of child positions from root to the bottom
  private IndentUpdater<SourceCT, ?> myUpdater;
  private SourceCT myPart;

  Position(IndentUpdater<SourceCT, ?> updater, SourceCT part) {
    myUpdater = updater;
    myPart = part;
  }

  Position<SourceCT> next() {
    SourceCT current = next(myPart);
    while (current != null && !myUpdater.isAttached(current)) {
      current = next(current);
    }
    if (current == null) return null;
    return new Position<SourceCT>(myUpdater, current);
  }

  Position<SourceCT> prev() {
    SourceCT current = prev(myPart);
    while (current != null && !myUpdater.isAttached(current)) {
      current = prev(current);
    }
    if (current == null) return null;
    return new Position<SourceCT>(myUpdater, current);
  }

  SourceCT get() {
    return myPart;
  }

  int deltaTo(Position<SourceCT> to) {
    if (to.myPart == myPart) return 0;
    if (Composites.isBefore(myPart, to.myPart)) {
      int count = 0;
      for (SourceCT p : nextLeaves(myPart)) {
        count++;
        if (p == to.myPart) break;
      }
      return count;
    } else {
      return -to.deltaTo(this);
    }
  }

  private SourceCT next(SourceCT item) {
    SourceCT next = nextVisibleLeaf(item);
    if (next == null) return null;
    SourceCT parentCell = upperMostCell(next);
    if (parentCell != null) return parentCell;
    return next;
  }

  private SourceCT prev(SourceCT item) {
    SourceCT prev = prevVisibleLeaf(item);
    if (prev == null) return null;
    SourceCT parentCell = upperMostCell(prev);
    if (parentCell != null) return parentCell;
    return prev;
  }

  private SourceCT nextVisibleLeaf(SourceCT item) {
    SourceCT current = Composites.nextLeaf(item);
    while (current != null) {
      if (myUpdater.isVisible(current)) return current;
      current = Composites.nextLeaf(current);
    }
    return null;
  }

  private SourceCT prevVisibleLeaf(SourceCT item) {
    SourceCT current = Composites.prevLeaf(item);
    while (current != null) {
      if (myUpdater.isVisible(current)) return current;
      current = Composites.prevLeaf(current);
    }
    return null;
  }

  private SourceCT upperMostCell(SourceCT item) {
    SourceCT current = item.parent().get();
    SourceCT upperMostCell = item;
    while (current != myUpdater.root()) {
      if (current == null) throw new IllegalStateException();
      if (myUpdater.isCell(current)) {
        upperMostCell = current;
      }
      current = current.parent().get();
    }
    return upperMostCell;
  }

  private Iterable<SourceCT> nextLeaves(final SourceCT item) {
    return new Iterable<SourceCT>() {
      @Override
      public Iterator<SourceCT> iterator() {
        return new Iterator<SourceCT>() {
          private SourceCT myCurrentLeaf = item;
          private SourceCT myNextLeaf = Position.this.next(item);

          @Override
          public boolean hasNext() {
            return myNextLeaf != null;
          }

          @Override
          public SourceCT next() {
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

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Position)) return false;
    return myPart == ((Position<SourceCT>) obj).myPart;
  }

  @Override
  public int hashCode() {
    return myPart.hashCode();
  }
}