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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.cell.ScrollCell;

class ScrollCellMapper extends BaseCellMapper<ScrollCell> {
  ScrollCellMapper(ScrollCell source, CellToDomContext ctx) {
    super(source, ctx, DOM.createDiv());
  }

  @Override
  protected void refreshProperties() {
    super.refreshProperties();
    Style style = getTarget().getStyle();
    Vector maxDim = getSource().maxDimension().get();
    style.setProperty("maxWidth", maxDim.x + "px");
    style.setProperty("maxHeight", maxDim.y + "px");

    Boolean scroll = getSource().scroll().get();
    style.setOverflow(scroll ? Style.Overflow.SCROLL : Style.Overflow.VISIBLE);
  }
}