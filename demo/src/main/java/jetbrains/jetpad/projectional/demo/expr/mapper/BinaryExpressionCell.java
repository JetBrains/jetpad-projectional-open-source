/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.demo.expr.mapper;

import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.indent.IndentCell;
import jetbrains.jetpad.cell.util.CellFactory;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.cell.util.Validators;

import static jetbrains.jetpad.cell.util.CellFactory.*;

class BinaryExpressionCell extends IndentCell {
  final IndentCell left = indent();
  final IndentCell right = indent();
  final TextCell sign;

  BinaryExpressionCell(String signText) {
    CellFactory.to(this, left, space(), sign = label(signText), space(), right);
    sign.addTrait(TextEditing.validTextEditing(Validators.equalsTo(signText)));
    focusable().set(true);
  }
}