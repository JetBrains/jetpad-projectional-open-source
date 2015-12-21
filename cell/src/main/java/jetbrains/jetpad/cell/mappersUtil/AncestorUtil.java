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
package jetbrains.jetpad.cell.mappersUtil;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.values.Color;

public class AncestorUtil {
  public static Color getAncestorBackground(Cell container, Cell leaf) {
    Cell current = leaf;
    do {
      current = current.getParent();
      Color background = current.get(Cell.BACKGROUND);
      if (background != null) {
        return background;
      }
    } while (current != container);
    return null;
  }
}
