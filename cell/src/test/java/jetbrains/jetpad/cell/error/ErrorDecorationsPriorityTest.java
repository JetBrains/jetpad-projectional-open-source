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

import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorDecorationsPriorityTest extends ErrorControllerTestCase {

  private void noOverwriteByAdd(CellPropertySpec<String> toKeep, CellPropertySpec<String> toAdd) {
    ErrorController.set(cell, "0", toKeep);
    mouseEntered();
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());

    assertNull(cell.get(toAdd));
    ErrorController.set(cell, "1", toAdd);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void warnAddCantOverwriteBroken() {
    noOverwriteByAdd(ErrorController.BROKEN, ErrorController.WARNING);
  }

  @Test
  public void warnAddCantOverwriteError() {
    noOverwriteByAdd(ErrorController.ERROR, ErrorController.WARNING);
  }

  @Test
  public void errorAddCantOverwriteBroken() {
    noOverwriteByAdd(ErrorController.BROKEN, ErrorController.ERROR);
  }

  private void noOverwriteByUpdate(CellPropertySpec<String> toKeep, CellPropertySpec<String> toUpdate) {
    set(toKeep, toUpdate);

    assertNotNull(cell.get(toUpdate));
    ErrorController.set(cell, "2", toUpdate);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  private void set(CellPropertySpec<String> higherPriority, CellPropertySpec<String> lowerPriority) {
    ErrorController.set(cell, "0", higherPriority);
    ErrorController.set(cell, "1", lowerPriority);
    mouseEntered();
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void warnUpdateCantOverwriteBroken() {
    noOverwriteByUpdate(ErrorController.BROKEN, ErrorController.WARNING);
  }

  @Test
  public void warnUpdateCantOverwriteError() {
    noOverwriteByUpdate(ErrorController.ERROR, ErrorController.WARNING);
  }

  @Test
  public void errorUpdateCantOverwriteBroken() {
    noOverwriteByUpdate(ErrorController.BROKEN, ErrorController.ERROR);
  }

  private void noOverwriteByRemove(CellPropertySpec<String> toKeep, CellPropertySpec<String> toRemove) {
    set(toKeep, toRemove);

    assertNotNull(cell.get(toRemove));
    ErrorController.set(cell, null, toRemove);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void warnRemoveCantOverwriteBroken() {
    noOverwriteByRemove(ErrorController.BROKEN, ErrorController.WARNING);
  }

  @Test
  public void warnRemoveCantOverwriteError() {
    noOverwriteByRemove(ErrorController.ERROR, ErrorController.WARNING);
  }

  @Test
  public void errorRemoveCantOverwriteBroken() {
    noOverwriteByRemove(ErrorController.BROKEN, ErrorController.ERROR);
  }

  private void lowerPriorityShownAfterHigherRemoved(CellPropertySpec<String> toRemove, CellPropertySpec<String> toActivate) {
    set(toRemove, toActivate);

    ErrorController.set(cell, null, toRemove);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" " + cell.get(toActivate) + " ", getPopupMessage());
  }

  @Test
  public void errorShownAfterBrokenRemoved() {
    lowerPriorityShownAfterHigherRemoved(ErrorController.BROKEN, ErrorController.ERROR);
  }

  @Test
  public void errorShownAfterBrokenRemovedWarnSet() {
    ErrorController.set(cell, "2", ErrorController.WARNING);
    lowerPriorityShownAfterHigherRemoved(ErrorController.BROKEN, ErrorController.ERROR);
  }

  @Test
  public void warnShownAfterBrokenRemoved() {
    lowerPriorityShownAfterHigherRemoved(ErrorController.BROKEN, ErrorController.WARNING);
  }

  @Test
  public void inactiveMessageUpdate() {
    set(ErrorController.BROKEN, ErrorController.ERROR);
    ErrorController.set(cell, "3", ErrorController.ERROR);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void activeMessageUpdate() {
    set(ErrorController.BROKEN, ErrorController.ERROR);
    ErrorController.set(cell, "3", ErrorController.BROKEN);
    assertEquals(" 3 ", getPopupMessage());
  }

  @Test
  public void hiddenMessageUpdate() {
    ErrorController.set(cell, "0", ErrorController.BROKEN);
    cell.set(ErrorDecorationTrait.POPUP_POSITION, new TextCell());
    mouseEntered();
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));

    ErrorController.set(cell, "1", ErrorController.BROKEN);
    assertFalse(cell.get(ErrorDecorationTrait.POPUP_ACTIVE));

    cell.set(ErrorDecorationTrait.POPUP_POSITION, null);
    assertDecorationPopupVisible(cell, false);
    mouseMoved();

    assertDecorationPopupVisible(cell, true);
    assertEquals(" 1 ", getPopupMessage());
  }
}
