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
package jetbrains.jetpad.cell.message;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.trait.CellTrait;
import jetbrains.jetpad.values.Color;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MessageStyleTest extends MessageControllerTestCase {

  @Override
  protected Cell doCreateCell() {
    return new HorizontalCell();
  }

  @Test
  public void simple() {
    assertNull(cell.get(Cell.BACKGROUND));
    MessageController.setBroken(cell, "0");
    assertEquals(Color.LIGHT_PINK, cell.get(Cell.BACKGROUND));

    MessageController.setBroken(cell, null);
    assertNull(cell.get(Cell.BACKGROUND));
  }

  @Test
  public void decorationOverwritesTrait() {
    cell.addTrait(new CellTrait() {
      @Override
      protected void provideProperties(Cell cell, PropertyCollector collector) {
        collector.add(Cell.BACKGROUND, Color.CYAN);
        super.provideProperties(cell, collector);
      }
    });
    assertEquals(Color.CYAN, cell.get(Cell.BACKGROUND));

    MessageController.setBroken(cell, "0");
    assertEquals(Color.LIGHT_PINK, cell.get(Cell.BACKGROUND));

    MessageController.setBroken(cell, null);
    assertEquals(Color.CYAN, cell.get(Cell.BACKGROUND));
  }
}