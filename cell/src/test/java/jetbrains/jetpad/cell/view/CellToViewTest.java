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
package jetbrains.jetpad.cell.view;

import jetbrains.jetpad.cell.trait.BaseCellTrait;
import jetbrains.jetpad.event.MouseEvent;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.projectional.view.ViewContainer;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class CellToViewTest {
  private ViewContainer targetViewContainer = new ViewContainer();
  private CellView cellView = new CellView();

  @Before
  public void init() {
    targetViewContainer.contentRoot().children().add(cellView);
  }

  @Test
  public void cellContainerRemovalInHandlers() {
    TextCell testCell = new TextCell();
    testCell.addTrait(new BaseCellTrait() {
      @Override
      public void onMousePressed(Cell cell, MouseEvent event) {
        targetViewContainer.contentRoot().children().clear();
        event.consume();
      }
    });
    cellView.cell.set(testCell);

    targetViewContainer.root().validate();
    targetViewContainer.mousePressed(new MouseEvent(testCell.getBounds().center()));
    
    assertFalse(cellView.isAttached());
  }



}