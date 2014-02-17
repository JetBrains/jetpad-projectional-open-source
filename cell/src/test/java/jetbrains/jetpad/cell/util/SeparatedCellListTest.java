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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SeparatedCellListTest {
  private List<Cell> targetList = new ArrayList<Cell>();
  private List<Cell> separatedList = new MySeparatedCellList(targetList);

  @Test
  public void emptyList() {
    assertList("");
  }

  @Test
  public void oneItem() {
    init("x");

    assertList("x");
  }

  @Test
  public void twoItems() {
    init("x", "y");

    assertList("x, x+y, y");
  }

  @Test
  public void insertInStart() {
    init("x", "y");

    separatedList.add(0, new TextCell("a"));

    assertList("a, a+x, x, x+y, y");
  }

  @Test
  public void insertInMiddle() {
    init("x", "y");

    separatedList.add(1, new TextCell("a"));

    assertList("x, x+a, a, a+y, y");
  }

  @Test
  public void removeFromStart() {
    init("x", "y");

    separatedList.remove(0);

    assertList("y");
  }

  @Test
  public void removeFromEnd() {
    init("x", "y");

    separatedList.remove(1);

    assertList("x");
  }

  @Test
  public void removeFromMiddle() {
    init("x", "y", "z");

    separatedList.remove(1);

    assertList("x, x+z, z");
  }

  private void init(String... items) {
    for (String i : items) {
      separatedList.add(new TextCell(i));
    }
  }


  private void assertList(String text) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < targetList.size(); i++) {
      if (i != 0) {
        result.append(", ");
      }
      result.append(((TextCell) targetList.get(i)).text().get());
    }

    assertEquals(text, result.toString());
  }


  private class MySeparatedCellList extends SeparatedCellList {
    private MySeparatedCellList(List<Cell> baseList) {
      super(baseList);
    }

    @Override
    protected TextCell createSeparator(Cell left, Cell right) {
      TextCell textLeft = (TextCell) left;
      TextCell textRight = (TextCell) right;
      return new TextCell(textLeft.text().get() + "+" + textRight.text().get());
    }
  }
}