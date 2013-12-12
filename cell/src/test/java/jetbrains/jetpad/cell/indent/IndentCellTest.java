/*
 * Copyright 2012-2013 JetBrains s.r.o
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

import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.cell.indent.test.IndentCell;
import jetbrains.jetpad.cell.indent.test.IndentPart;
import jetbrains.jetpad.cell.indent.test.NewLinePart;
import org.junit.Test;

import java.util.Arrays;

import static jetbrains.jetpad.cell.indent.CellUtil.*;
import static org.junit.Assert.assertEquals;

public class IndentCellTest {
  private IndentCell indentCell = new IndentCell();
  private ObservableList<IndentPart> children = indentCell.root().children;

  @Test
  public void singleLineAdd() {
    children.addAll(Arrays.asList(text("a"), text("b")));

    assertCell("[['a', 'b']]");
  }

  @Test
  public void collectionAdd() {
    children.addAll(Arrays.asList(list(text("a"), text("b")), list(text("c"))));

    assertCell("[['a', 'b', 'c']]");
  }

  @Test
  public void newLineAdd() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b")));

    assertCell("[['a'], ['b']]");
  }

  @Test
  public void collectionWithNewLinesAdd() {
    children.add(list(text("a"), newLine(), text("b"), newLine(), text("c")));

    assertCell("[['a'], ['b'], ['c']]");
  }

  @Test
  public void insertInTheMiddleFirstLine() {
    children.addAll(Arrays.asList(text("a"), text("b")));

    children.add(1, text("z"));

    assertCell("[['a', 'z', 'b']]");
  }

  @Test
  public void insertInTheMiddleNonFirstLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b"), text("c")));

    children.add(3, text("z"));

    assertCell("[['a'], ['b', 'z', 'c']]");
  }

  @Test
  public void splitLastLine() {
    children.addAll(Arrays.asList(text("a"), text("b")));

    children.add(1, newLine());

    assertCell("[['a'], ['b']]");
  }

  @Test
  public void splitNonLastLine() {
    children.addAll(Arrays.asList(text("a"), text("b"), newLine(), text("c")));

    children.add(1, newLine());
    assertCell("[['a'], ['b'], ['c']]");
  }

  @Test
  public void deleteAtFirstPosition() {
    children.addAll(Arrays.asList(text("a"), text("b")));
    children.remove(0);

    assertCell("[['b']]");
  }

  @Test
  public void deleteInFirstLine() {
    children.addAll(Arrays.asList(text("a"), text("b")));
    children.remove(1);

    assertCell("[['a']]");
  }

  @Test
  public void deleteInNonFirstLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b"), text("c")));
    children.remove(2);

    assertCell("[['a'], ['c']]");
  }

  @Test
  public void deleteFirstNewLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b")));
    children.remove(1);

    assertCell("[['a', 'b']]");
  }

  @Test
  public void deleteNonFirstNewLine() {
    children.addAll(Arrays.asList(text("a"), newLine(), text("b"), newLine(), text("c")));
    children.remove(3);

    assertCell("[['a'], ['b', 'c']]");
  }

  @Test
  public void deleteNewLineBeforeIndent() {
    IndentPart list = list(true, newLine(), text("b"), newLine(), text("c"));
    children.addAll(Arrays.asList(text("a"), list));

    list.children.remove(2);

    assertCell("[['a'], ['  ', 'b', 'c']]");
  }

  @Test
  public void deleteIndentedLeaf() {
    IndentPart list = list(true, newLine(), text("b"), text("c"));
    children.addAll(Arrays.asList(text("a"), list));

    list.children.remove(1);

    assertCell("[['a'], ['  ', 'c']]");
  }

  @Test
  public void collectionRemove() {
    children.addAll(Arrays.asList(text("a"), list(newLine(), text("b"), newLine()), text("c")));
    children.remove(1);

    assertCell("[['a', 'c']]");
  }

  @Test
  public void indentedSequence() {
    children.addAll(Arrays.asList(text("a"), list(true, text("b"), newLine(), text("c"))));

    assertCell("[['a', 'b'], ['  ', 'c']]");
  }

  @Test
  public void nestedIndentSequence() {
    children.addAll(Arrays.asList(text("a"), list(true, newLine(), text("b"), list(true, newLine(), text("c")))));

    assertCell("[['a'], ['  ', 'b'], ['    ', 'c']]");
  }

  @Test
  public void cellChildrenAreIngored() {
    children.addAll(Arrays.asList(text("a"), composite("b"), newLine(), text("c")));

    assertCell("[['a', 'b'], ['c']]");
  }

  @Test
  public void cellChildrenExceptionFirstPosition() {
    children.addAll(Arrays.asList(composite("b")));

    assertCell("[['b']]");
  }

  @Test
  public void compositesOneAfterOther() {
    children.addAll(Arrays.asList(composite("a"), composite("b")));

    assertCell("[['a', 'b']]");
  }

  @Test
  public void exceptionOnDeeplyNestedCompositesInsideOfCells() {
    IndentPart c = composite("z");
    c.children().add(0, composite("zz"));

    children.addAll(Arrays.asList(newLine(), c));

    assertCell("[[], ['z']]");
  }

  @Test
  public void visibilityChange() {
    IndentPart ta = text("a");
    children.addAll(Arrays.asList(ta, text("b")));
    ta.setVisible(false);

    assertCell("[['b']]");
  }

  @Test
  public void newLineVisibilityChange() {
    NewLinePart nl = newLine();
    nl.setVisible(false);
    children.addAll(Arrays.asList(text("a"), nl, text("b")));
    assertCell("[['a', 'b']]");

    nl.setVisible(true);
    assertCell("[['a'], ['b']]");
  }

  @Test
  public void nestedVisibility() {
    IndentPart l = list(text("b"), text("c"));
    l.setVisible(false);
    children.addAll(Arrays.asList(text("1"), newLine(), text("a"), l, text("d")));

    assertCell("[['1'], ['a', 'd']]");
  }

  private void assertCell(String presentation) {
    assertEquals(presentation, CellUtil.toString(indentCell));
  }
}