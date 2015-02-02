/*
 * Copyright 2012-2015 JetBrains s.r.o
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

import jetbrains.jetpad.cell.toView.CellToView;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CellContainerTest {
  CellContainer container = new CellContainer();
  ViewContainer viewContainer = new ViewContainer();
  TestCell cell1 = new TestCell();
  TestCell cell2 = new TestCell();

  @Before
  public void init() {
    CellToView.map(container, viewContainer);

    container.root.children().addAll(Arrays.asList(cell1, cell2));
  }

  @Test
  public void mouseEnter() {
    container.mouseEntered(new MouseEvent(cell1.getBounds().center()));

    assertTrue(cell1.myMouseIn);
    assertFalse(cell2.myMouseIn);
  }

  @Test
  public void mouseMove() {
    container.mouseEntered(new MouseEvent(cell1.getBounds().center()));
    container.mouseMoved(new MouseEvent(cell1.getBounds().center().add(new Vector(1, 1))));
    container.mouseMoved(new MouseEvent(cell2.getBounds().center()));

    assertFalse(cell1.myMouseIn);
    assertTrue(cell2.myMouseIn);
  }

  @Test
  public void mouseLeft() {
    container.mouseEntered(new MouseEvent(cell1.getBounds().center()));
    container.mouseLeft(new MouseEvent(new Vector(0, 0)));

    assertFalse(cell1.myMouseIn);
    assertFalse(cell2.myMouseIn);
  }

  private class TestCell extends TextCell {
    private boolean myMouseIn;

    {
      text().set("Test");

      addTrait(new CellTrait() {
        @Override
        public void onMouseEntered(Cell cell, MouseEvent event) {
          super.onMouseEntered(cell, event);
          myMouseIn = true;
        }

        @Override
        public void onMouseLeft(Cell cell, MouseEvent event) {
          super.onMouseLeft(cell, event);
          myMouseIn = false;
        }
      });
    }
  }



}