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
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.CellLists;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;
import jetbrains.jetpad.projectional.util.RootController;
import jetbrains.jetpad.cell.util.Validators;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(Parameterized.class)
public class ProjectionalListSynchronizerTest extends EditingTestCase {
  @Parameterized.Parameters
  public static List<Boolean[]> parameters() {
    return Arrays.asList(new Boolean[] { true }, new Boolean[] { false });
  }

  private static final ContentKind<Child> KIND = ContentKinds.create("child");

  private boolean myWithSeparator;

  private Container container = new Container();
  private ContainerMapper rootMapper;

  public ProjectionalListSynchronizerTest(boolean withSeparator) {
    myWithSeparator = withSeparator;
  }

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
    enter();
    assertEmpty(0);
  }

  @Test
  public void insertWithInsert() {
    insert();
    assertEmpty(0);
  }

  @Test
  public void insertWithShiftEnter() {
    shiftEnter();
    assertEmpty(0);
  }

  @Test
  public void insertWithCompletion() {
    type("item");
    complete();
    assertNonEmpty(0);
  }

  @Test
  public void insertAfter() {
    container.children.add(new EmptyChild());
    selectChild(0);
    enter();

    assertEquals(2, container.children.size());
    assertFocused(1);
  }

  @Test
  public void insertBefore() {
    container.children.add(new EmptyChild());
    selectChild(0);
    insert();

    assertEquals(2, container.children.size());
    assertFocused(0);
  }

  @Test
  public void insertInTheMiddle() {
    container.children.add(new NonEmptyChild());
    TextCell v = (TextCell) getChild(0);
    v.caretPosition().set(1);
    v.focus();

    enter();

    assertEquals(2, container.children.size());
    assertFocused(1);
  }

  @Test
  public void enterInFirstPositionInNonEmpty() {
    NonEmptyChild nec = new NonEmptyChild();
    container.children.add(nec);
    TextCell v = (TextCell) getChild(0);
    v.caretPosition().set(0);
    v.focus();

    enter();

    assertEquals(2, container.children.size());
    assertFocused(1);
    assertSame(nec, container.children.get(1));
  }

  @Test
  public void lastItemDelete() {
    container.children.add(new NonEmptyChild());
    selectChild(0);

    backspace();

    assertTrue(container.children.isEmpty());
  }

  @Test
  public void firstItemDeleteInCaseOfTwoChildren() {
    container.children.addAll(Arrays.asList(new ComplexChild(), new NonEmptyChild(), new NonEmptyChild()));
    selectChild(0);

    del();

    assertEquals(2, container.children.size());
    assertFocusedHome(0);
  }

  @Test
  public void tailItemDelete() {
    container.children.addAll(Arrays.asList(new NonEmptyChild(), new NonEmptyChild()));

    selectChild(1);
    getChild(1).get(PositionHandler.PROPERTY).end();

    del();

    assertFocused(0);
  }

  @Test
  public void middleItemDelete() {
    container.children.addAll(Arrays.asList(new NonEmptyChild(), new NonEmptyChild()));
    selectChild(0);

    del();

    assertFocused(0);
  }

  @Test
  public void backspaceInFirstPosition() {
    NonEmptyChild c2 = new NonEmptyChild();
    container.children.addAll(Arrays.asList(new NonEmptyChild(), c2));

    selectFirst(1);

    backspace();

    assertFocused(0);

    assertSame(c2, container.children.get(0));
  }

  @Test
  public void deleteInLastPosition() {
    NonEmptyChild c1 = new NonEmptyChild();
    container.children.addAll(Arrays.asList(c1, new NonEmptyChild()));

    selectLast(0);

    del();

    assertFocused(0);
    assertSame(c1, container.children.get(0));
  }

  @Test
  public void backspaceInEmptyAfterNonEmpty() {
    NonEmptyChild c = new NonEmptyChild();
    container.children.addAll(Arrays.asList(c, new EmptyChild()));
    selectChild(1);

    backspace();

    assertFocused(0);
    assertSame(c, container.children.get(0));
  }

  @Test
  public void deleteInEmptyBeforeNonEmpty() {
    NonEmptyChild c = new NonEmptyChild();
    container.children.addAll(Arrays.asList(new EmptyChild(), c));
    selectChild(0);

    del();

    assertFocused(0);
    assertSame(c, container.children.get(0));
  }

  @Test
  public void backspaceInEmptySeq() {
    EmptyChild c = new EmptyChild();
    container.children.addAll(Arrays.asList(new EmptyChild(), c));
    selectChild(1);

    backspace();

    assertFocused(0);
    assertSame(c, container.children.get(0));
  }

  @Test
  public void backspaceInEmptyBetweenNonEmpty() {
    container.children.addAll(Arrays.asList(new NonEmptyChild(), new EmptyChild(), new NonEmptyChild()));
    selectChild(1);

    backspace();

    assertFocused(0);
    assertEquals(2, container.children.size());
  }

  @Test
  public void deleteInEmptySeq() {
    EmptyChild c = new EmptyChild();
    container.children.addAll(Arrays.asList(c, new EmptyChild()));
    selectChild(0);

    del();

    assertFocused(0);
    assertSame(c, container.children.get(0));
  }

  @Test
  public void controlSpaceInCaseSomethingTyped() {
    EmptyChild c = new EmptyChild();
    container.children.add(c);
    selectChild(0);

    type("ite");

    complete();

    assertEquals(1, container.children.size());
    assertTrue(container.children.get(0) instanceof NonEmptyChild);
  }

  @Test
  public void selectDown() {
    add3Items();
    selectFirst(0);

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(get(0));
  }

  @Test
  public void selectDownInEmptyList() {
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();

    press(Key.DOWN, ModifierKey.SHIFT);

    assertTrue(rootMapper.mySynchronizer.getSelectedItems().isEmpty());
  }

  @Test
  public void selectUpInEmptyList() {
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();

    press(Key.UP, ModifierKey.SHIFT);

    assertTrue(rootMapper.mySynchronizer.getSelectedItems().isEmpty());
  }

  @Test
  public void unselectDownLast() {
    add3Items();
    selectLast(2);

    press(Key.UP, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected();
  }

  @Test
  public void selectDownInCaseOfComplexChild() {
    add3ComplexItems();

    selectFirst(0);

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(get(0));
  }

  @Test
  public void multiSelectDownInCaseOfComplexChild() {
    add3ComplexItems();

    selectFirst(0);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(get(0), get(1));
  }

  @Test
  public void selectDownInComplexNonSelectableChild() {
    container.children.add(new ComplexNonSelectableChild());

    CellActions.toFirstFocusable(getChild(0)).run();

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(get(0));
  }

  @Test
  public void selectUp() {
    add3Items();
    selectChild(2);

    press(Key.UP, ModifierKey.SHIFT);

    assertSelected(get(1));
  }

  @Test
  public void unSelectDownInEnd() {
    add3Items();
    selectChild(2);

    press(Key.UP, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected();
  }

  @Test
  public void unSelectUp() {
    add3Items();
    selectChild(0);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.UP, ModifierKey.SHIFT);

    assertSelected();
  }

  @Test
  public void selectDownInsideSelectsChildViewFirst() {
    container.children.add(new ComplexChild());
    CellActions.toFirstFocusable(getChild(0)).run();

    assertTrue(rootMapper.mySynchronizer.getSelectedItems().isEmpty());

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(get(0));
  }

  @Test
  public void selectDownUnselectableChildManyItems() {
    ObservableList<Child> children = container.children;
    children.add(new NonSelectableChild());
    children.add(new NonSelectableChild());

    CellActions.toFirstFocusable(getChild(0)).run();

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(get(0));
  }

  @Test
  public void selectDownsMovesCaretToEndInCaseOfExtension() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell lastChild = (TextCell) getChild(1);
    lastChild.caretPosition().set(1);

    getChild(0).focus();

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertEquals(4, (int) lastChild.caretPosition().get());
  }

  @Test
  public void selectDownsMovesCaretToEndInCaseOfReduction() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell lastChild = (TextCell) getChild(1);
    lastChild.focus();

    press(Key.UP, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    assertEquals(0, (int) lastChild.caretPosition().get());
  }

  @Test
  public void selectUpMovesCaretToEndInCaseOfExtension() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell firstChild = (TextCell) getChild(0);
    firstChild.caretPosition().set(1);

    getChild(1).focus();

    press(Key.UP, ModifierKey.SHIFT);

    assertEquals(0, (int) firstChild.caretPosition().get());
  }

  @Test
  public void selectUpMovesCaretToEndInCaseOfReduction() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell firstChild = (TextCell) getChild(0);
    firstChild.focus();

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.UP, ModifierKey.SHIFT);

    assertEquals(4, (int) firstChild.caretPosition().get());
  }

  @Test
  public void programmaticSelectionChange() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    getChild(0).focus();

    rootMapper.mySynchronizer.select(children.get(0), children.get(1));

    assertSelected(get(0), get(1));
  }

  @Test(expected = IllegalArgumentException.class)
  public void incorrectArgsToProgrammaticSelectionChange() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    getChild(1).focus();

    rootMapper.mySynchronizer.select(children.get(0), children.get(0));
  }

  @Test
  public void selectionDelete() {
    EmptyChild c2 = new EmptyChild();
    EmptyChild c3 = new EmptyChild();
    container.children.addAll(Arrays.asList(new EmptyChild(), c2, c3));
    selectFirst(0);

    press(Key.DOWN, ModifierKey.SHIFT);
    del();

    assertEquals(Arrays.<Child>asList(c3), container.children);
  }

  @Test
  public void selectUpInEmpty() {
    EmptyChild c1 = new EmptyChild();
    EmptyChild c2 = new EmptyChild();
    container.children.addAll(Arrays.asList(c1, c2));

    selectFirst(1);

    press(Key.UP, ModifierKey.SHIFT);

    assertSelected(c1, c2);
  }

  @Test
  public void selectDownInEmptyChilden() {
    EmptyChild c1 = new EmptyChild();
    EmptyChild c2 = new EmptyChild();
    container.children.addAll(Arrays.asList(c1, c2));

    selectFirst(0);

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected(c1, c2);
  }

  @Test
  public void selectDownInPlaceholder() {
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();

    press(Key.DOWN, ModifierKey.SHIFT);

    assertSelected();
  }

  @Test
  public void selectUpAndNestedSelection() {
    CompositeChild cc = new CompositeChild();
    cc.children.add(new NonEmptyChild());
    container.children.addAll(Arrays.<Child>asList(new NonEmptyChild(), cc));
    selectFirst(0);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(Key.DOWN, ModifierKey.SHIFT);

    press(Key.UP, ModifierKey.SHIFT);

    assertSelected(get(0));
  }

  @Test
  public void copyPaste() {
    container.children.add(new EmptyChild());
    selectChild(0);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(KeyStrokeSpecs.COPY);
    press(KeyStrokeSpecs.PASTE);

    assertEquals(2, container.children.size());
  }


  @Test
  public void canCopyFocusedItem() {
    container.children.add(new ComplexChild());
    selectChild(0);

    press(KeyStrokeSpecs.COPY);
    press(KeyStrokeSpecs.PASTE);

    assertEquals(2, container.children.size());
  }

  @Test
  public void cut() {
    container.children.add(new EmptyChild());
    selectChild(0);

    press(Key.DOWN, ModifierKey.SHIFT);
    press(KeyStrokeSpecs.CUT);

    assertTrue(container.children.isEmpty());
  }

  private Child get(int index) {
    return container.children.get(index);
  }

  private void add3Items() {
    container.children.addAll(Arrays.<Child>asList(new NonEmptyChild(), new NonEmptyChild(), new NonEmptyChild()));
  }

  private void add3ComplexItems() {
    container.children.addAll(Arrays.<Child>asList(new ComplexChild(), new ComplexChild(), new ComplexChild()));
  }

  private void selectChild(int index) {
    getChild(index).focus();
  }

  private void selectFirst(int index) {
    Cell child = getChild(index);
    child.get(PositionHandler.PROPERTY).home();
    child.focus();
  }


  private void selectLast(int index) {
    Cell child = getChild(index);
    child.get(PositionHandler.PROPERTY).end();
    child.focus();
  }

  private void assertFocused(int index) {
    assertTrue(getChild(index).focused().get());
  }

  private void assertFocusedHome(int index) {
    Cell child = getChild(index);
    assertTrue(child.focused().get());
    assertTrue(child.get(PositionHandler.PROPERTY).isHome());
  }

  private void assertFocusedEnd(int index) {
    Cell child = getChild(index);
    assertTrue(child.focused().get());
    assertTrue(child.get(PositionHandler.PROPERTY).isEnd());
  }

  private void assertSelected(Child... expected) {
    assertEquals(Arrays.asList(expected), rootMapper.mySynchronizer.getSelectedItems());
  }

  private Cell getChild(int index) {
    Child child = container.children.get(index);
    return (Cell) rootMapper.getDescendantMapper(child).getTarget();
  }

  private void assertEmpty(int index) {
    assertTrue(container.children.get(index) instanceof EmptyChild);
  }

  private void assertNonEmpty(int index) {
    assertTrue(container.children.get(index) instanceof NonEmptyChild);
  }

  private MapperFactory<Child, Cell> createChildMapperFactory() {
    return new MapperFactory<Child, Cell>() {
      @Override
      public Mapper<? extends Child, ? extends Cell> createMapper(Child source) {
        if (source instanceof EmptyChild) {
          return new EmptyChildMapper((EmptyChild) source);
        }

        if (source instanceof NonEmptyChild) {
          return new NonEmptyChildMapper((NonEmptyChild) source);
        }

        if (source instanceof ComplexChild) {
          return new ComplexChildMapper((ComplexChild) source);
        }

        if (source instanceof NonSelectableChild) {
          return new NonSelectableChildMapper((NonSelectableChild) source);
        }

        if (source instanceof ComplexNonSelectableChild) {
          return new ComplexNonSelectableChildMapper((ComplexNonSelectableChild) source);
        }

        if (source instanceof CompositeChild) {
          return new CompositeChildMapper((CompositeChild) source);
        }

        return null;
      }
    };
  }

  private ProjectionalRoleSynchronizer<Object, Child> createSynchronizer(Mapper<?, ? extends Cell> contextMapper, Cell target, ObservableList<Child> list) {
    ProjectionalRoleSynchronizer<Object, Child> result = ProjectionalSynchronizers.<Object, Child>forRole(
      contextMapper,
      list,
      target,
      myWithSeparator ? CellLists.separated(target.children(), " ") : target.children(), createChildMapperFactory());

    result.setItemFactory(new Supplier<Child>() {
      @Override
      public Child get() {
        return new EmptyChild();
      }
    });
    result.setCompletion(new RoleCompletion<Object, Child>() {
      @Override
      public List<CompletionItem> createRoleCompletion(CompletionParameters ctx, Mapper<?, ?> mapper, Object contextNode, final Role<Child> target) {
        List<CompletionItem> result = new ArrayList<>();
        result.add(new SimpleCompletionItem("item") {
          @Override
          public Runnable complete(String text) {
            return target.set(new NonEmptyChild());
          }
        });
        return result;
      }
    });
    result.setClipboardParameters(KIND, new Function<Child, Child>() {
      @Override
      public Child apply(Child input) {
        return input;
      }
    });

    return result;
  }

  private class Container {
    final ObservableList<Child> children = new ObservableArrayList<>();
  }

  private abstract class Child {
  }

  private class EmptyChild extends Child {
  }

  private class NonEmptyChild extends Child {
  }

  private class ComplexChild extends Child {
  }

  private class CompositeChild extends Child {
    final ObservableList<Child> children = new ObservableArrayList<>();
  }


  private class NonSelectableChild extends Child {
  }

  private class ComplexNonSelectableChild extends Child {
  }


  private class ContainerMapper extends Mapper<Container, VerticalCell> {
    private ProjectionalRoleSynchronizer<Object, Child> mySynchronizer;

    ContainerMapper(Container source) {
      super(source, new VerticalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      conf.add(mySynchronizer = createSynchronizer(this, getTarget(), getSource().children));
    }
  }

  private class EmptyChildMapper extends Mapper<EmptyChild, TextCell> {
    EmptyChildMapper(EmptyChild source) {
      super(source, new TextCell());
      getTarget().text().set("");
      getTarget().addTrait(TextEditing.validTextEditing(Validators.equalsTo("")));
    }
  }

  private class NonEmptyChildMapper extends Mapper<NonEmptyChild, TextCell> {
    NonEmptyChildMapper(NonEmptyChild source) {
      super(source, new TextCell());
      getTarget().text().set("item");
      getTarget().addTrait(TextEditing.validTextEditing(Validators.equalsTo("item")));
    }
  }

  private class ComplexChildMapper extends Mapper<ComplexChild, ComplexChildCell> {
    private ComplexChildMapper(ComplexChild source) {
      super(source, new ComplexChildCell());
    }
  }

  private class ComplexChildCell extends HorizontalCell {
    private ComplexChildCell() {
      children().addAll(Arrays.asList(CellFactory.label("a"), CellFactory.label("b")));
      focusable().set(true);
    }
  }

  private class NonSelectableChildMapper extends Mapper<NonSelectableChild, NonSelectableChildCell> {
    private NonSelectableChildMapper(NonSelectableChild source) {
      super(source, new NonSelectableChildCell());
    }
  }

  private class NonSelectableChildCell extends HorizontalCell {
    private NonSelectableChildCell() {
      children().add(CellFactory.label("text"));
    }
  }

  private class ComplexNonSelectableChildCell extends HorizontalCell {
    private ComplexNonSelectableChildCell() {
      HorizontalCell horizontalView = new HorizontalCell();
      horizontalView.focusable().set(true);
      horizontalView.children().add(CellFactory.label("text"));

      children().add(horizontalView);
    }
  }

  private class ComplexNonSelectableChildMapper extends Mapper<ComplexNonSelectableChild, ComplexNonSelectableChildCell> {
    private ComplexNonSelectableChildMapper(ComplexNonSelectableChild source) {
      super(source, new ComplexNonSelectableChildCell());
    }
  }

  private class CompositeChildMapper extends Mapper<CompositeChild, HorizontalCell> {
    private CompositeChildMapper(CompositeChild source) {
      super(source, new HorizontalCell());
      getTarget().children().add(createInvisible());
      getTarget().children().add(new HorizontalCell());
      getTarget().children().add(createInvisible());
      getTarget().visible().set(true);
    }

    private HorizontalCell createInvisible() {
      HorizontalCell invisible = new HorizontalCell();
      invisible.visible().set(false);
      return invisible;
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      conf.add(createSynchronizer(this, getTarget().children().get(1), getSource().children));
    }
  }

}