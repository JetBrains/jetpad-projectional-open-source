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
package jetbrains.jetpad.projectional.testApp.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.projectional.testApp.model.IdExpr;
import jetbrains.jetpad.base.Validators;

public class IdExprMapper extends Mapper<IdExpr, TextCell> {
  IdExprMapper(IdExpr source) {
    super(source, new TextCell());
    getTarget().text().set("id");
    getTarget().addTrait(TextEditing.validTextEditing(Validators.equalsTo("id")));
  }
}