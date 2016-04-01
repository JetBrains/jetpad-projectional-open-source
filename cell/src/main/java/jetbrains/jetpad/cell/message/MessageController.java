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
package jetbrains.jetpad.cell.message;

import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellContainerAdapter;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.event.CompositeRegistration;

import java.util.HashMap;
import java.util.Map;

public final class MessageController {
  private static final CellTraitPropertySpec<MessageController> TRAIT = new CellTraitPropertySpec<>("messageController");
  static final CellPropertySpec<String> ERROR = new CellPropertySpec<>("error");
  static final CellPropertySpec<String> WARNING = new CellPropertySpec<>("warning");
  static final CellPropertySpec<String> BROKEN = new CellPropertySpec<>("broken");
  static final CellPropertySpec<String> INFO = new CellPropertySpec<>("info");

  public static Registration install(CellContainer container) {
    return install(container, null);
  }

  public static Registration install(CellContainer container, MessageStyler styler) {
    MessageController controller = new MessageController(container);
    MessageTrait trait = new MessageTrait(container, new StyleApplicator(styler));
    return controller.install(trait);
  }

  public static void setBroken(Cell cell, String message) {
    set(cell, message, BROKEN);
  }

  public static void setError(Cell cell, String message) {
    set(cell, message, ERROR);
  }

  public static void setWarning(Cell cell, String message) {
    set(cell, message, WARNING);
  }

  public static void setInfo(Cell cell, String message) {
    set(cell, message, INFO);
  }

  static void set(Cell cell, String message, CellPropertySpec<String> prop) {
    if (canHaveMessages(cell)) {
      cell.set(prop, message);
    }
  }

  private static boolean canHaveMessages(Cell cell) {
    return !Cells.inPopup(cell);
  }

  public static MessageController getController(Cell cell) {
    if (!cell.isAttached()) {
      throw new IllegalStateException();
    }
    return getController(cell.cellContainer().get());
  }

  public static MessageController getController(CellContainer container) {
    return container.root.get(MessageController.TRAIT);
  }

  public static boolean isBroken(Cell cell) {
    return cell.get(BROKEN) != null;
  }

  public static boolean hasWarning(Cell cell) {
    return cell.get(WARNING) != null;
  }

  public static boolean hasError(Cell cell) {
    return cell.get(ERROR) != null;
  }

  public static boolean hasInfo(Cell cell) {
    return cell.get(INFO) != null;
  }

  private CellContainer myContainer;
  private MyChildrenListener myChildrenListener;

  private MessageController(CellContainer container) {
    if (container.root.get(TRAIT) != null) {
      throw new IllegalArgumentException("Message controller is already installed");
    }
    myContainer = container;
  }

  private CompositeRegistration install(MessageTrait trait) {
    CompositeRegistration result = new CompositeRegistration();

    result.add(myContainer.root.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == TRAIT) {
          return MessageController.this;
        }
        return super.get(cell, spec);
      }
    }));

    myChildrenListener = new MyChildrenListener(myContainer, trait);
    result.add(myContainer.addListener(myChildrenListener));
    result.add(new Registration() {
      @Override
      protected void doRemove() {
        myChildrenListener.dispose();
      }
    });

    return result;
  }

  // for tests
  int getDecoratedCellsCount() {
    return myChildrenListener.myRegistrations.size();
  }

  private static class MyChildrenListener extends CellContainerAdapter implements Disposable {
    private MessageTrait myTrait;
    private Map<Cell, Registration> myRegistrations;

    private Handler<Cell> myAttachHandler = new Handler<Cell>() {
      @Override
      public void handle(final Cell cell) {
        if (!canHaveMessages(cell)) {
          return;
        }
        if (myRegistrations != null && myRegistrations.containsKey(cell)) {
          throw new IllegalStateException();
        }
        final Registration decorationReg = cell.addTrait(myTrait);
        if (myRegistrations == null) {
          myRegistrations = new HashMap<>();
        }
        myRegistrations.put(cell, new Registration() {
          @Override
          protected void doRemove() {
            decorationReg.remove();
            myTrait.detach(cell);
          }
        });
      }
    };

    private Handler<Cell> myDetachHandler = new Handler<Cell>() {
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

    private MyChildrenListener(CellContainer container, MessageTrait trait) {
      myTrait = trait;
      visit(container.root, myAttachHandler);
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
  }
}