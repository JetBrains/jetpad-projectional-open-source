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
package jetbrains.jetpad.cell;

import com.google.common.base.Supplier;
import jetbrains.jetpad.cell.trait.CellTrait;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TraitPropagatorTest extends EditingTestCase {
  private static final CellPropertySpec<Boolean> TEST_CONTROLLER_INSTALLED = new CellPropertySpec<>("test", false);
  private Cell cell;
  private Supplier<Integer> size;

  @Before
  public void init() {
    installController(myCellContainer);
    cell = new HorizontalCell();
    myCellContainer.root.children().add(cell);
  }

  private void installController(CellContainer container) {
    size = TraitPropagator.installWithSizeSupplier(container, new CellTrait() {},
        TEST_CONTROLLER_INSTALLED, TraitPropagator.NOT_POPUP,
        TraitPropagator.EMPTY_CELL_HANDLER).second;
  }

  @Test
  public void notEmptyContainer() {
    CellContainer container = new CellContainer();
    container.root.children().add(new HorizontalCell());
    installController(container);
    assertEquals(2, (int) size.get());
  }

  @Test(expected = IllegalArgumentException.class)
  public void doubleInstall() {
    installController(myCellContainer);
  }

  @Test
  public void detachChild() {
    HorizontalCell parent = new HorizontalCell();
    HorizontalCell child = new HorizontalCell();
    parent.children().add(child);
    myCellContainer.root.children().add(parent);
    assertEquals(4, (int) size.get());

    parent.children().remove(0);
    assertEquals(3, (int) size.get());
  }

  @Test
  public void noTraitForPopup() {
    assertEquals(2, (int) size.get());

    HorizontalCell popup = new HorizontalCell();
    cell.bottomPopup().set(popup);
    assertEquals(2, (int) size.get());

    cell.bottomPopup().set(null);
    assertEquals(2, (int) size.get());
  }
}