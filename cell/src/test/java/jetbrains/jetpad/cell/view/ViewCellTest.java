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
package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.toView.CellToView;
import jetbrains.jetpad.projectional.view.TextView;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class ViewCellTest {
  private ViewContainer viewContainer = new ViewContainer();
  private CellContainer cellContainer = new CellContainer();
  private ViewCell viewCell = new ViewCell();
  private TextView testView = new TextView();

  @Before
  public void init() {
    CellToView.map(cellContainer, viewContainer);
    viewCell.view.set(testView);
    cellContainer.root.children().add(viewCell);
  }

  @Test
  public void addHandling() {
    assertSame(viewContainer, testView.container());
  }

  @Test
  public void setViewToNull() {
    viewCell.view.set(null);

    assertNull(testView.container());
  }

}