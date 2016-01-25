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
package jetbrains.jetpad.cell.mappersUtil;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.mapper.Mapper;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

import java.util.Collection;

public abstract class BasePopupManager<TargetT> implements PopupManager {
  private Registration myUpdateRegistration = null;
  private Collection<Mapper<? extends Cell, ? extends TargetT>> myPopupMappers = null;

  protected abstract Collection<Mapper<? extends Cell, ? extends TargetT>> createContainer();
  protected abstract Mapper<? extends Cell, ? extends TargetT> attachPopup(Cell popup);
  protected abstract void detachPopup(Mapper<? extends Cell, ? extends TargetT> popupMapper);
  protected abstract Registration setPopupUpdate();
  protected abstract PopupPositionUpdater<TargetT> getPositionUpdater(Mapper<? extends Cell, ? extends TargetT> popupMapper);

  @Override
  public void dispose() {
    if (myUpdateRegistration != null) {
      myUpdateRegistration.remove();
      myUpdateRegistration = null;
    }
  }

  @Override
  public void attach(Cell cell) {
    for (CellPropertySpec<Cell> ps : Cell.POPUP_SPECS) {
      Cell popup = cell.get(ps);
      if (popup != null) {
        onEvent(new PropertyChangeEvent<>(null, popup));
      }
    }
  }

  @Override
  public final void onPopupPropertyChanged(Cell popup, CellPropertySpec<?> prop, PropertyChangeEvent<?> change) {
    if (prop == Cell.VISIBLE && (Boolean) change.getNewValue()) {
      Mapper<? extends Cell, ? extends TargetT> popupMapper = findPopupMapper(popup);
      updatePosition(popupMapper, false);
    }
  }

  @Override
  public final void onEvent(PropertyChangeEvent<Cell> event) {
    if (event.getOldValue() != null) {
      Mapper<? extends Cell, ? extends TargetT> popupMapper = findPopupMapper(event.getOldValue());
      myPopupMappers.remove(popupMapper);
      detachPopup(popupMapper);
      if (myPopupMappers.isEmpty()) {
        myPopupMappers = null;
        myUpdateRegistration.remove();
        myUpdateRegistration = null;
      }
    }
    if (event.getNewValue() != null) {
      if (myPopupMappers == null) {
        myPopupMappers = createContainer();
        myUpdateRegistration = setPopupUpdate();
      }
      updatePopupPositions();
      Mapper<? extends Cell, ? extends TargetT> popupMapper = attachPopup(event.getNewValue());
      myPopupMappers.add(popupMapper);
      updatePosition(popupMapper, true);
    }
  }

  private Mapper<? extends Cell, ? extends TargetT> findPopupMapper(Cell popup) {
    for (Mapper<? extends Cell, ? extends TargetT> popupMapper : myPopupMappers) {
      if (popupMapper.getSource() == popup) {
        return popupMapper;
      }
    }
    throw new IllegalStateException();
  }

  @Override
  public final void updatePopupPositions() {
    for (Mapper<? extends Cell, ? extends TargetT> popupMapper : myPopupMappers) {
      updatePosition(popupMapper, false);
    }
  }

  private void updatePosition(Mapper<? extends Cell, ? extends TargetT> popupMapper, boolean initial) {
    if (!initial && !popupMapper.getSource().get(Cell.VISIBLE)) return;
    Cell parent = popupMapper.getSource().getParent();
    if (parent == null) return;
    CellPropertySpec<Cell> spec = getSpec(parent, popupMapper.getSource());
    getPositionUpdater(popupMapper).update(spec, popupMapper.getTarget(), parent.getBounds(), hasOpposite(spec, parent));
  }

  private CellPropertySpec<Cell> getSpec(Cell parent, Cell popup) {
    for (CellPropertySpec<Cell> spec : Cell.POPUP_SPECS) {
      if (parent.get(spec) == popup) return spec;
    }
    throw new RuntimeException();
  }

  private boolean hasOpposite(CellPropertySpec<Cell> spec, Cell parent) {
    CellPropertySpec<Cell> opposite = getOpposite(spec);
    if (opposite == null) return false;
    Cell oppositePopup = parent.get(opposite);
    return oppositePopup != null && oppositePopup.get(Cell.VISIBLE);
  }

  private CellPropertySpec<Cell> getOpposite(CellPropertySpec<Cell> prop) {
    if (Cell.BOTTOM_POPUP == prop) return Cell.TOP_POPUP;
    if (Cell.TOP_POPUP == prop) return Cell.BOTTOM_POPUP;
    if (Cell.RIGHT_POPUP == prop) return Cell.LEFT_POPUP;
    if (Cell.LEFT_POPUP == prop) return Cell.RIGHT_POPUP;
    return null;
  }
}