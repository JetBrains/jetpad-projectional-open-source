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
package jetbrains.jetpad.cell.toView;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.util.CounterSpec;
import jetbrains.jetpad.cell.util.Counters;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.values.Color;

class BaseCellMapper<SourceT extends Cell, TargetT extends View> extends Mapper<SourceT, TargetT> {
  static final CounterSpec HIGHLIGHT_COUNT = new CounterSpec("focusHighlight");
  static final CounterSpec SELECT_COUNT = new CounterSpec("selectCount");
  static final CounterSpec ERROR_COUNT = new CounterSpec("errors");
  static final CounterSpec WARNING_COUNT = new CounterSpec("warning");

  private CellToViewContext myContext;

  private ObservableList<BaseCellMapper<?, ?>> myChildMappers;
  private ObservableSet<BaseCellMapper<?, ?>> myPopupMappers;

  private Counters myCounters;

  private Registration myPopupUpdateReg;
  
  BaseCellMapper(SourceT source, TargetT target, CellToViewContext ctx) {
    super(source, target);

    if (ctx == null) {
      throw new NullPointerException();
    }

    myContext = ctx;
    myChildMappers = createChildList();
  }

  protected CellToViewContext cellToViewContext() {
    return myContext;
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);

    myContext.register(this);

    refreshProperties();


    ObservableList<Cell> children = getSource().children();
    for (int i = 0; i < children.size(); i++) {
      childAdded(i, children.get(i));
    }

    if (isAutoPopupManagement()) {
      updatePopup(new PropertyChangeEvent<>(null, getSource().frontPopup().get()));
      updatePopup(new PropertyChangeEvent<>(null, getSource().bottomPopup().get()));
      updatePopup(new PropertyChangeEvent<>(null, getSource().leftPopup().get()));
      updatePopup(new PropertyChangeEvent<>(null, getSource().rightPopup().get()));
    }
  }

  @Override
  protected void onDetach() {
    myChildMappers.clear();
    getTarget().children().clear();

    if (myPopupUpdateReg != null) {
      myPopupUpdateReg.remove();
      myPopupUpdateReg = null;
    }

    myContext.unregister(this);

    super.onDetach();
  }

  int getCounter(CounterSpec spec) {
    if (myCounters == null) return 0;
    return myCounters.getCounter(spec);
  }

  void changeCounter(CounterSpec spec, int delta) {
    if (myCounters == null) {
      myCounters = new Counters();
    }
    myCounters.changeCounter(spec, delta);
    if (myCounters.isEmpty()) {
      myCounters = null;
    }
  }

  boolean isLeaf() {
    return false;
  }

  void refreshProperties() {
    Cell cell = getSource();
    View view = getTarget();

    boolean selected = getSource().selected().get() || getCounter(SELECT_COUNT) > 0;
    boolean paired = getSource().pairHighlighted().get();
    boolean focusHighlighted = getSource().focusHighlighted().get() || getCounter(HIGHLIGHT_COUNT) > 0;

    Color background = cell.background().get();
    if (selected) {
      background = CellContainerToViewMapper.SELECTION_COLOR;
    } else if (focusHighlighted) {
      background = isLeaf() ? CellContainerToViewMapper.FOCUS_HIGHLIGHT_COLOR : CellContainerToViewMapper.SELECTION_COLOR;
    } else if (paired) {
      background = CellContainerToViewMapper.PAIR_HIGHLIGHT_COLOR;
    }
    view.background().set(background);

    Color borderColor;
    if (cell.hasError().get() || getCounter(ERROR_COUNT) > 0) {
      borderColor = Color.RED;
    } else if (cell.hasWarning().get() || getCounter(WARNING_COUNT) > 0) {
      borderColor = Color.YELLOW;
    } else {
      borderColor = cell.borderColor().get();
    }
    view.border().set(borderColor);
    view.visible().set(cell.visible().get());
    view.hasShadow().set(cell.hasShadow().get());
  }

  void updatePopup(PropertyChangeEvent<Cell> event) {
    if (event.getOldValue() != null) {
      for (BaseCellMapper<?, ?> pm : myPopupMappers) {
        if (pm.getSource() == event.getOldValue()) {
          myPopupMappers.remove(pm);
          myContext.popupView().children().remove(pm.getTarget());
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
        myPopupUpdateReg = getTarget().bounds().addHandler(new EventHandler<PropertyChangeEvent<Rectangle>>() {
          @Override
          public void onEvent(PropertyChangeEvent<Rectangle> event) {
            updatePopupPositions(getSource());
          }
        });
      }

      BaseCellMapper<?, ?> pm = createMapper(event.getNewValue());
      myPopupMappers.add(pm);
      myContext.popupView().children().add(pm.getTarget());

      updatePopupPositions(getSource());
    }
  }

  protected void updatePopupPositions(Cell target) {
    Cell bottomPopup = target.bottomPopup().get();
    Cell frontPopup = target.frontPopup().get();
    Cell leftPopup = target.leftPopup().get();
    Cell rightPopup = target.rightPopup().get();

    refreshPopupPosition(target, bottomPopup, PopupPositionUpdaters.bottomUpdater());
    refreshPopupPosition(target, frontPopup, PopupPositionUpdaters.frontUpdater());
    refreshPopupPosition(target, leftPopup, PopupPositionUpdaters.leftUpdater());
    refreshPopupPosition(target, rightPopup, PopupPositionUpdaters.rightUpdater());
  }

  protected void refreshPopupPosition(Cell target, Cell popup, PopupPositionUpdater updater) {
    if (popup == null) return;
    BaseCellMapper<?, ?> popupMapper = (BaseCellMapper<?, ?>) getDescendantMapper(popup);
    updater.update(target.getBounds(), getTarget().container().visibleRect(), popupMapper.getTarget());
  }

  boolean isAutoChildManagement() {
    return true;
  }

  boolean isAutoPopupManagement() {
    return true;
  }

  void childAdded(int index, Cell child) {
    if (!isAutoChildManagement()) return;
    BaseCellMapper<?, ?> mapper = createMapper(child);
    myChildMappers.add(index, mapper);
    getTarget().children().add(index, mapper.getTarget());
  }

  void childRemoved(int index, Cell child) {
    if (!isAutoChildManagement()) return;
    myChildMappers.remove(index);
    getTarget().children().remove(index);
  }
  
  protected BaseCellMapper<?, ?> createMapper(Cell cell) {
    return CellMappers.create(cell, myContext);
  }

}