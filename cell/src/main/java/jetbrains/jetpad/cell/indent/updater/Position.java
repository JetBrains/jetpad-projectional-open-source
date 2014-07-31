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

import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.composite.NavComposite;

import java.util.Iterator;

class Position<SourceCT extends NavComposite<SourceCT>> {
  private IndentUpdater<SourceCT, ?> myUpdater;
  private SourceCT myPart;

  Position(IndentUpdater<SourceCT, ?> updater, SourceCT part) {
    myUpdater = updater;
    myPart = part;
  }

  Position<SourceCT> next() {
    SourceCT current = next(myPart);
    if (current == null) return null;
    return new Position<>(myUpdater, current);
  }

  Position<SourceCT> prev() {
    SourceCT current = prev(myPart);
    if (current == null) return null;
    return new Position<>(myUpdater, current);
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

  private SourceCT upperMostCell(SourceCT item) {
    SourceCT current = item.getParent();
    SourceCT upperMostCell = item;
    SourceCT root = myUpdater.root();
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

  private SourceCT nextVisibleLeaf(SourceCT item) {
    SourceCT current = nextLeaf(item, myUpdater.root());
    while (current != null) {
      if (myUpdater.isVisible(current)) return current;
      current = nextLeaf(current, myUpdater.root());
    }
    return null;
  }

  private SourceCT prevVisibleLeaf(SourceCT item) {
    SourceCT current = prevLeaf(item, myUpdater.root());
    while (current != null) {
      if (myUpdater.isVisible(current)) return current;
      current = prevLeaf(current, myUpdater.root());
    }
    return null;
  }

  private SourceCT firstChild(SourceCT p) {
    SourceCT current = p.firstChild();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.nextSibling();
    }
    return null;
  }

  private SourceCT lastChild(SourceCT p) {
    SourceCT current = p.lastChild();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.prevSibling();
    }
    return null;
  }

  private SourceCT prevSibling(SourceCT c) {
    SourceCT current = c.prevSibling();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.prevSibling();
    }
    return null;
  }

  private SourceCT nextSibling(SourceCT c) {
    SourceCT current = c.nextSibling();
    while (current != null) {
      if (myUpdater.isAttached(current)) return current;
      current = current.nextSibling();
    }
    return null;
  }

  private SourceCT firstLeaf(SourceCT p) {
    SourceCT first = firstChild(p);
    if (first == null) return p;
    return firstLeaf(first);
  }

  private SourceCT lastLeaf(SourceCT p) {
    SourceCT last = lastChild(p);
    if (last == null) return p;
    return lastLeaf(last);
  }

  private SourceCT nextLeaf(SourceCT c, SourceCT within) {
    SourceCT current = c;
    while (true) {
      SourceCT nextSibling = nextSibling(current);
      if (nextSibling != null) {
        return firstLeaf(nextSibling);
      }
      SourceCT parent = current.getParent();
      if (parent == within) return null;
      current = parent;
    }
  }

  private SourceCT prevLeaf(SourceCT c, SourceCT within) {
    SourceCT current = c;
    while (true) {
      SourceCT prevSibling = prevSibling(current);
      if (prevSibling != null) {
        return lastLeaf(prevSibling);
      }

      SourceCT parent = current.getParent();
      if (parent == within) return null;
      current = parent;
    }
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