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
package jetbrains.jetpad.cell.mappers;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.toUtil.CounterSpec;
import jetbrains.jetpad.cell.toUtil.Counters;
import jetbrains.jetpad.cell.toUtil.HasCounters;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.mapper.MappingContext;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.values.Color;

import java.util.List;
import java.util.Set;

public abstract class CellMapper<SourceT extends Cell, TargetT> extends Mapper<SourceT, TargetT> implements HasCounters {
  private CellMapperContext<TargetT> myContext;

  private List<Mapper<?, ?>> myChildMappers;

  private Set<Mapper<? extends Cell, ? extends TargetT>> myPopupMappers;
  private Registration myPopupUpdateReg;

  private Counters myCounters;
  private Color myAncestorBackground;

  protected CellMapper(SourceT source, TargetT target, CellMapperContext<TargetT> context) {
    super(source, target);
    if (context == null) {
      throw new IllegalArgumentException();
    }
    myContext = context;
  }

  protected abstract CellMapper<? extends Cell, ? extends TargetT> createMapper(Cell source);
  protected abstract void doAddChild(int index, TargetT child);
  protected abstract void doRemoveChild(int index);
  protected abstract void attachPopup(TargetT popup);
  protected abstract void detachPopup(TargetT popup);
  protected abstract PopupPositionUpdater<TargetT> popupPositionUpdater();
  protected abstract Registration enablePopupUpdates();
  protected abstract void applyStyle(boolean selected, boolean focusHighlighted, boolean hasError, boolean hasWarning, Color background);

  protected CellMapperContext<? super TargetT> getContext() {
    return myContext;
  }

  public boolean isAutoPopupManagement() {
    return true;
  }

  protected boolean isAutoChildManagement() {
    return true;
  }

  protected boolean isLeaf() {
    return false;
  }

  public void setAncestorBackground(Color color) {
    myAncestorBackground = color;
  }

  @Override
  protected void onAttach(MappingContext ctx) {
    super.onAttach(ctx);
    myContext.register(this);
    if (isAutoChildManagement()) {
      myChildMappers = createChildList();
      ObservableList<Cell> children = getSource().children();
      for (int i = 0; i < children.size(); i++) {
        childAdded(i, children.get(i));
      }
    }
    if (isAutoPopupManagement()) {
      updatePopups(getSource());
    }
    refreshProperties();
  }

  @Override
  protected void onDetach() {
    if (myPopupUpdateReg != null) {
      myPopupUpdateReg.remove();
      myPopupUpdateReg = null;
    }
    myContext.unregister(this);
    super.onDetach();
  }

  public final void childAdded(int index, Cell child) {
    if (!isAutoChildManagement()) return;
    Mapper<?, ? extends TargetT> mapper = createMapper(child);
    myChildMappers.add(index, mapper);
    doAddChild(index, mapper.getTarget());
  }

  public final void childRemoved(int index, Cell child) {
    if (!isAutoChildManagement()) return;
    myChildMappers.remove(index);
    doRemoveChild(index);
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

  public void refreshProperties() {
    boolean selected = getSource().get(Cell.SELECTED) || getCounter(Counters.SELECT_COUNT) > 0;
    boolean focusHighlighted = getSource().get(Cell.FOCUS_HIGHLIGHTED) || getCounter(Counters.HIGHLIGHT_COUNT) > 0;
    boolean hasError = getSource().get(Cell.HAS_ERROR) || getCounter(Counters.ERROR_COUNT) > 0;
    boolean hasWarning = getSource().get(Cell.HAS_WARNING) || getCounter(Counters.WARNING_COUNT) > 0;
    Color background = getSource().get(Cell.BACKGROUND);
    applyStyle(selected, focusHighlighted, hasError, hasWarning, (background == null ? myAncestorBackground : background));
  }

  public final void updatePopup(PropertyChangeEvent<Cell> event) {
    if (event.getOldValue() != null) {
      for (Mapper<? extends Cell, ? extends TargetT> pm : myPopupMappers) {
        if (pm.getSource() == event.getOldValue()) {
          myPopupMappers.remove(pm);
          detachPopup(pm.getTarget());
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
        myPopupUpdateReg = enablePopupUpdates();
      }
      Mapper<? extends Cell, ? extends TargetT> pm = createMapper(event.getNewValue());
      myPopupMappers.add(pm);
      attachPopup(pm.getTarget());
      updatePopupPositions(getSource());
    }
  }

  protected final void updatePopupPositions(Cell c) {
    PopupPositionUpdater<TargetT> p = null;
    for (CellPropertySpec<Cell> ps : Cell.POPUP_SPECS) {
      Cell popup = c.get(ps);
      if (popup != null) {
        Mapper<?, ? extends TargetT> popupMapper = (Mapper<?, ? extends TargetT>) getDescendantMapper(popup);
        if (p == null) {
          p = popupPositionUpdater();
        }
        p.update(ps, popupMapper.getTarget(), c.getBounds());
      }
    }
  }

  private void updatePopups(Cell c) {
    for (CellPropertySpec<Cell> ps : Cell.POPUP_SPECS) {
      Cell popup = c.get(ps);
      if (popup != null) {
        updatePopup(new PropertyChangeEvent<>(null, popup));
      }
    }
  }
}


