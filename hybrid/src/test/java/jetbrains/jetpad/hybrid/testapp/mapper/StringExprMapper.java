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
package jetbrains.jetpad.hybrid.testapp.mapper;

import jetbrains.jetpad.cell.HorizontalCell;
import jetbrains.jetpad.cell.TextCell;
import jetbrains.jetpad.cell.action.CellActions;
import jetbrains.jetpad.cell.text.TextEditing;
import jetbrains.jetpad.hybrid.testapp.model.StringExpr;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.Synchronizers;
import jetbrains.jetpad.projectional.cell.ProjectionalSynchronizers;

import static jetbrains.jetpad.cell.util.CellFactory.label;
import static jetbrains.jetpad.cell.util.CellFactory.to;

class StringExprMapper extends Mapper<StringExpr, StringExprMapper.StringExprCell> {
  StringExprMapper(StringExpr source) {
    super(source, new StringExprCell(source.quote.get()));
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    conf.add(Synchronizers.forPropsTwoWay(getSource().body, getTarget().body.text()));
  }

  static class StringExprCell extends HorizontalCell {
    private final TextCell body = new TextCell();

    private StringExprCell(String quote) {
      to(
          this,
          label(quote, true, false),
          body,
          label(quote, false, true)
      );
      set(ProjectionalSynchronizers.ON_CREATE, CellActions.toFirstFocusable(body));
      body.addTrait(TextEditing.textEditing());
    }
  }
}
