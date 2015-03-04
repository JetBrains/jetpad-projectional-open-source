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
package jetbrains.jetpad.projectional.util.awt;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.toView.CellToView;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.projectional.view.ViewContainer;
import jetbrains.jetpad.projectional.view.toAwt.AwtViewDemo;

public class AwtCellDemo {
  public static void show(final CellContainer container) {
    ViewContainer viewContainer = new ViewContainer();
    CellToView.map(container, viewContainer);

    Cell firstFocusable = Composites.<Cell>firstFocusable(container.root);
    if (firstFocusable != null) {
      firstFocusable.focus();
    }

    AwtViewDemo.show(viewContainer);
  }
}