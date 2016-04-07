/*
 * Copyright 2012-2016 JetBrains s.r.o
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
package jetbrains.jetpad.cell;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Pair;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.CompositeRegistration;

import java.util.HashMap;
import java.util.Map;

public class TraitPropagator {

  public static final Predicate<Cell> NOT_POPUP = new Predicate<Cell>() {
    @Override
    public boolean apply(Cell cell) {
      return !Cells.inPopup(cell);
    }
  };

  public static final Handler<Cell> EMPTY_CELL_HANDLER = new Handler<Cell>() {
    @Override
    public void handle(Cell item) {
    }
  };

  public static Registration install(CellContainer container, CellTrait trait, CellPropertySpec<Boolean> hasCurrentTraitProp) {
    return install(container, trait, hasCurrentTraitProp, NOT_POPUP, EMPTY_CELL_HANDLER);
  }

  public static Registration install(CellContainer container, CellTrait trait,
      CellPropertySpec<Boolean> hasCurrentTraitProp, Predicate<Cell> watchPredicate, Handler<Cell> detachHandler) {
    return installWithSizeSupplier(container, trait, hasCurrentTraitProp, watchPredicate, detachHandler).first;
  }

  // for test
  static Pair<CompositeRegistration, Supplier<Integer>> installWithSizeSupplier(
      CellContainer container, final CellTrait trait, CellPropertySpec<Boolean> hasCurrentTraitProp,
      Predicate<Cell> watchPredicate, Handler<Cell> detachHandler) {

    if (container.root.get(hasCurrentTraitProp)) {
      throw new IllegalArgumentException("Controller(" + hasCurrentTraitProp + ") is already installed");
    }
    CompositeRegistration result = new CompositeRegistration();
    result.add(container.root.set(hasCurrentTraitProp, true));
    final MyChildrenListener listener = new MyChildrenListener(container, trait, watchPredicate, detachHandler);
    result.add(container.addListener(listener));
    result.add(new Registration() {
      @Override
      protected void doRemove() {
        listener.dispose();
      }
    });

    return new Pair<>(result, listener.registrationsSizeSupplier());
  }

  private TraitPropagator() {
  }

  private static class MyChildrenListener extends CellContainerAdapter implements Disposable {
    private final CellTrait myTrait;
    private final Predicate<Cell> myWatchPredicate;
    private final Handler<Cell> myCellDetachHandler;
    private Map<Cell, Registration> myRegistrations;
    private final Handler<Cell> myAttachHandler;
    private final Handler<Cell> myDetachHandler = new Handler<Cell>() {
      @Override
      public void handle(Cell cell) {
        if (myRegistrations == null) return;
        Registration registration = myRegistrations.remove(cell);
        if (registration != null) {
          registration.remove();
        }
        if (myRegistrations.isEmpty()) {
          myRegistrations = null;
        }
      }
    };

    private MyChildrenListener(CellContainer container, CellTrait trait, Predicate<Cell> watchPredicate,
        Handler<Cell> detachHandler) {
      myTrait = trait;
      myWatchPredicate = watchPredicate;
      myCellDetachHandler = detachHandler;
      myAttachHandler = new Handler<Cell>() {
        @Override
        public void handle(Cell item) {
          doAttach(item);
        }
      };
      visit(container.root, myAttachHandler);
    }

    private void doAttach(final Cell cell) {
      if (!myWatchPredicate.apply(cell)) {
        return;
      }
      if (myRegistrations != null && myRegistrations.containsKey(cell)) {
        throw new IllegalStateException(cell + " is already registered in trait-based controller");
      }
      final Registration decorationReg = cell.addTrait(myTrait);
      if (myRegistrations == null) {
        myRegistrations = new HashMap<>();
      }
      myRegistrations.put(cell, new Registration() {
        @Override
        protected void doRemove() {
          decorationReg.remove();
          myCellDetachHandler.handle(cell);
        }
      });
    }

    @Override
    public void onChildAdded(Cell parent, CollectionItemEvent<? extends Cell> change) {
      visit(change.getNewItem(), myAttachHandler);
    }

    @Override
    public void onChildRemoved(Cell parent, CollectionItemEvent<? extends Cell> change) {
      visit(change.getOldItem(), myDetachHandler);
    }

    private void visit(Cell cell, Handler<Cell> handler) {
      handler.handle(cell);
      for (Cell child : cell.children()) {
        visit(child, handler);
      }
    }

    @Override
    public void dispose() {
      if (myRegistrations == null) return;
      for (Cell cell : myRegistrations.keySet()) {
        myRegistrations.get(cell).remove();
      }
      myRegistrations = null;
    }

    Supplier<Integer> registrationsSizeSupplier() {
      return new Supplier<Integer>() {
        @Override
        public Integer get() {
          return myRegistrations == null ? 0 : myRegistrations.size();
        }
      };
    }
  }
}
