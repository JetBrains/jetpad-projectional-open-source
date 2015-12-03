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
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.mappers.CellMapper;
import jetbrains.jetpad.cell.mappers.PopupPositionUpdater;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.values.Color;

import java.util.AbstractList;
import java.util.List;

import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.CSS;
import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.ELEMENT;

abstract class BaseCellMapper<SourceT extends Cell> extends CellMapper<SourceT, Element> {
  private static final String BACKGROUND = "background";
  private static final String UNDERLINE_SUFFIX = " bottom repeat-x";

  private List<Node> myTarget = null;
  private boolean myWasPopup;

  BaseCellMapper(SourceT source, CellToDomContext ctx, Element target) {
    super(source, target, ctx);
  }

  @Override
  protected CellToDomContext getContext() {
    return (CellToDomContext) super.getContext();
  }

  @Override
  protected CellMapper<? extends Cell, ? extends Element> createMapper(Cell source) {
    return CellMappers.createMapper(source, getContext());
  }

  protected PopupPositionUpdater<Element> popupPositionUpdater() {
    return new PopupPositioner(getContext());
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    if (isAutoChildManagement()) {
      myTarget = divWrappedElementChildren(getTarget());
    }
    myWasPopup = Composites.<Cell>isNonCompositeChild(getSource());
    getSource().getProp(ELEMENT).set(getTarget());
    getTarget().addClassName(CSS.cell());
    super.onAttach(ctx);
  }

  @Override
  protected void onDetach() {
    getSource().getProp(ELEMENT).set(null);
    getTarget().removeClassName(CSS.cell());
    if (myWasPopup) {
      getTarget().removeFromParent();
    }
    super.onDetach();
  }

  @Override
  protected void doAddChild(int index, Element child) {
    myTarget.add(index, child);
  }

  @Override
  protected void doRemoveChild(int index) {
    myTarget.remove(index);
  }

  @Override
  protected void attachPopup(Element popup) {
    getContext().rootElement.appendChild(popup);
    popup.getStyle().setPosition(Style.Position.ABSOLUTE);
    popup.getStyle().setZIndex(100);
  }

  @Override
  protected void detachPopup(Element popup) {
    popup.removeFromParent();
  }

  @Override
  protected Registration enablePopupUpdates() {
    final Timer timer = new Timer() {
      @Override
      public void run() {
        updatePopupPositions(getSource());
      }
    };
    timer.scheduleRepeating(50);
    return new Registration() {
      @Override
      protected void doRemove() {
        timer.cancel();
      }
    };
  }

  @Override
  protected void applyStyle(boolean selected, boolean focusHighlighted, boolean hasError, boolean hasWarning, Color background) {
    updateCssStyle(CSS.selected(), (focusHighlighted && !isLeaf()) || selected);
    updateCssStyle(CSS.paired(), getSource().get(Cell.PAIR_HIGHLIGHTED));

    String backgroundColor = null;
    if (isLeaf() && focusHighlighted) {
      backgroundColor = CSS.currentHighlightColor();
    } else if (background != null) {
      backgroundColor = background.toCssColor();
    }
    String underline = hasError ? CSS.redUnderline() : (hasWarning ? CSS.yellowUnderline() : null);
    applyBackground(backgroundColor, underline);

    Style style = getTarget().getStyle();
    Color borderColor = getSource().get(Cell.BORDER_COLOR);
    style.setBorderStyle(borderColor == null ? Style.BorderStyle.NONE : Style.BorderStyle.SOLID);
    style.setBorderWidth(borderColor == null ? 0 : 1, Style.Unit.PX);
    style.setBorderColor(borderColor == null ? null : borderColor.toCssColor());

    updateCssStyle(CSS.hidden(), !getSource().get(Cell.VISIBLE));
    updateCssStyle(CSS.hasShadow(), getSource().get(Cell.HAS_SHADOW));
  }

  protected final void updateCssStyle(String cssStyle, boolean apply) {
    if (apply) {
      getTarget().addClassName(cssStyle);
    } else {
      getTarget().removeClassName(cssStyle);
    }
  }

  private void applyBackground(String color, String underline) {
    Style style = getTarget().getStyle();
    if (color == null) {
      if (underline == null) {
        style.clearProperty(BACKGROUND);
      } else {
        style.setProperty(BACKGROUND, underline + UNDERLINE_SUFFIX);
      }
    } else {
      if (underline == null) {
        style.setProperty(BACKGROUND, color);
      } else {
        style.setProperty(BACKGROUND, underline + UNDERLINE_SUFFIX + "," + color);
      }
    }
  }

  List<Node> divWrappedElementChildren(final Element e) {
    return new AbstractList<Node>() {
      @Override
      public Node get(int index) {
        return e.getChild(index).getFirstChild();
      }

      @Override
      public Node set(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        Element wrapperDiv = DOM.createDiv();
        wrapperDiv.appendChild(element);

        Node child = e.getChild(index);
        e.replaceChild(child, wrapperDiv);
        return child;
      }

      @Override
      public void add(int index, Node element) {
        if (element.getParentElement() != null) {
          throw new IllegalStateException();
        }

        Element wrapperDiv = DOM.createDiv();
        wrapperDiv.appendChild(element);
        if (index == 0) {
          e.insertFirst(wrapperDiv);
        } else {
          Node prev = e.getChild(index - 1);
          e.insertAfter(wrapperDiv, prev);
        }
      }

      @Override
      public Node remove(int index) {
        Element childWrapper = (Element) e.getChild(index);
        get(index).removeFromParent();
        e.removeChild(childWrapper);
        return childWrapper;
      }

      @Override
      public int size() {
        return e.getChildCount();
      }
    };
  }
}