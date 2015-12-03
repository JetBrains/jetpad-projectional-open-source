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
import jetbrains.jetpad.cell.mappers.CellMapper;
import jetbrains.jetpad.cell.mappers.PopupPositionUpdater;
import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.projectional.view.View;
import jetbrains.jetpad.values.Color;

class BaseCellMapper<SourceT extends Cell, TargetT extends View> extends CellMapper<SourceT, View> {

  BaseCellMapper(SourceT source, TargetT target, CellToViewContext ctx) {
    super(source, target, ctx);
  }

  TargetT getTypedTarget() {
    return (TargetT) getTarget();
  }

  @Override
  protected CellToViewContext getContext() {
    return (CellToViewContext) super.getContext();
  }

  @Override
  protected CellMapper<? extends Cell, ? extends View> createMapper(Cell cell) {
    return CellMappers.create(cell, getContext());
  }

  protected void doAddChild(int index, View child) {
    getTarget().children().add(index, child);
  }

  @Override
  protected void doRemoveChild(int index) {
    getTarget().children().remove(index);
  }

  @Override
  protected void attachPopup(View popup) {
    getContext().popupView.children().add(popup);
  }

  @Override
  protected void detachPopup(View popup) {
    getContext().popupView.children().remove(popup);
  }

  @Override
  protected PopupPositionUpdater<View> popupPositionUpdater() {
    return new PopupPositioner(getTarget());
  }

  @Override
  protected Registration enablePopupUpdates() {
    return getTarget().bounds().addHandler(new EventHandler<PropertyChangeEvent<Rectangle>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Rectangle> event) {
        updatePopupPositions(getSource());
      }
    });
  }

  @Override
  protected void onDetach() {
    getTarget().children().clear();
    super.onDetach();
  }

  @Override
  protected void applyStyle(boolean selected, boolean focusHighlighted, boolean hasError, boolean hasWarning, Color background) {
    if (selected) {
      background = CellContainerToViewMapper.SELECTION_COLOR;
    } else if (focusHighlighted) {
      background = isLeaf() ? CellContainerToViewMapper.FOCUS_HIGHLIGHT_COLOR : CellContainerToViewMapper.SELECTION_COLOR;
    } else if (getSource().get(Cell.PAIR_HIGHLIGHTED)) {
      background = CellContainerToViewMapper.PAIR_HIGHLIGHT_COLOR;
    }
    getTarget().background().set(background);

    Color borderColor = hasError ? Color.PINK : (hasWarning ? Color.YELLOW : getSource().get(Cell.BORDER_COLOR));
    getTarget().border().set(borderColor);

    getTarget().visible().set(getSource().get(Cell.VISIBLE));
    getTarget().hasShadow().set(getSource().get(Cell.HAS_SHADOW));
  }
}