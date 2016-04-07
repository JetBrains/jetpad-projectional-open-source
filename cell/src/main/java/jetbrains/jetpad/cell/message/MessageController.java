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

import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.TraitPropagator;
import jetbrains.jetpad.cell.util.Cells;

public final class MessageController {
  private static final CellPropertySpec<Boolean> HAS_MESSAGE_CONTROLLER =
      new CellPropertySpec<>("hasMessageController", false);

  public static final CellPropertySpec<String> ERROR = new CellPropertySpec<>("error");
  public static final CellPropertySpec<String> WARNING = new CellPropertySpec<>("warning");
  public static final CellPropertySpec<String> BROKEN = new CellPropertySpec<>("broken");
  public static final CellPropertySpec<String> INFO = new CellPropertySpec<>("info");

  public static Registration install(CellContainer container) {
    return install(container, null);
  }

  public static Registration install(CellContainer container, MessageStyler styler) {
    final MessageTrait trait = new MessageTrait(container, new StyleApplicator(styler));
    return TraitPropagator.install(container, trait, HAS_MESSAGE_CONTROLLER,
        new Predicate<Cell>() {
          @Override
          public boolean apply(Cell cell) {
            return canHaveMessages(cell);
          }
        },
        new Handler<Cell>() {
          @Override
          public void handle(Cell cell) {
            trait.detach(cell);
          }
        });
  }

  public static void setBroken(Cell cell, String message) {
    set(cell, message, BROKEN);
  }

  public static void setError(Cell cell, String message) {
    set(cell, message, ERROR);
  }

  public static void setWarning(Cell cell, String message) {
    set(cell, message, WARNING);
  }

  public static void setInfo(Cell cell, String message) {
    set(cell, message, INFO);
  }

  public static void set(Cell cell, String message, CellPropertySpec<String> prop) {
    if (canHaveMessages(cell)) {
      cell.set(prop, message);
    }
  }

  private static boolean canHaveMessages(Cell cell) {
    return !Cells.inPopup(cell);
  }

  public static boolean isBroken(Cell cell) {
    return cell.get(BROKEN) != null;
  }

  public static boolean hasWarning(Cell cell) {
    return cell.get(WARNING) != null;
  }

  public static boolean hasError(Cell cell) {
    return cell.get(ERROR) != null;
  }

  public static boolean hasInfo(Cell cell) {
    return cell.get(INFO) != null;
  }

  private MessageController() {
  }
}