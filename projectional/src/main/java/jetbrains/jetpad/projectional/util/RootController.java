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
package jetbrains.jetpad.projectional.util;

import com.google.common.base.Function;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellContainer;
import jetbrains.jetpad.cell.error.ErrorController;
import jetbrains.jetpad.cell.error.ErrorStyler;
import jetbrains.jetpad.cell.text.TextEditing;

import java.util.Collections;
import java.util.List;

public class RootController {
  public static Registration install(CellContainer container) {
    return CellNavigationController.install(container);
  }

  public static Registration supportErrors(CellContainer container) {
    return supportErrors(container, null, null);
  }

  public static Registration supportErrors(CellContainer container, ErrorStyler defaultErrorStyler,
                                           List<Function<Cell, ErrorStyler>> customStylers) {
    if (customStylers == null) {
      customStylers = Collections.singletonList(TextEditing.errorStyler());
    } else {
      customStylers.add(TextEditing.errorStyler());
    }
    return ErrorController.install(container, defaultErrorStyler, customStylers);
  }
}