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
import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class PopupsMappingTest extends MessageControllerTestCase {

  @Test
  public void setPopupAfterMapping() {
    TextCell popup = new TextCell("popup");
    cell.bottomPopup().set(popup);
    assertNotNull(getContainerMapper().getDescendantMapper(popup));
  }

  @Test
  public void attachCellWithPopup() {
    Cell child = new HorizontalCell();
    TextCell popup = new TextCell("popup");
    child.bottomPopup().set(popup);

    cell.children().add(child);
    assertNotNull(getContainerMapper().getDescendantMapper(popup));
  }
}