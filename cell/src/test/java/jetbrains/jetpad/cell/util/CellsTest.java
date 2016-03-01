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
package jetbrains.jetpad.cell.util;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.VerticalCell;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.*;

public class CellsTest extends BaseTestCase {
  HorizontalCell container = new HorizontalCell();
  Cell c1 = new TextCell();
  Cell c2 = new TextCell();
  Cell c3 = new TextCell();

  @Test
  public void emptinessOfEmptyText() {
    TextCell cell = new TextCell();
    cell.text().set("");

    assertTrue(Cells.isEmpty(cell));
  }

  @Test
  public void emptinessOfNullText() {
    TextCell cell = new TextCell();
    cell.text().set(null);

    assertTrue(Cells.isEmpty(cell));
  }

  @Test
  public void nonEmptinessOfNonEmptyText() {
    TextCell cell = new TextCell();
    cell.text().set("aaa");

    assertFalse(Cells.isEmpty(cell));
  }

  @Test
  public void nonEmptinessOfComposite() {
    assertTrue(Cells.isEmpty(new HorizontalCell()));
  }

  @Test
  public void emptinessOfCellWithPositionHandler() {
    VerticalCell cell = new VerticalCell();
    cell.children().add(new TextCell());
    cell.addTrait(new CellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == PositionHandler.PROPERTY) {
          return new PositionHandler() {
            @Override
            public boolean isHome() {
              return true;
            }

            @Override
            public boolean isEnd() {
              return true;
            }

            @Override
            public boolean isEmpty() {
              return true;
            }

            @Override
            public void home() {
            }

            @Override
            public void end() {
            }

            @Override
            public Property<Integer> caretOffset() {
              return new ValueProperty<>(0);
            }
          };
        }

        return super.get(cell, spec);
      }
    });

    assertTrue(Cells.isEmpty(cell));
  }

  @Test
  public void emptinessOfVerticalCell() {
    VerticalCell cell = new VerticalCell();
    cell.children().add(new TextCell());
    assertTrue(Cells.isEmpty(cell));
  }

  @Test
  public void nextCellAfterAdd() {
    container.children().add(c1);
    container.children().add(c2);

    assertNull(c2.nextSibling());
    assertSame(c2, c1.nextSibling());
  }

  @Test
  public void nextCellAfterRemove() {
    container.children().add(c1);
    container.children().add(c2);
    container.children().add(c3);

    container.children().remove(c2);

    assertSame(c3, c1.nextSibling());
    assertNull(c2.nextSibling());
  }

  @Test
  public void prevCellAfterAdd() {
    container.children().add(c1);
    container.children().add(c2);

    assertNull(c1.prevSibling());
    assertSame(c1, c2.prevSibling());
  }

  @Test
  public void prevCellAfterRemove() {
    container.children().add(c1);
    container.children().add(c2);
    container.children().add(c3);

    container.children().remove(c2);

    assertNull(c2.prevSibling());
    assertSame(c1, c3.prevSibling());
  }
}