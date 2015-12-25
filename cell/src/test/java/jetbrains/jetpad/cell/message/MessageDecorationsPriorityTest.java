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

import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TextCell;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageDecorationsPriorityTest extends MessageControllerTestCase {

  private void noOverwriteByAdd(CellPropertySpec<String> toKeep, CellPropertySpec<String> toAdd) {
    MessageController.set(cell, "0", toKeep);
    mouseEntered(cell);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());

    assertNull(cell.get(toAdd));
    MessageController.set(cell, "1", toAdd);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void warnAddCantOverwriteBroken() {
    noOverwriteByAdd(MessageController.BROKEN, MessageController.WARNING);
  }

  @Test
  public void warnAddCantOverwriteError() {
    noOverwriteByAdd(MessageController.ERROR, MessageController.WARNING);
  }

  @Test
  public void errorAddCantOverwriteBroken() {
    noOverwriteByAdd(MessageController.BROKEN, MessageController.ERROR);
  }

  private void noOverwriteByUpdate(CellPropertySpec<String> toKeep, CellPropertySpec<String> toUpdate) {
    set(toKeep, toUpdate);

    assertNotNull(cell.get(toUpdate));
    MessageController.set(cell, "2", toUpdate);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  private void set(CellPropertySpec<String> higherPriority, CellPropertySpec<String> lowerPriority) {
    MessageController.set(cell, "0", higherPriority);
    MessageController.set(cell, "1", lowerPriority);
    mouseEntered(cell);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void warnUpdateCantOverwriteBroken() {
    noOverwriteByUpdate(MessageController.BROKEN, MessageController.WARNING);
  }

  @Test
  public void warnUpdateCantOverwriteError() {
    noOverwriteByUpdate(MessageController.ERROR, MessageController.WARNING);
  }

  @Test
  public void errorUpdateCantOverwriteBroken() {
    noOverwriteByUpdate(MessageController.BROKEN, MessageController.ERROR);
  }

  private void noOverwriteByRemove(CellPropertySpec<String> toKeep, CellPropertySpec<String> toRemove) {
    set(toKeep, toRemove);

    assertNotNull(cell.get(toRemove));
    MessageController.set(cell, null, toRemove);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void warnRemoveCantOverwriteBroken() {
    noOverwriteByRemove(MessageController.BROKEN, MessageController.WARNING);
  }

  @Test
  public void warnRemoveCantOverwriteError() {
    noOverwriteByRemove(MessageController.ERROR, MessageController.WARNING);
  }

  @Test
  public void errorRemoveCantOverwriteBroken() {
    noOverwriteByRemove(MessageController.BROKEN, MessageController.ERROR);
  }

  private void lowerPriorityShownAfterHigherRemoved(CellPropertySpec<String> toRemove, CellPropertySpec<String> toActivate) {
    set(toRemove, toActivate);

    MessageController.set(cell, null, toRemove);
    assertDecorationPopupVisible(cell, true);
    assertEquals(" " + cell.get(toActivate) + " ", getPopupMessage());
  }

  @Test
  public void errorShownAfterBrokenRemoved() {
    lowerPriorityShownAfterHigherRemoved(MessageController.BROKEN, MessageController.ERROR);
  }

  @Test
  public void errorShownAfterBrokenRemovedWarnSet() {
    MessageController.set(cell, "2", MessageController.WARNING);
    lowerPriorityShownAfterHigherRemoved(MessageController.BROKEN, MessageController.ERROR);
  }

  @Test
  public void warnShownAfterBrokenRemoved() {
    lowerPriorityShownAfterHigherRemoved(MessageController.BROKEN, MessageController.WARNING);
  }

  @Test
  public void inactiveMessageUpdate() {
    set(MessageController.BROKEN, MessageController.ERROR);
    MessageController.set(cell, "3", MessageController.ERROR);
    assertEquals(" 0 ", getPopupMessage());
  }

  @Test
  public void activeMessageUpdate() {
    set(MessageController.BROKEN, MessageController.ERROR);
    MessageController.set(cell, "3", MessageController.BROKEN);
    assertEquals(" 3 ", getPopupMessage());
  }

  @Test
  public void hiddenMessageUpdate() {
    MessageController.set(cell, "0", MessageController.BROKEN);
    cell.set(MessageTrait.POPUP_POSITION, new TextCell());
    mouseEntered(cell);
    assertFalse(cell.get(MessageTrait.POPUP_ACTIVE));

    MessageController.set(cell, "1", MessageController.BROKEN);
    assertFalse(cell.get(MessageTrait.POPUP_ACTIVE));

    cell.set(MessageTrait.POPUP_POSITION, null);
    assertDecorationPopupVisible(cell, false);
    mouseMoved();

    assertDecorationPopupVisible(cell, true);
    assertEquals(" 1 ", getPopupMessage());
  }
}