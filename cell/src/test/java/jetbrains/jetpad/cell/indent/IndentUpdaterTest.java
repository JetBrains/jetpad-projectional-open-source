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
package jetbrains.jetpad.cell.indent;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.toView.CellContainerToViewMapper;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static jetbrains.jetpad.cell.util.CellFactory.*;
import static org.junit.Assert.assertEquals;

public class IndentUpdaterTest {
  private ViewContainer viewContainer = new ViewContainer();
  private CellContainer cellContainer = new CellContainer();
  private IndentCell indentCell = new IndentCell();
  private View indentView;
  private ObservableList<Cell> children = indentCell.children();
  private CellContainerToViewMapper rootMapper;

  @Before
  public void init() {
    rootMapper = new CellContainerToViewMapper(cellContainer, viewContainer.root(), viewContainer.contentRoot(), viewContainer.decorationRoot());
    rootMapper.attachRoot();

    init(cellContainer.root);
  }

  private void init(Cell target) {
    Composites.<Cell>removeFromParent(indentCell);
    target.children().add(indentCell);
    indentView = (View) rootMapper.getDescendantMapper(indentCell).getTarget();
  }

  @Test
  public void singleLineAdd() {

    children.addAll(Arrays.asList(text("a"), text("b")));

    assertTarget("[['a', 'b']]");
  }

  @Test
  public void collectionAdd() {
    children.addAll(Arrays.asList(indent(text("a"), text("b")), indent(text("c"))));

    assertTarget("[['a', 'b', 'c']]");
  }

  @Test
  public void newLineAdd() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b")));

