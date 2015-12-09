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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.StyleElement;
import com.google.gwt.dom.client.StyleInjector;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.values.Color;

import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.CSS;

class Tooltip {
  private static final int PSEUDO_ELEMENT_HEIGHT = 6;

  static Registration applyDecoration(Mapper<? extends Cell, ? extends Element> popupMapper) {
    final Cell cell = popupMapper.getSource();
    if (!cell.isPopup() || !cell.get(Cell.HAS_POPUP_DECORATION)) return Registration.EMPTY;
    String id = CSS.tooltip() + Integer.toHexString(cell.hashCode());
    popupMapper.getTarget().addClassName(CSS.tooltip());
    popupMapper.getTarget().addClassName(id);
    return injectCornerStyle(id, cell);
  }

  private static Registration injectCornerStyle(String id, Cell cell) {
    Color borderColor = cell.get(Cell.BORDER_COLOR);
    String border = borderColor == null ? "none" : "1px solid " + borderColor.toCssColor();
    Color backgroundColor = cell.get(Cell.BACKGROUND);
    String background = backgroundColor == null ? Color.WHITE.toCssColor() : backgroundColor.toCssColor();
    final StyleElement styleElement = StyleInjector.injectStylesheet("." + id
        + "::before { border-top: " + border + "; border-left: " + border + "; " +
        "background: linear-gradient(135deg, " + background + " 0%, " + background
        + " 70%, rgba(0,0,0,0) 71%, rgba(0,0,0,0) 100%) }");
    StyleInjector.flush();
    return new Registration() {
      @Override
      protected void doRemove() {
        styleElement.removeFromParent();
      }
    };
  }

  static void bottom(Element tooltip) {
    if (!isTooltip(tooltip)) return;
    tooltip.getStyle().setMarginTop(PSEUDO_ELEMENT_HEIGHT, Style.Unit.PX);
    tooltip.removeClassName(CSS.tooltipTop());
    tooltip.addClassName(CSS.tooltipBottom());
  }

  static void top(Element tooltip) {
    if (!isTooltip(tooltip)) return;
    tooltip.getStyle().clearMarginTop();
    tooltip.removeClassName(CSS.tooltipBottom());
    tooltip.addClassName(CSS.tooltipTop());
  }

  static int height(Element popup) {
    int height = popup.getAbsoluteBottom() - popup.getAbsoluteTop();
    return isTooltip(popup) ? height + PSEUDO_ELEMENT_HEIGHT : height;
  }

  private static boolean isTooltip(Element element) {
    return element.getClassName().contains(CSS.tooltip());
  }
}
