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

import com.google.common.base.Joiner;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.decorations.Popups;
import jetbrains.jetpad.cell.toUtil.CounterSpec;
import jetbrains.jetpad.cell.toUtil.Counters;
import jetbrains.jetpad.cell.toUtil.HasCounters;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.CSS;
import static jetbrains.jetpad.cell.toDom.CellContainerToDomMapper.ELEMENT;

abstract class BaseCellMapper<SourceT extends Cell>
    extends Mapper<SourceT, Element>
    implements HasCounters, EventHandler<PropertyChangeEvent<Cell>> {

  private ObservableSet<Mapper<? extends Cell, ? extends Element>> myPopupMappers;
  private Registration myPopupUpdateReg;

  private List<Mapper<? extends Cell, ? extends Element>> myChildrenMappers;
  private List<Node> myTarget;
  private boolean myWasPopup;
  private CellToDomContext myContext;

  private Counters myCounters;
  private Color myAncestorBackground;

  BaseCellMapper(SourceT source, CellToDomContext ctx, Element target) {
    super(source, target);
    myContext = ctx;
  }

  protected CellToDomContext getContext() {
    return myContext;
  }

  protected boolean isAutoChildManagement() {
    return true;
  }

  protected boolean isAutoPopupManagement() {
    return true;
  }

  @Override
  protected void registerSynchronizers(SynchronizersConfiguration conf) {
    super.registerSynchronizers(conf);
    myTarget = divWrappedElementChildren(getTarget());
    if (isAutoChildManagement()) {
      myChildrenMappers = createChildList();
      for (Cell child : getSource().children()) {
        Mapper<? extends Cell, ? extends Element> mapper = createMapper(child);
        myChildrenMappers.add(mapper);
        myTarget.add(mapper.getTarget());
      }
    }
    refreshProperties();
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);
    myWasPopup = Composites.<Cell>isNonCompositeChild(getSource());
    if (isAutoPopupManagement()) {
      Popups.updatePopups(getSource(), this);
    }
    getSource().getProp(ELEMENT).set(getTarget());
    getTarget().addClassName(CSS.cell());
    myContext.register(this);
  }

  @Override
  protected void onDetach() {
    myContext.unregister(this);
    getSource().getProp(ELEMENT).set(null);
    getTarget().removeClassName(CSS.cell());
    if (myWasPopup) {
      getTarget().removeFromParent();
    }
    if (myPopupUpdateReg != null) {
      myPopupUpdateReg.remove();
      myPopupUpdateReg = null;
    }
    super.onDetach();
  }

  @Override
  public int getCounter(CounterSpec spec) {
    if (myCounters == null) return 0;
    return myCounters.getCounter(spec);
  }

  @Override
  public void changeCounter(CounterSpec spec, int delta) {
    if (myCounters == null) {
      myCounters = new Counters();
    }
    myCounters.changeCounter(spec, delta);
    if (myCounters.isEmpty()) {
      myCounters = null;
    }
  }

  public void setAncestorBackground(Color color) {
    myAncestorBackground = color;
  }

  boolean isEmpty() {
    return false;
  }

  boolean isLeaf() {
    return false;
  }

  protected void refreshProperties() {
    Style style = getTarget().getStyle();

    boolean selected = getSource().selected().get() || getCounter(Counters.SELECT_COUNT) > 0;
    boolean paired = getSource().pairHighlighted().get();
    boolean focusHighlighted = getSource().focusHighlighted().get() || getCounter(Counters.HIGHLIGHT_COUNT) > 0;

    getTarget().removeClassName(CSS.selected());
    getTarget().removeClassName(CSS.paired());

    if (focusHighlighted) {
      if (!isLeaf()) {
        getTarget().addClassName(CSS.selected());
      }
    }

    if (paired) {
      getTarget().addClassName(CSS.paired());
    }

    if (selected) {
      getTarget().addClassName(CSS.selected());
    }

    Color bg = getSource().get(Cell.BACKGROUND);
    Color background = bg == null ? myAncestorBackground : bg;
    List<String> backgrounds = new ArrayList<>();
    if (isLeaf() && focusHighlighted) {
      backgrounds.add(CSS.currentHighlightColor());
    } else if (background != null) {
      backgrounds.add(background.toCssColor());
    }

    String underlineSuffix = " bottom repeat-x";
    if (getSource().hasError().get() || getCounter(Counters.ERROR_COUNT) > 0) {
      backgrounds.add(0, CSS.redUnderline() + underlineSuffix);
    } else if (getSource().hasWarning().get() || getCounter(Counters.WARNING_COUNT) > 0) {
      backgrounds.add(0, CSS.yellowUnderline() + underlineSuffix);
    }

    if (backgrounds.isEmpty()) {
      style.clearProperty("background");
    } else {
      style.setProperty("background", Joiner.on(",").join(backgrounds));
    }

    Color borderColor = getSource().get(Cell.BORDER_COLOR);
    style.setBorderStyle(borderColor == null ? Style.BorderStyle.NONE : Style.BorderStyle.SOLID);
    style.setBorderWidth(borderColor == null ? 0 : 1, Style.Unit.PX);
    style.setBorderColor(borderColor == null ? null : borderColor.toCssColor());

    if (getSource().get(Cell.VISIBLE)) {
      getTarget().removeClassName(CSS.hidden());
    } else {
      getTarget().addClassName(CSS.hidden());
    }

    if (getSource().get(Cell.HAS_SHADOW)) {
      getTarget().addClassName(CSS.hasShadow());
    } else {
      getTarget().removeClassName(CSS.hasShadow());
    }
  }

  @Override
  public final void onEvent(PropertyChangeEvent<Cell> event) {
    if (event.getOldValue() != null) {
      for (Mapper<? extends Cell, ? extends Element> pm : myPopupMappers) {
        if (pm.getSource() == event.getOldValue()) {
          myPopupMappers.remove(pm);
          pm.getTarget().removeFromParent();
          break;
        }
      }
      if (myPopupMappers.isEmpty()) {
        myPopupMappers = null;
        myPopupUpdateReg.remove();
        myPopupUpdateReg = null;
      }
    }

    if (event.getNewValue() != null) {
      if (myPopupMappers == null) {
        myPopupMappers = createChildSet();
        final Timer timer = new Timer() {
          @Override
          public void run() {
            updatePopupPositions(getSource());
          }
        };
        timer.scheduleRepeating(50);
        myPopupUpdateReg = new Registration() {
          @Override
          protected void doRemove() {
            timer.cancel();
          }
        };
      }
      BaseCellMapper<?> pm = createMapper(event.getNewValue());
      Element target = pm.getTarget();
      myPopupMappers.add(pm);
      myContext.rootElement.appendChild(target);
      target.getStyle().setPosition(Style.Position.ABSOLUTE);
      target.getStyle().setZIndex(100);
      updatePopupPositions(getSource());
    }
  }

  protected void updatePopupPositions(Cell cell) {
    Popups.updatePopupsPositions(cell, new PopupPositioner(myContext), this);
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

  void childAdded(CollectionItemEvent<? extends Cell> event) {
    if (!isAutoChildManagement()) return;
    Mapper<? extends Cell, ? extends Element> mapper = createMapper(event.getNewItem());
    myChildrenMappers.add(event.getIndex(), mapper);
    myTarget.add(event.getIndex(), mapper.getTarget());
  }

  void childRemoved(CollectionItemEvent<? extends Cell> event) {
    if (!isAutoChildManagement()) return;
    myChildrenMappers.remove(event.getIndex());
    myTarget.remove(event.getIndex());
  }

  BaseCellMapper<?> createMapper(Cell source) {
    return CellMappers.createMapper(source, myContext);
  }
}