    assertTarget("[['a'], ['b']]");
  }

  @Test
  public void collectionWithNewLinesAdd() {
    children.add(indent(text("a"), newLine(), text("b"), newLine(), text("c")));

    assertTarget("[['a'], ['b'], ['c']]");
  }

  @Test
  public void insertInTheMiddleFirstLine() {
    children.addAll(Arrays.asList(text("a"), text("b")));

    children.add(1, text("z"));

    assertTarget("[['a', 'z', 'b']]");
  }

  @Test
  public void insertInTheMiddleNonFirstLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b"), text("c")));

    children.add(3, text("z"));

    assertTarget("[['a'], ['b', 'z', 'c']]");
  }

  @Test
  public void splitLastLine() {
    children.addAll(Arrays.asList(text("a"), text("b")));

    children.add(1, newLine());

    assertTarget("[['a'], ['b']]");
  }

  @Test
  public void splitNonLastLine() {
    children.addAll(Arrays.asList(text("a"), text("b"), newLine(), text("c")));

    children.add(1, newLine());
    assertTarget("[['a'], ['b'], ['c']]");
  }

  @Test
  public void deleteAtFirstPosition() {
    children.addAll(Arrays.asList(text("a"), text("b")));
    children.remove(0);

    assertTarget("[['b']]");
  }

  @Test
  public void deleteInFirstLine() {
    children.addAll(Arrays.asList(text("a"), text("b")));
    children.remove(1);

    assertTarget("[['a']]");
  }

  @Test
  public void deleteInNonFirstLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b"), text("c")));
    children.remove(2);

    assertTarget("[['a'], ['c']]");
  }

  @Test
  public void deleteFirstNewLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b")));
    children.remove(1);

    assertTarget("[['a', 'b']]");
  }

  @Test
  public void deleteNonFirstNewLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b"), newLine(), text("c")));
    children.remove(3);

    assertTarget("[['a'], ['b', 'c']]");
  }

  @Test
  public void deleteNewLineBeforeIndent() {
    IndentCell list = indent(true, newLine(), text("b"), newLine(), text("c"));
    children.addAll(Arrays.asList(text("a"), list));

    list.children().remove(2);

    assertTarget("[['a'], ['  ', 'b', 'c']]");
  }

  @Test
  public void deleteIndentedLeaf() {
    IndentCell list = indent(true, newLine(), text("b"), text("c"));
    children.addAll(Arrays.asList(text("a"), list));

    list.children().remove(1);

    assertTarget("[['a'], ['  ', 'c']]");
  }

  @Test
  public void collectionRemove() {
    children.addAll(Arrays.asList(text("a"), indent(newLine(), text("b"), newLine()), text("c")));
    children.remove(1);

    assertTarget("[['a', 'c']]");
  }

  @Test
  public void indentedSequence() {
    children.addAll(Arrays.asList(text("a"), indent(true, text("b"), newLine(), text("c"))));

    assertTarget("[['a', 'b'], ['  ', 'c']]");
  }

  @Test
  public void nestedIndentSequence() {
    children.addAll(Arrays.asList(text("a"), indent(true, newLine(), text("b"), indent(true, newLine(), text("c")))));

    assertTarget("[['a'], ['  ', 'b'], ['    ', 'c']]");
  }

  @Test
  public void cellChildrenExceptionFirstPosition() {
    children.addAll(Arrays.asList(composite("b")));

    assertTarget("[[['composite', 'b']]]");
  }

  @Test
  public void compositesOneAfterOther() {
    children.addAll(Arrays.asList(composite("a"), composite("b")));

    assertTarget("[[['composite', 'a'], ['composite', 'b']]]");
  }

  @Test
  public void exceptionOnDeeplyNestedCompositesInsideOfCells() {
    Cell c = composite("z");
    c.children().add(0, composite("zz"));

    children.addAll(Arrays.asList(newLine(), c));

    assertTarget("[[], [[['composite', 'zz'], 'composite', 'z']]]");
  }

  @Test
  public void visibilityChange() {
    Cell ta = text("a");
    children.addAll(Arrays.asList(ta, text("b")));
    ta.visible().set(false);

    assertTarget("[['b']]");
  }

  @Test
  public void newLineVisibilityChange() {
    NewLineCell nl = newLine();
    nl.visible().set(false);
    children.addAll(Arrays.asList(text("a"), nl, text("b")));
    assertTarget("[['a', 'b']]");

    nl.visible().set(true);
    assertTarget("[['a'], ['b']]");
  }

  @Test
  public void newLineVisibilityChangeInCaseOfEmptyContainer() {
    NewLineCell nl = newLine();
    nl.visible().set(false);
    children.addAll(Arrays.asList(text("a"), nl, indent()));
    nl.visible().set(true);

  }

  @Test
  public void nestedVisibility() {
    IndentCell l = indent(text("b"), text("c"));
    l.visible().set(false);
    children.addAll(Arrays.asList(text("1"), newLine(), text("a"), l, text("d")));

    assertTarget("[['1'], ['a', 'd']]");
  }

  @Test
  public void changeVisibilityOfCollectionWithNewLine() {
    IndentCell l = indent(newLine(), text("z"));
    l.visible().set(false);

    children.add(l);

    l.visible().set(true);

    assertTarget("[[], ['z']]");
  }


  @Test
  public void mixingIndentWithOtherContainers() {
    cellContainer.root.children().clear();

    HorizontalCell target = new HorizontalCell();
    cellContainer.root.children().add(horizontal(label("aaa"), target, label("bbb")));

    indentCell.children().addAll(Arrays.asList(text("b"), text("c")));

    init(target);

    assertTarget("[['b', 'c']]");
  }

  @Test
  public void toggleLeafVisibility() {
    Cell t = text("a");
    children.addAll(Arrays.asList(t));

    t.visible().set(false);
    t.visible().set(true);

    assertTarget("[['a']]");
  }

  @Test
  public void rootVisibilityChangeDoesntLeadToException() {
    IndentCell root = new IndentCell();
    root.children().add(text("a"));
    root.visible().set(false);

    cellContainer.root.children().add(root);

    root.visible().set(true);
  }

  private Cell composite(String text) {
    Cell result = new HorizontalCell();
    result.children().addAll(Arrays.asList(text("composite"), text(text)));
    return result;
  }

  private Cell text(String text) {
    return new TextCell(text);
  }


  private void assertTarget(String presentation) {
    assertEquals(presentation, toString(indentView));
  }

  static String toString(View view) {
    StringBuilder builder = new StringBuilder();
    toString(view, builder);
    return builder.toString();
  }

  private static void toString(View view, StringBuilder result) {
    if (view instanceof TextView) {
      result.append("'").append(((TextView) view).text().get()).append("'");
    } else {
      result.append("[");
      boolean first = true;
      for (View child : view.children()) {
        if (first) {
          first = false;
        } else {
          result.append(", ");
        }
        toString(child, result);
      }
      result.append("]");
    }
  }
}