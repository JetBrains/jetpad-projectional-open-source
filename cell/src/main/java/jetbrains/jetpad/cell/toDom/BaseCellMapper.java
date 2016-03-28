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
package jetbrains.jetpad.cell.toDom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.mappersUtil.CounterSpec;
import jetbrains.jetpad.cell.mappersUtil.Counters;
import jetbrains.jetpad.cell.mappersUtil.HasCounters;
import jetbrains.jetpad.cell.mappersUtil.PopupManager;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.CSS;
import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.ELEMENT;

abstract class BaseCellMapper<SourceT extends Cell> extends Mapper<SourceT, Element> implements HasCounters, EventHandler<PropertyChangeEvent<Cell>> {
  private static final String BACKGROUND = "background";
  private static final String UNDERLINE_SUFFIX = " bottom repeat-x";

  private CellToDomContext myContext;
  private Counters myCounters;

  private List<Mapper<?, ?>> myChildMappers = null;
  private List<Node> myChildTargets = null;

  private PopupManager myPopupManager;
  private Color myAncestorBackground;
  private boolean myWasPopup;

  BaseCellMapper(SourceT source, CellToDomContext ctx, Element target) {
    super(source, target);
    if (ctx == null) {
      throw new IllegalArgumentException();
    }
    myContext = ctx;
  }

  protected CellToDomContext getContext() {
    return myContext;
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    myContext.register(this);

    myWasPopup = Composites.<Cell>isNonCompositeChild(getSource());
    getSource().getProp(ELEMENT).set(getTarget());
    getTarget().addClassName(CSS.cell());

    if (isAutoChildManagement()) {
      myChildMappers = createChildList();
      myChildTargets = divWrappedElementChildren(getTarget());
      ObservableList<Cell> children = getSource().children();
      for (int i = 0; i < children.size(); i++) {
        childAdded(i, children.get(i));
      }
    }

    myPopupManager = createPopupManager();
    myPopupManager.attach(getSource());

    refreshProperties();
  }

  @Override
  protected void onDetach() {
    getSource().getProp(ELEMENT).set(null);
    getTarget().removeClassName(CSS.cell());
    if (myWasPopup) {
      getTarget().removeFromParent();
    }
    myPopupManager.dispose();
    myContext.unregister(this);
    super.onDetach();
  }

  protected boolean isLeaf() {
    return false;
  }

  protected boolean isAutoChildManagement() {
    return true;
  }

  protected boolean isAutoPopupManagement() {
    return true;
  }

  @Override
  public final int getCounter(CounterSpec spec) {
    if (myCounters == null) return 0;
    return myCounters.getCounter(spec);
  }

  @Override
  public final void changeCounter(CounterSpec spec, int delta) {
    if (myCounters == null) {
      myCounters = new Counters();
    }
    myCounters.changeCounter(spec, delta);
    if (myCounters.isEmpty()) {
      myCounters = null;
    }
  }

  void childAdded(int index, Cell child) {
    if (!isAutoChildManagement()) return;
    BaseCellMapper<? extends Cell> mapper = myContext.apply(child);
    myChildTargets.add(index, mapper.getTarget());
    myChildMappers.add(index, mapper);
  }

  void childRemoved(int index, Cell child) {
    if (!isAutoChildManagement()) return;
    myChildMappers.remove(index);
    myChildTargets.remove(index);
  }

  @Override
  public final void onEvent(PropertyChangeEvent<Cell> event) {
    myPopupManager.onEvent(event);
  }

  void onPopupPropertyChanged(CellPropertySpec<?> prop, PropertyChangeEvent<?> change) {
    if (getParent() == null) return;
    ((BaseCellMapper<?>) getParent()).myPopupManager.onPopupPropertyChanged(getSource(), prop, change);
  }

  void setAncestorBackground(Color background) {
    myAncestorBackground = background;
  }

  void refreshProperties() {
    boolean selected = getSource().get(Cell.SELECTED) || getCounter(Counters.SELECT_COUNT) > 0;
    boolean focusHighlighted = getSource().get(Cell.FOCUS_HIGHLIGHTED) || getCounter(Counters.HIGHLIGHT_COUNT) > 0;
    Color background = getSource().get(Cell.BACKGROUND);
    applyStyle(selected, focusHighlighted, (background == null ? myAncestorBackground : background));
  }

  private void applyStyle(boolean selected, boolean focusHighlighted, Color background) {
    updateCssStyle(CSS.selected(), (focusHighlighted && !isLeaf()) || selected);
    updateCssStyle(CSS.paired(), getSource().get(Cell.PAIR_HIGHLIGHTED));

    String backgroundColor = null;
    if (isLeaf() && focusHighlighted) {
      backgroundColor = CSS.currentHighlightColor();
    } else if (background != null) {
      backgroundColor = background.toCssColor();
    }
    String underline = getSource().get(Cell.RED_UNDERLINE) ? CSS.redUnderline()
        : (getSource().get(Cell.YELLOW_UNDERLINE) ? CSS.yellowUnderline() : null);
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
        style.setProperty(BACKGROUND, underline + UNDERLINE_SUFFIX + " " + color);
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

  protected PopupManager createPopupManager() {
    return new DomPopupManager(myContext) {
      @Override
      protected Collection<Mapper<? extends Cell, ? extends Element>> createContainer() {
        return createChildSet();
      }
    };
  }
}