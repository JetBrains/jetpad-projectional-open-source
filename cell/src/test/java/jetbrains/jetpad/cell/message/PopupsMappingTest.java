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
package jetbrains.jetpad.cell.message;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.toView.CellContainerToViewMapper;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PopupsMappingTest extends BaseTestCase {
  private CellContainer cellContainer;

  @Before
  public void init() {
    cellContainer = new CellContainer();
    MessageController.install(cellContainer);
  }

  @Test
  public void setPopupAfterMapping() {
    setPopupAfterMapping(new HorizontalCell());
  }

  @Test
  public void setPopupAfterIndentMapping() {
    setPopupAfterMapping(new IndentCell());
  }

  private void setPopupAfterMapping(Cell container) {
    HorizontalCell cell = new HorizontalCell();
    container.children().add(cell);
    cellContainer.root.children().add(container);
    CellContainerToViewMapper mapper = mapContainer();

    TextCell popup = new TextCell("popup");
    cell.bottomPopup().set(popup);
    assertNotNull(mapper.getDescendantMapper(popup));
  }

  @Test
  public void attachCellWithPopup() {
    attachCellWithPopup(new HorizontalCell());
  }

  @Test
  public void attachCellWithPopupToIndent() {
    attachCellWithPopup(new IndentCell());
  }

  private void attachCellWithPopup(Cell container) {
    cellContainer.root.children().add(container);
    CellContainerToViewMapper mapper = mapContainer();

    HorizontalCell cell = new HorizontalCell();
    TextCell popup = new TextCell("popup");
    cell.bottomPopup().set(popup);
    container.children().add(cell);
    assertNotNull(mapper.getDescendantMapper(popup));
  }

  private CellContainerToViewMapper mapContainer() {
    ViewContainer viewContainer = new ViewContainer();
    CellContainerToViewMapper mapper = new CellContainerToViewMapper(
        cellContainer, viewContainer.root(), viewContainer.contentRoot(), viewContainer.decorationRoot());
    mapper.attachRoot();
    return mapper;
  }
}