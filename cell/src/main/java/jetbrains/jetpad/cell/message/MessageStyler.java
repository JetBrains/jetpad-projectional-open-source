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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.values.Color;

public class MessageStyler {
  protected Registration doApplyBroken(Cell cell) {
    if (cell instanceof TextCell) {
      return cell.set(TextCell.TEXT_COLOR, Color.RED);
    }
    return cell.set(Cell.BACKGROUND, Color.LIGHT_PINK);
  }

  protected Registration doApplyError(Cell cell) {
    return cell.set(Cell.RED_UNDERLINE, true);
  }

  protected Registration doApplyWarning(Cell cell) {
    return cell.set(Cell.YELLOWISH_BACKGROUND, true);
  }
}