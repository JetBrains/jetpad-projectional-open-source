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
package jetbrains.jetpad.projectional.cell;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.completion.CompletionItem;
import jetbrains.jetpad.cell.completion.CompletionParameters;
import jetbrains.jetpad.cell.completion.SimpleCompletionItem;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitEventSpec;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.cell.util.Validators;
import jetbrains.jetpad.base.Value;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ProjectionalPropertySynchronizerTest extends EditingTestCase {
  private static final ContentKind<Child> KIND = ContentKinds.create("child");

  private Container container = new Container();
  private ContainerMapper rootMapper;

  @Before
  public void init() {
    rootMapper = new ContainerMapper(container);
    rootMapper.attachRoot();
    myCellContainer.root.children().add(rootMapper.getTarget());
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();

    RootController.install(myCellContainer);
  }

  @Test
  public void insertWithEnter() {
    insert();
    assertNotNull(container.child.get());
  }

  @Test
  public void completionOnPlaceholder() {
    complete();
    down();
    enter();

    assertTrue(container.child.get() instanceof Child2);
  }

  @Test
  public void ctrlDeleteItem() {
    Child1 child = new Child1();
    container.child.set(child);
    focusChild(child);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertNull(container.child.get());
  }

  @Test
  public void deleteInTheMiddle() {
    Child3 child = new Child3();
    container.child.set(child);
    focusChild(child, 1);

    press(Key.DELETE);

    assertNull(container.child.get());
  }

  @Test
  public void backspaceInTheMiddle() {
    Child3 child = new Child3();
    container.child.set(child);
    focusChild(child, 1);

    press(Key.BACKSPACE);

    assertNull(container.child.get());
  }

  @Test
  public void backspaceInTheStartDoesntWork() {
    Child3 child = new Child3();
    container.child.set(child);
    focusChild(child, 0);

    press(Key.BACKSPACE);

    assertSame(child, container.child.get());
  }

  @Test
  public void deleteInTheEndDoesntWork() {
    Child3 child = new Child3();
    container.child.set(child);
    focusChild(child, 2);

    press(Key.DELETE);

    assertSame(child, container.child.get());
  }

  private void copyWorks(Runnable beforeCopyAction) {
    Child1 child = new Child1();
    container.child.set(child);
    focusChild(child);

    beforeCopyAction.run();

    press(KeyStrokeSpecs.COPY);
    press(Key.DELETE, ModifierKey.CONTROL);

    assertNull(container.child.get());

    press(KeyStrokeSpecs.PASTE);

    assertNotNull(container.child.get());
  }

  @Test
  public void copyPaste() {
    copyWorks(new Runnable() {
      @Override
      public void run() {
        press(Key.DOWN, ModifierKey.SHIFT);
      }
    });
  }

  @Test
  public void copyForFocusedItem() {
    copyWorks(new Runnable() {
      @Override
      public void run() {
      }
    });
  }

  @Test
  public void cut() {
    Child1 child = new Child1();
    container.child.set(child);
    focusChild(child);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(KeyStrokeSpecs.CUT);

    assertNull(container.child.get());
  }

  @Test
  public void emptyAutoDeletionIfSetup() {
    Child1 child = new Child1();
    container.child.set(child);
    Cell childCell = focusChild(child);
    CellActions.toEnd(childCell).run();

    backspace();
    backspace();

    assertNull(container.child.get());
  }

  @Test
  public void noEmptyAutoDeletionIfNotSetup() {
    Child2 child = new Child2();
    container.child.set(child);
    Cell childCell = focusChild(child);
    CellActions.toEnd(childCell).run();

    backspace();
    backspace();

    assertNotNull(container.child.get());
  }

  @Test
  public void becameEmptyEventIfAllowed() {
    final Value<Boolean> becameEmptyFired = new Value<Boolean>(false);
    rootMapper.getTarget().addTrait(new BaseCellTrait() {

      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == ProjectionalSynchronizers.DELETE_ON_EMPTY) {
          return true;
        }

        return super.get(cell, spec);
      }

      @Override
      public void onViewTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
        if (spec == Cells.BECAME_EMPTY) {
          becameEmptyFired.set(true);
          event.consume();
          return;
        }

        super.onViewTraitEvent(cell, spec, event);
      }
    });

    Child2 child = new Child2();
    container.child.set(child);
    focusChild(child);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertTrue(becameEmptyFired.get());
  }

  private Cell focusChild(Child child) {
    Cell childCell = (Cell) rootMapper.getDescendantMapper(child).getTarget();
    childCell.focus();
    return childCell;
  }

  private TextCell focusChild(Child child, int caret) {
    TextCell childView = (TextCell) rootMapper.getDescendantMapper(child).getTarget();
    childView.focus();
    childView.caretPosition().set(caret);
    return childView;
  }

  private class Container {
    final Property<Child> child = new ValueProperty<Child>();
  }

  private abstract class Child {
  }

  private class Child1 extends Child {
  }

  private class Child2 extends Child {
  }

  private class Child3 extends Child {
  }

  private class ContainerMapper extends Mapper<Container, VerticalCell> {
    private ContainerMapper(Container source) {
      super(source, new VerticalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);

      ProjectionalRoleSynchronizer<Object, Child> sync = ProjectionalSynchronizers.<Object, Child>forSingleRole(this, getSource().child, getTarget(), new MapperFactory<Child, Cell>() {
        @Override
        public Mapper<? extends Child, ? extends Cell> createMapper(Child source) {
          if (source instanceof Child1) {
            return new ChildMapper(source, "c1", true, true);
          }

          if (source instanceof Child2) {
            return new ChildMapper(source, "c2", true, false);
          }

          if (source instanceof Child3) {
            return new ChildMapper(source, "c3", false, false);
          }

          return null;
        }
      });
      sync.setItemFactory(new Supplier<Child>() {
        @Override
        public Child get() {
          return new Child1();
        }
      });
      sync.setCompletion(new RoleCompletion<Object, Child>() {
        @Override
        public List<CompletionItem> createRoleCompletion(CompletionParameters ctx, final Mapper<?, ?> mapper, final Object contextNode, final Role<Child> target) {
          List<CompletionItem> result = new ArrayList<CompletionItem>();
          result.add(new SimpleCompletionItem("c1") {
            @Override
            public Runnable complete(String text) {
              return target.set(new Child1());
            }
          });
          result.add(new SimpleCompletionItem("c2") {
            @Override
            public Runnable complete(String text) {
              return target.set(new Child2());
            }
          });
          return result;
        }
      });
      sync.setClipboardParameters(KIND, new Function<Child, Child>() {
        @Override
        public Child apply(Child input) {
          return input;
        }
      });
      conf.add(sync);
    }
  }

  private class ChildMapper extends Mapper<Child, TextCell> {
    private String myText;
    private boolean myAutoDelete;
    private boolean myEditable;

    ChildMapper(Child source, String text, boolean editable, boolean autoDelete) {
      super(source, new TextCell());
      myText = text;
      myEditable = editable;
      myAutoDelete = autoDelete;

      getTarget().text().set(myText);

      getTarget().addTrait(new BaseCellTrait() {
        @Override
        protected CellTrait[] getBaseTraits(Cell cell) {
          return new CellTrait[] {
            myEditable ? TextEditing.validTextEditing(Validators.equalsTo(myText)) : TextEditing.textNavigation(true, true)
          };
        }

        @Override
        public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == ProjectionalSynchronizers.DELETE_ON_EMPTY && myAutoDelete) {
            return true;
          }

          return super.get(cell, spec);
        }
      });
    }
  }
}