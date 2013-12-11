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
package jetbrains.jetpad.projectional.cell.hybrid.testapp.mapper;

import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.projectional.cell.hybrid.testapp.model.ComplexValueExpr;
import jetbrains.jetpad.projectional.cell.*;
import jetbrains.jetpad.projectional.cell.action.CellActions;
import jetbrains.jetpad.projectional.cell.util.CellFactory;
import jetbrains.jetpad.projectional.cell.support.ProjectionalSynchronizers;
import jetbrains.jetpad.projectional.cell.trait.BaseCellTrait;
import jetbrains.jetpad.projectional.cell.trait.CellTraitPropertySpec;

class ComplexValueExprMapper extends Mapper<ComplexValueExpr, HorizontalCell> {
  ComplexValueExprMapper(ComplexValueExpr source) {
    super(source, new HorizontalCell());
    HorizontalCell target = getTarget();
    target.focusable().set(true);
    target.children().add(CellFactory.label("aaaa"));
    final TextCell second = CellFactory.label("bbbb");
    target.children().add(second);
    target.children().add(CellFactory.label("cccc"));

    target.addTrait(new BaseCellTrait() {
      @Override
      public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
        if (spec == ProjectionalSynchronizers.ON_CREATE) return CellActions.toFirstFocusable(second);
        return super.get(cell, spec);
      }
    });
  }
}