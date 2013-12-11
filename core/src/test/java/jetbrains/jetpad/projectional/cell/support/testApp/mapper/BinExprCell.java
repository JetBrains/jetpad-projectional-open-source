/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.cell.support.testApp.mapper;

import jetbrains.jetpad.projectional.cell.HorizontalCell;
import jetbrains.jetpad.projectional.cell.TextCell;
import jetbrains.jetpad.projectional.cell.util.CellFactory;

import static jetbrains.jetpad.projectional.cell.util.CellFactory.horizontal;
import static jetbrains.jetpad.projectional.cell.util.CellFactory.space;
import static jetbrains.jetpad.projectional.cell.util.CellFactory.label;

public class BinExprCell extends HorizontalCell {
  public final HorizontalCell left = horizontal();
  public final HorizontalCell right = horizontal();
  public final TextCell sign;

  BinExprCell(String signText) {
    CellFactory.to(this, left, space(), sign = label(signText), space(), right);
    focusable().set(true);
  }
}