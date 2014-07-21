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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.composite.Composites;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

import java.util.AbstractList;
import java.util.List;

abstract class BaseCellMapper<SourceT extends Cell> extends Mapper<SourceT, Element> {
  private ObservableSet<Mapper<? extends Cell, ? extends Element>> myPopupMappers;
  private Registration myPopupUpdateReg;

  private List<Mapper<? extends Cell, ? extends Element>> myChildrenMappers;
  private List<Node> myTarget;
  private boolean myWasPopup;
  private CellToDomContext myContext;

  private int myExternalHighlightCount;
  private int myExternalSelectCount;

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
      if (getSource().bottomPopup().get() != null) {
        updatePopup(new PropertyChangeEvent<>(null, getSource().bottomPopup().get()));
      } else if (getSource().frontPopup().get() != null) {
        updatePopup(new PropertyChangeEvent<>(null, getSource().frontPopup().get()));
      } else if (getSource().leftPopup().get() != null) {
        updatePopup(new PropertyChangeEvent<>(null, getSource().leftPopup().get()));
      } else if (getSource().rightPopup().get() != null) {
        updatePopup(new PropertyChangeEvent<>(null, getSource().rightPopup().get()));
      }
    }

    getSource().getProp(CellContainerToDomMapper.ELEMENT).set(getTarget());
  }

  @Override
  protected void onDetach() {
    getSource().getProp(CellContainerToDomMapper.ELEMENT).set(null);

    if (myWasPopup) {
      getTarget().removeFromParent();
    }

    if (myPopupUpdateReg != null) {
      myPopupUpdateReg.remove();
      myPopupUpdateReg = null;
    }

    super.onDetach();
  }

  void changeExtenralHighlight(int delta) {
    myExternalHighlightCount += delta;
  }

  void changeExternalSelect(int delta) {
    myExternalSelectCount += delta;
  }

  boolean isEmpty() {
    return false;
  }

  protected void refreshProperties() {
    Style style = getTarget().getStyle();
    if (getSource().selected().get() || myExternalSelectCount > 0) {
      getTarget().addClassName(CellContainerToDomMapper.CSS.selected());
    } else {
      getTarget().removeClassName(CellContainerToDomMapper.CSS.selected());
    }

    if (!isEmpty() && (getSource().highlighted().get() || myExternalHighlightCount > 0)) {
      getTarget().addClassName(CellContainerToDomMapper.CSS.highlighted());
    } else {
      getTarget().removeClassName(CellContainerToDomMapper.CSS.highlighted());
    }

    Color background = getSource().background().get();
    style.setBackgroundColor(background == null ? "" : background.toCssColor());

    Color borderColor = getSource().borderColor().get();
    style.setBorderStyle(borderColor == null ? Style.BorderStyle.NONE : Style.BorderStyle.SOLID);
    style.setBorderWidth(borderColor == null ? 0 : 1, Style.Unit.PX);
    style.setBorderColor(borderColor == null ? null : borderColor.toCssColor());

    if (!getSource().visible().get()) {
      getTarget().addClassName(CellContainerToDomMapper.CSS.hidden());
    } else {
      getTarget().removeClassName(CellContainerToDomMapper.CSS.hidden());
    }

    if (getSource().hasShadow().get()) {
      getTarget().addClassName(CellContainerToDomMapper.CSS.hasShadow());
    } else {
      getTarget().removeClassName(CellContainerToDomMapper.CSS.hasShadow());
    }
  }

  void updatePopup(PropertyChangeEvent<Cell> event) {
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
            updatePopupPositions();
          }
        };
        timer.scheduleRepeating(50);
        myPopupUpdateReg = new Registration() {
          @Override
          public void remove() {
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

      updatePopupPositions();
    }
  }

  private void updatePopupPositions() {
    Rectangle bounds = targetBounds();
    Cell bottomPopup = getSource().bottomPopup().get();
    Cell frontPopup = getSource().frontPopup().get();
    Cell leftPopup = getSource().leftPopup().get();
    Cell rightPopup = getSource().rightPopup().get();
    PopupPositioner positioner = new PopupPositioner(myContext);

    if (bottomPopup != null) {
      positioner.positionBottom(bounds, ((BaseCellMapper<?>) getDescendantMapper(bottomPopup)).getTarget());
    }
    if (frontPopup != null) {
      positioner.positionFront(bounds, ((BaseCellMapper<?>) getDescendantMapper(frontPopup)).getTarget());
    }
    if (leftPopup != null) {
      positioner.positionLeft(bounds, ((BaseCellMapper<?>) getDescendantMapper(leftPopup)).getTarget());
    }
    if (rightPopup != null) {
      positioner.positionRight(bounds, ((BaseCellMapper<?>) getDescendantMapper(rightPopup)).getTarget());
    }
  }

  private Rectangle targetBounds() {
    return new Rectangle(
      getTarget().getAbsoluteLeft(), getTarget().getAbsoluteTop(),
      getTarget().getClientWidth(), getTarget().getClientHeight()
    );
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

  void childAdded(CollectionItemEvent<Cell> event) {
    if (isAutoChildManagement()) return;
    Mapper<? extends Cell, ? extends Element> mapper = createMapper(event.getItem());
    myChildrenMappers.add(event.getIndex(), mapper);
    myTarget.add(event.getIndex(), mapper.getTarget());
  }

  void childRemoved(CollectionItemEvent<Cell> event) {
    if (isAutoChildManagement()) return;
    myChildrenMappers.remove(event.getIndex());
    myTarget.remove(event.getIndex());
  }

  BaseCellMapper<?> createMapper(Cell source) {
    return CellMappers.createMapper(source, myContext);
  }
}