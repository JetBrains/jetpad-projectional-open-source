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
package jetbrains.jetpad.cell.error;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellAdapter;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorMarkersTest extends BaseTestCase {
  private TextCell cell;

  @Before
  public void init() {
    cell = new TextCell("abc");
    ErrorMarkers.install(cell);
  }

  @Test
  public void setErrorPopup() {
    final Value<Boolean> errorPopupChanged = new Value<>(false);
    cell.addListener(new CellAdapter() {
      @Override
      public void onPropertyChanged(CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
        if (Cell.isPopupProp(prop) && cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE)) {
          errorPopupChanged.set(true);
        }
      }
    });
    cell.set(Cell.HAS_ERROR, true);
    assertTrue(cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE));
    assertNotNull(cell.get(ErrorMarkers.ERROR_POPUP_POSITION));
    assertTrue(errorPopupChanged.get());
  }

  @Test
  public void removeErrorPopup() {
    cell.set(Cell.HAS_ERROR, true);
    cell.set(Cell.HAS_ERROR, false);
    assertFalse(cell.get(ErrorMarkers.ERROR_POPUP_ACTIVE));
    assertNull(cell.get(ErrorMarkers.ERROR_POPUP_POSITION));
  }
}