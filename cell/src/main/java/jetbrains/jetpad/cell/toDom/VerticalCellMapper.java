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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.VerticalCell;

class VerticalCellMapper extends BaseCellMapper<VerticalCell> {
  VerticalCellMapper(VerticalCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
    getTarget().addClassName(CellContainerToDomMapper.CSS.vertical());
  }

  @Override
  public void refreshProperties() {
    super.refreshProperties();

    updateCssStyle(CellContainerToDomMapper.CSS.indented(), getSource().get(VerticalCell.INDENTED));
  }
}