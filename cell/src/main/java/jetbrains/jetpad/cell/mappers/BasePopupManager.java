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
  public final void onEvent(PropertyChangeEvent<Cell> event) {
    if (event.getOldValue() != null) {
      for (Mapper<? extends Cell, ? extends TargetT> popupMapper : myPopupMappers) {
        if (popupMapper.getSource() == event.getOldValue()) {
          myPopupMappers.remove(popupMapper);
          detachPopup(popupMapper);
          break;
        }
      }
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
      Mapper<? extends Cell, ? extends TargetT> popupMapper = attachPopup(event.getNewValue());
      myPopupMappers.add(popupMapper);
      updatePopupPositions();
    }
  }

  @Override
  public final void updatePopupPositions() {
    for (Mapper<? extends Cell, ? extends TargetT> popupMapper : myPopupMappers) {
      if (!popupMapper.getSource().get(Cell.VISIBLE)) continue;
      Cell parent = popupMapper.getSource().getParent();
      if (parent == null) continue;
      CellPropertySpec<Cell> spec = getSpec(parent, popupMapper.getSource());
      getPositionUpdater(popupMapper).update(spec, popupMapper.getTarget(), parent.getBounds());
    }
  }

  private CellPropertySpec<Cell> getSpec(Cell parent, Cell popup) {
    for (CellPropertySpec<Cell> spec : Cell.POPUP_SPECS) {
      if (parent.get(spec) == popup) return spec;
    }
    throw new RuntimeException();
  }
}
