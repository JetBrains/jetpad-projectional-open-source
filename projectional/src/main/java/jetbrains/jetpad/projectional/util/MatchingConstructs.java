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
package jetbrains.jetpad.projectional.util;

import jetbrains.jetpad.cell.Cell;

public class MatchingConstructs {
  public static void pair(Cell c1, Cell c2) {
    pairOneWay(c1, c2);
    pairOneWay(c2, c1);
  }

  private static void pairOneWay(Cell c1, Cell c2) {
    c1.set(CellNavigationController.PAIR_CELL, c2);
  }
}