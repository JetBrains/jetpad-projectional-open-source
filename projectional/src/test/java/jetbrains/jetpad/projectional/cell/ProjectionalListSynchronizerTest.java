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
package jetbrains.jetpad.projectional.cell;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import jetbrains.jetpad.base.*;
import jetbrains.jetpad.cell.*;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.util.CellLists;
import jetbrains.jetpad.cell.util.Cells;
import jetbrains.jetpad.completion.CompletionItem;
import jetbrains.jetpad.completion.CompletionParameters;
import jetbrains.jetpad.completion.CompletionSupplier;
import jetbrains.jetpad.completion.SimpleCompletionItem;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MapperFactory;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.projectional.generic.Role;
import jetbrains.jetpad.projectional.generic.RoleCompletion;
import jetbrains.jetpad.projectional.util.RootController;
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

  private final Function<String, Child> lineParser = new Function<String, Child>() {
    @Override
    public Child apply(String s) {
      switch (s) {
        case "EmptyChild":
          return new EmptyChild();
        case "NonEmptyChild":
          return new NonEmptyChild();
        default:
          return null;
      }
    }
  };

  private final Function<Iterable<String>, List<Child>> multilineParser = new Function<Iterable<String>, List<Child>>() {
    @Override
    public List<Child> apply(Iterable<String> items) {
      List<Child> children = Lists.newArrayList(Iterables.transform(items, new Function<String, Child>() {
        @Override
        public Child apply(String s) {
          return lineParser.apply(s);
        }
      }));
      if (children.contains(null)) {
        return null;
      } else {
        return children;
      }
    }
  };

  private final Function<Child, String> itemToString = new Function<Child, String>() {
    @Override
    public String apply(Child child) {
      return child.getClass().getSimpleName();
    }
  };

  private final Function<List<Child>, String> listToString = new Function<List<Child>, String>() {
    @Override
    public String apply(List<Child> children) {
      return TextContentHelper.joinLines(Iterables.transform(children, new Function<Child, String>() {
        @Override
        public String apply(Child child) {
          return child.getClass().getSimpleName();
        }
      }));
    }
  };

  private boolean myWithSeparator;

  private Container container = new Container();
  private ContainerMapper rootMapper;
  private Value<Boolean> itemHandlerWasCalled = new Value<>(false);

  public ProjectionalListSynchronizerTest(boolean withSeparator) {
    myWithSeparator = withSeparator;
  }

  @Before
  public void init() {
    rootMapper = new ContainerMapper(container);
    rootMapper.attachRoot();
    myCellContainer.root.children().add(rootMapper.getTarget());
    selectPlaceholder();

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
  public void insertAfterWithSeparator() {
    container.children.add(new EmptyChild());
    selectChild(0);
    type(',');

    assertEquals(2, container.children.size());
    assertFocused(1);
  }

  @Test
  public void insertWithActiveSelection() {
    container.children.add(new ComplexChild());

    selectChild(0);
    enter();
    assertEquals(2, container.children.size());
    assertFocused(1);
  }

  @Test
  public void insertWithActiveMultiSelection() {
    container.children.addAll(Arrays.asList(new ComplexChild(), new ComplexChild()));

    selectChild(0);
    press(KeyStrokeSpecs.SELECT_AFTER);
    enter();

    assertEquals(2, container.children.size());
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
  public void smartInsert() {
    enableItemHandler();

    container.children.add(new EmptyChild());

    selectChild(0);
    enter();

    assertTrue(container.children.isEmpty());
    assertTrue(itemHandlerWasCalled.get());
  }

  @Test
  public void smartInsertDoesntWorkWithSeparators() {
    enableItemHandler();

    container.children.add(new EmptyChild());
    container.parensVisible.set(true);

    selectChild(0);
    enter();

    assertEquals(2, container.children.size());
    assertFalse(itemHandlerWasCalled.get());
  }

  @Test
  public void smartInsertWorksWithIgnoredSeparators() {
    enableItemHandler();

    container.children.add(new EmptyChild());
    container.parensVisible.set(true);
    rootMapper.getTarget().set(ProjectionalSynchronizers.IGNORED_ON_BOUNDARY, true);

    selectChild(0);
    enter();

    assertTrue(container.children.isEmpty());
    assertTrue(itemHandlerWasCalled.get());
  }

  @Test
  public void smartInsertDoesntWorkInTheMiddle() {
    enableItemHandler();

    container.children.addAll(Arrays.asList(new EmptyChild(), new EmptyChild()));

    selectChild(0);
    enter();

    assertEquals(3, container.children.size());
    assertFalse(itemHandlerWasCalled.get());
  }

  @Test
  public void lastItemDelete() {
    container.children.add(new NonEmptyChild());
    selectChild(0);

    backspace();
    backspace();

    assertTrue(container.children.isEmpty());
  }

  @Test
  public void cancelDeleteSelectionWithEsc() {
    container.children.add(new NonEmptyChild());
    selectChild(0);

    backspace();
    escape();

    assertEquals(1, container.children.size());
  }

  @Test
  public void deleteSelectionInsideIndent() {
    CompositeChild cc = new CompositeChild();
    cc.children.add(new IndentChild());
    container.children.add(cc);

    CellActions.toFirstFocusable(getChild(0)).run();

    backspace();
    backspace();

    assertEquals(0, cc.children.size());
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
    del();

    assertFocusedEnd(0);
  }


  @Test
  public void tailItemDeleteManyItems() {
    container.children.addAll(Arrays.asList(new NonEmptyChild(), new NonEmptyChild(), new NonEmptyChild()));

    selectChild(2);

    press(Key.DELETE, ModifierKey.CONTROL);

    assertFocusedEnd(1) ;
  }

  @Test
  public void middleItemDelete() {
    container.children.addAll(Arrays.asList(new NonEmptyChild(), new NonEmptyChild(), new NonEmptyChild()));
    selectChild(1);

    del();

    assertFocusedHome(1);
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
  public void asyncCompletionInPlaceholder() {
    selectPlaceholder();

    complete();

    type("async");

    enter();

    assertEquals(1, container.children.size());
    assertTrue(container.children.get(0) instanceof NonEmptyChild);
  }

  @Test
  public void asyncCompletionInItem() {
    container.children.add(new EmptyChild());
    selectChild(0);

    complete();

    type("async");

    enter();

    assertTrue(container.children.get(0) instanceof NonEmptyChild);
  }

  @Test
  public void selectAfter() {
    add3Items();
    selectFirst(0);

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(get(0));
  }

  @Test
  public void selectAfterInEmptyList() {
    selectPlaceholder();

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertTrue(rootMapper.mySynchronizer.getSelectedItems().isEmpty());
  }

  @Test
  public void selectBeforeInEmptyList() {
    selectPlaceholder();

    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertTrue(rootMapper.mySynchronizer.getSelectedItems().isEmpty());
  }

  @Test
  public void unselectBeforeLast() {
    add3Items();
    selectLast(2);

    press(KeyStrokeSpecs.SELECT_BEFORE);
    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected();
  }

  @Test
  public void selectUpChangesSelectionInCaseOfComplexChild() {
    add3ComplexItems();

    selectFirst(0);
    press(KeyStrokeSpecs.SELECT_UP);

    assertSelected(get(0));
  }

  @Test
  public void selectDownResetsSelection() {
    add3ComplexItems();

    selectChild(0);
    press(KeyStrokeSpecs.SELECT_DOWN);

    assertSelected();
  }

  @Test
  public void selectAfterInCaseOfComplexChild() {
    add3ComplexItems();

    selectFirst(0);

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(get(0));
  }

  @Test
  public void selectAfterInComplexChild() {
    add3ComplexItems();

    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(get(0), get(1));
  }

  @Test
  public void selectBeforeInComplexChild() {
    add3ComplexItems();

    selectChild(1);

    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertSelected(get(0), get(1));
  }

  @Test
  public void multiSelectAfterInCaseOfComplexChild() {
    add3ComplexItems();

    selectFirst(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(get(0), get(1));
  }

  @Test
  public void selectAfterInComplexNonSelectableChild() {
    container.children.add(new ComplexNonSelectableChild());

    CellActions.toFirstFocusable(getChild(0)).run();

    press(KeyStrokeSpecs.SELECT_AFTER);

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
  public void unSelectAfterInEnd() {
    add3Items();
    selectChild(2);

    press(KeyStrokeSpecs.SELECT_BEFORE);
    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected();
  }

  @Test
  public void unSelectUp() {
    add3Items();
    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertSelected();
  }

  @Test
  public void selectBeforeConsumed() {
    add3Items();
    selectChild(2);

    assertConsumed(press(KeyStrokeSpecs.SELECT_BEFORE));
  }

  @Test
  public void selectAfterInsideSelectsChildViewFirst() {
    container.children.add(new ComplexChild());
    CellActions.toFirstFocusable(getChild(0)).run();

    assertTrue(rootMapper.mySynchronizer.getSelectedItems().isEmpty());

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(get(0));
  }

  @Test
  public void selectAfterUnselectableChildManyItems() {
    ObservableList<Child> children = container.children;
    children.add(new NonSelectableChild());
    children.add(new NonSelectableChild());

    CellActions.toFirstFocusable(getChild(0)).run();

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(get(0));
  }

  @Test
  public void selectAfterMovesCaretToEndInCaseOfExtension() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell lastChild = (TextCell) getChild(1);
    lastChild.caretPosition().set(1);

    getChild(0).focus();

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);

    assertEquals(4, (int) lastChild.caretPosition().get());
  }

  @Test
  public void selectAfterMovesCaretToEndInCaseOfReduction() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell lastChild = (TextCell) getChild(1);
    lastChild.focus();

    press(KeyStrokeSpecs.SELECT_BEFORE);
    press(KeyStrokeSpecs.SELECT_AFTER);

    assertEquals(0, (int) lastChild.caretPosition().get());
  }

  @Test
  public void selectBeforeMovesCaretToEndInCaseOfExtension() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell firstChild = (TextCell) getChild(0);
    firstChild.caretPosition().set(1);

    getChild(1).focus();

    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertEquals(0, (int) firstChild.caretPosition().get());
  }

  @Test
  public void selectBeforeMovesCaretToEndInCaseOfReduction() {
    ObservableList<Child> children = container.children;
    children.add(new NonEmptyChild());
    children.add(new NonEmptyChild());

    TextCell firstChild = (TextCell) getChild(0);
    firstChild.focus();

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_BEFORE);

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

    press(KeyStrokeSpecs.SELECT_AFTER);
    del();

    assertEquals(Arrays.<Child>asList(c3), container.children);
  }


  @Test
  public void selectAfterInDecoratedChild() {
    DecoratedChild c1 = new DecoratedChild();
    DecoratedChild c2 = new DecoratedChild();

    container.children.addAll(Arrays.asList(c1, c2));
    selectFirst(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    c1.after.set(true);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(c1, c2);
  }

  @Test
  public void selectBeforeInDecoratedChild() {
    DecoratedChild c1 = new DecoratedChild();
    DecoratedChild c2 = new DecoratedChild();

    container.children.addAll(Arrays.asList(c1, c2));
    selectLast(1);

    press(KeyStrokeSpecs.SELECT_BEFORE);
    c1.before.set(true);

    press(KeyStrokeSpecs.SELECT_BEFORE);
    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertSelected(c1, c2);
  }

  @Test
  public void selectBeforeInEmpty() {
    EmptyChild c1 = new EmptyChild();
    EmptyChild c2 = new EmptyChild();
    container.children.addAll(Arrays.asList(c1, c2));

    selectFirst(1);

    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertSelected(c1, c2);
  }

  @Test
  public void selectAfterInEmptyChilden() {
    EmptyChild c1 = new EmptyChild();
    EmptyChild c2 = new EmptyChild();
    container.children.addAll(Arrays.asList(c1, c2));

    selectFirst(0);

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected(c1, c2);
  }

  @Test
  public void selectAfterInPlaceholder() {
    selectPlaceholder();

    press(KeyStrokeSpecs.SELECT_AFTER);

    assertSelected();
  }

  private void selectPlaceholder() {
    CellActions.toFirstFocusable(rootMapper.getTarget()).run();
  }

  @Test
  public void selectBeforeAndNestedSelection() {
    CompositeChild cc = new CompositeChild();
    cc.children.add(new NonEmptyChild());
    container.children.addAll(Arrays.<Child>asList(new NonEmptyChild(), cc));
    selectFirst(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);

    press(KeyStrokeSpecs.SELECT_BEFORE);

    assertSelected(get(0));
  }

  @Test
  public void copyPaste() {
    container.children.add(new EmptyChild());
    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.COPY);
    press(KeyStrokeSpecs.PASTE);

    assertEquals(2, container.children.size());
  }

  @Test
  public void copyPasteMultiple() {
    container.children.add(new EmptyChild());
    container.children.add(new EmptyChild());
    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.COPY);
    press(KeyStrokeSpecs.PASTE);

    assertEquals(4, container.children.size());
  }

  @Test
  public void copyWithItemToTextSupport() {
    rootMapper.mySynchronizer.supportContentToString(itemToString);
    container.children.add(new EmptyChild());
    container.children.add(new NonEmptyChild());
    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);

    ClipboardContent content = copy();
    assertEquals(itemToString.apply(new EmptyChild()), content.toString());
  }

  @Test
  public void copyWithListToTextSupport() {
    rootMapper.mySynchronizer.supportContentListToString(listToString);
    container.children.add(new EmptyChild());
    container.children.add(new NonEmptyChild());

    selectChild(0);
    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.SELECT_AFTER);

    ClipboardContent content = copy();
    assertEquals(getMultiline(new EmptyChild(), new NonEmptyChild()), content.toString());
  }

  @Test
  public void pasteItemAsText() {
    rootMapper.mySynchronizer.supportListContentKind(ContentKinds.MULTILINE_TEXT, multilineParser);

    paste(getMultiline(new EmptyChild()));
    assertEquals(1, container.children.size());
    assertTrue(container.children.get(0) instanceof EmptyChild);
  }

  @Test
  public void pasteListAsText() {
    rootMapper.mySynchronizer.supportListContentKind(ContentKinds.MULTILINE_TEXT, multilineParser);

    paste(getMultiline(new EmptyChild(), new NonEmptyChild()));
    assertEquals(2, container.children.size());
    assertTrue(container.children.get(0) instanceof EmptyChild);
    assertTrue(container.children.get(1) instanceof NonEmptyChild);
  }

  @Test
  public void pasteIncorrectItem() {
    rootMapper.mySynchronizer.supportListContentKind(ContentKinds.MULTILINE_TEXT, multilineParser);

    paste("BadItem\n");
    assertTrue(container.children.isEmpty());
  }

  @Test
  public void pasteIncorrectItems() {
    rootMapper.mySynchronizer.supportListContentKind(ContentKinds.MULTILINE_TEXT, multilineParser);

    paste("BadItem\nEmptyChild\n");
    assertTrue(container.children.isEmpty());
  }

  @Test
  public void onlyTextualCopy() {
    rootMapper.mySynchronizer.setClipboardParameters(null, null);
    rootMapper.mySynchronizer.supportContentListToString(listToString);
    container.children.add(new EmptyChild());
    selectChild(0);

    assertNull(cut());

    ClipboardContent copied = copy();
    assertEquals(getMultiline(new EmptyChild()), TextContentHelper.getText(copied));

    paste(copied.toString());
    assertEquals(1, container.children.size());
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
  public void cutOneChild() {
    container.children.add(new EmptyChild());
    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(KeyStrokeSpecs.CUT);

    assertTrue(container.children.isEmpty());
  }

  @Test
  public void deleteOnEmpty() {
    container.children.addAll(Arrays.asList(new DeleteOnEmptyChild(), new NonEmptyChild()));
    selectChild(0);

    rootMapper.getTarget().container.children().get(0).dispatch(new Event(), Cells.BECAME_EMPTY);

    assertEquals(1, container.children.size());
    assertTrue(container.children.get(0) instanceof NonEmptyChild);
  }

  @Test
  public void deleteDeleteEatingChild() {
    container.children.add(new DeleteEatingChild());
    selectChild(0);

    press(KeyStrokeSpecs.SELECT_AFTER);
    press(Key.DELETE);

    assertTrue(container.children.isEmpty());
  }

  @Test
  public void emptinessOfPlaceholder() {
    EmptyCompositeChild emptyCompositeChild = new EmptyCompositeChild();
    container.children.add(emptyCompositeChild);
    assertTrue(Cells.isEmpty(getChild(0)));
  }

  @Test
  public void roleSetNullForPlaceholder() {
    container.children.add(new EmptyCompositeChild());

    CellActions.toFirstFocusable(getChild(0)).run();
    type("clea");
    complete();

    assertTrue(Cells.isEmpty(getChild(0)));
  }

  @Test
  public void addEmptyAfter() {
    EmptyCompositeChild cc = new EmptyCompositeChild();
    container.children.add(cc);
    selectFirst(0);

    enter();
    enter();

    assertEquals(0, cc.children.size());
    assertEquals(2, container.children.size());
  }

  @Test
  public void canCreateItemCheck() {
    RecordCompositeChild recordCompositeChild = new RecordCompositeChild();
    container.children.add(recordCompositeChild);
    CompositeRecord compositeRecord = new CompositeRecord();
    recordCompositeChild.records.add(compositeRecord);
    selectFirst(0);

    enter();
    enter();

    assertEquals(0, compositeRecord.records.size());
    assertEquals(1, recordCompositeChild.records.size());
    assertEquals(2, container.children.size());
  }

  @Test
  public void placeholderDisabled() {
    container.children.add(new EmptyChild());
    rootMapper.mySynchronizer.setOnLastItemDeleted(Runnables.EMPTY);
    rootMapper.mySynchronizer.disablePlaceholder();
    container.children.clear();
    assertTrue(rootMapper.getTarget().container.children().isEmpty());
    assertNull(myCellContainer.focusedCell.get());
  }

  @Test(expected = IllegalStateException.class)
  public void placeholderDisabledIncorrectly() {
    rootMapper.mySynchronizer.disablePlaceholder();
  }

  @Test(expected = IllegalArgumentException.class)
  public void onLastItemDeletedActionRestoredIncorrectly() {
    rootMapper.mySynchronizer.setOnLastItemDeleted(Runnables.EMPTY);
    rootMapper.mySynchronizer.disablePlaceholder();
    rootMapper.mySynchronizer.setOnLastItemDeleted(null);
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
    Cell target = Composites.firstFocusable(getChild(index));
    target.get(PositionHandler.PROPERTY).home();
    target.focus();
  }


  private void selectLast(int index) {
    Cell target = Composites.lastFocusable(getChild(index));
    target.get(PositionHandler.PROPERTY).end();
    target.focus();
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

        if (source instanceof EmptyCompositeChild) {
          return new EmptyCompositeChildMapper((EmptyCompositeChild) source);
        }

        if (source instanceof RecordCompositeChild) {
          return new RecordCompositeChildMapper((RecordCompositeChild) source);
        }

        if (source instanceof DeleteOnEmptyChild) {
          return new DeleteOnEmptyChildMapper((DeleteOnEmptyChild) source);
        }

        if (source instanceof DecoratedChild) {
          return new DecoratedChildMapper((DecoratedChild) source);
        }

        if (source instanceof DeleteEatingChild) {
          return new DeleteEatingChildMapper((DeleteEatingChild) source);
        }

        if (source instanceof IndentChild) {
          return new IndentChildMapper((IndentChild) source);
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
    result.setSeparator(',');

    result.setItemFactory(new Supplier<Child>() {
      @Override
      public Child get() {
        return new EmptyChild();
      }
    });
    result.setCompletion(new RoleCompletion<Object, Child>() {
      @Override
      public CompletionSupplier createRoleCompletion(Mapper<?, ?> mapper, Object contextNode, final Role<Child> target) {

        return new CompletionSupplier() {
          @Override
          public List<CompletionItem> get(CompletionParameters cp) {
            List<CompletionItem> result = new ArrayList<>();
            result.add(new SimpleCompletionItem("item") {
              @Override
              public Runnable complete(String text) {
                return target.set(new NonEmptyChild());
              }
            });
            result.add(new SimpleCompletionItem("clear") {
              @Override
              public Runnable complete(String text) {
                target.set(null);
                return Runnables.EMPTY;
              }
            });
            return result;
          }

          @Override
          public Async<Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
            return Asyncs.<Iterable<CompletionItem>>constant(Arrays.<CompletionItem>asList(new SimpleCompletionItem("asyncItem") {
              @Override
              public Runnable complete(String text) {
                return target.set(new NonEmptyChild());
              }
            }));
          }
        };
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

  private MapperFactory<Record, Cell> createRecordMapperFactory() {
    return new MapperFactory<Record, Cell>() {
      @Override
      public Mapper<? extends Record, ? extends Cell> createMapper(Record source) {
        if (source instanceof EmptyRecord) {
          return new EmptyRecordMapper((EmptyRecord) source);
        }
        if (source instanceof CompositeRecord) {
          return new CompositeRecordMapper((CompositeRecord) source);
        }
        return null;
      }
    };
  }

  private ProjectionalRoleSynchronizer<Object, Record> createRecordSynchronizer(
      Mapper<?, ? extends Cell> contextMapper, Cell target, ObservableList<Record> list, boolean hasItemFactory) {
    ProjectionalRoleSynchronizer<Object, Record> result =
        ProjectionalSynchronizers.forRole(contextMapper, list, target, createRecordMapperFactory());

    if (hasItemFactory) {
      result.setItemFactory(new Supplier<Record>() {
        @Override
        public Record get() {
          return new EmptyRecord();
        }
      });
    }

    return result;
  }

  private void enableItemHandler() {
    rootMapper.getTarget().set(ProjectionalObservableListSynchronizer.ITEM_HANDLER, new ProjectionalObservableListSynchronizer.ItemHandler() {
      @Override
      public Runnable addEmptyAfter() {
        itemHandlerWasCalled.set(true);
        return Runnables.EMPTY;
      }
    });
  }

  private String getMultiline(Child ... children) {
    return listToString.apply(Arrays.asList(children));
  }

  private class Container {
    final Property<Boolean> parensVisible = new ValueProperty<>(false);
    final ObservableList<Child> children = new ObservableArrayList<>();
  }

  private abstract class Record {
  }

  private class EmptyRecord extends Record {
  }

  private class CompositeRecord extends Record {
    final ObservableList<Record> records = new ObservableArrayList<>();
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

  private class EmptyCompositeChild extends Child {
    final ObservableList<Child> children = new ObservableArrayList<>();
  }

  private class RecordCompositeChild extends Child {
    final ObservableList<Record> records = new ObservableArrayList<>();
  }

  private class NonSelectableChild extends Child {
  }

  private class ComplexNonSelectableChild extends Child {
  }

  private class DeleteOnEmptyChild extends Child {
  }

  private class DecoratedChild extends Child {
    final Property<Boolean> before = new ValueProperty<>(false);
    final Property<Boolean> after = new ValueProperty<>(false);
  }

  private class DeleteEatingChild extends Child {
  }

  private class IndentChild extends Child {
  }

  private class ContainerMapper extends Mapper<Container, ContainerCell> {
    private ProjectionalRoleSynchronizer<Object, Child> mySynchronizer;

    ContainerMapper(Container source) {
      super(source, new ContainerCell());

    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      conf.add(mySynchronizer = createSynchronizer(this, getTarget().container, getSource().children));

      conf.add(Synchronizers.forPropsOneWay(getSource().parensVisible, Properties.compose(getTarget().lp.visible(), getTarget().rp.visible())));
    }
  }

  private class ContainerCell extends VerticalCell {
    final TextCell lp = CellFactory.text("(");
    final TextCell rp = CellFactory.text(")");
    final VerticalCell container = CellFactory.vertical();

    ContainerCell() {
      CellFactory.to(this, lp, container, rp);
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
      getTarget().children().add(new IndentCell());
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

  private class EmptyCompositeChildMapper extends Mapper<EmptyCompositeChild, HorizontalCell> {
    private EmptyCompositeChildMapper(EmptyCompositeChild source) {
      super(source, new HorizontalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      conf.add(createSynchronizer(this, getTarget(), getSource().children));
    }
  }

  private class DeleteOnEmptyChildCell extends HorizontalCell {
    private DeleteOnEmptyChildCell() {
      focusable().set(true);

      addTrait(new CellTrait() {
        @Override
        public Object get(final Cell cell, CellTraitPropertySpec<?> spec) {
          if (spec == ProjectionalSynchronizers.DELETE_ON_EMPTY) return true;
          return super.get(cell, spec);
        }
      });
    }
  }

  private class DeleteOnEmptyChildMapper extends Mapper<DeleteOnEmptyChild, DeleteOnEmptyChildCell> {
    private DeleteOnEmptyChildMapper(DeleteOnEmptyChild source) {
      super(source, new DeleteOnEmptyChildCell());
    }
  }

  private class DecoratedChildCell extends VerticalCell {
    final TextCell before = new TextCell("");
    final TextCell target = new TextCell("");
    final TextCell after = new TextCell("");

    public DecoratedChildCell() {
      CellFactory.to(this, before, target, after);

      target.text().set("   ");
      before.text().set("before");
      after.text().set("after");

      target.focusable().set(true);
      before.focusable().set(true);
      after.focusable().set(true);
    }
  }

  private class DecoratedChildMapper extends Mapper<DecoratedChild, DecoratedChildCell> {
    public DecoratedChildMapper(DecoratedChild source) {
      super(source, new DecoratedChildCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);

      conf.add(Synchronizers.forPropsOneWay(getSource().before, getTarget().before.visible()));
      conf.add(Synchronizers.forPropsOneWay(getSource().after, getTarget().after.visible()));
    }
  }

  private class DeleteEatingChildMapper extends Mapper<DeleteEatingChild, TextCell> {
    public DeleteEatingChildMapper(DeleteEatingChild source) {
      super(source, CellFactory.label("aaaa"));

      getTarget().focusable().set(true);

      getTarget().addTrait(new CellTrait() {
        @Override
        public void onKeyPressed(Cell cell, KeyEvent event) {
          if (event.is(Key.DELETE)) {
            event.consume();
            return;
          }

          super.onKeyPressed(cell, event);
        }
      });
    }
  }

  private class IndentChildMapper extends Mapper<IndentChild, IndentCell> {
    public IndentChildMapper(IndentChild source) {
      super(source, CellFactory.indent());
      Cell l = CellFactory.label(">>");
      getTarget().children().add(l);

      l.focusable().set(true);
    }
  }

  private class RecordCompositeChildMapper extends Mapper<RecordCompositeChild, VerticalCell> {
    private RecordCompositeChildMapper(RecordCompositeChild source) {
      super(source, new VerticalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      conf.add(createRecordSynchronizer(this, getTarget(), getSource().records, false));
    }
  }

  private class EmptyRecordMapper extends Mapper<EmptyRecord, TextCell> {
    EmptyRecordMapper(EmptyRecord source) {
      super(source, new TextCell());
      getTarget().text().set("");
      getTarget().addTrait(TextEditing.validTextEditing(Validators.equalsTo("")));
    }
  }

  private class CompositeRecordMapper extends Mapper<CompositeRecord, VerticalCell> {
    CompositeRecordMapper(CompositeRecord source) {
      super(source, new VerticalCell());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);
      conf.add(createRecordSynchronizer(this, getTarget(), getSource().records, true));
    }
  }
}