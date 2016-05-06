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
package jetbrains.jetpad.cell.trait;

import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.CellPropertySpec;
import jetbrains.jetpad.cell.event.CompletionEvent;
import jetbrains.jetpad.cell.event.FocusEvent;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.model.property.PropertyChangeEvent;

/**
 * Cell trait which 'extends' a number of other cell traits.
 *
 * Using this class results in a structure resembling a JS prototype chain and multiple inheritance hierarchy.
 */
public abstract class DerivedCellTrait extends CellTrait {
  protected abstract CellTrait getBase(Cell cell);

  @Override
  public void onAdd(Cell cell) {
    getBase(cell).onAdd(cell);
  }

  @Override
  public void onRemove(Cell cell) {
    getBase(cell).onRemove(cell);
  }

  @Override
  public void onPropertyChanged(Cell cell, CellPropertySpec<?> prop, PropertyChangeEvent<?> event) {
    getBase(cell).onPropertyChanged(cell, prop, event);
  }

  @Override
  public void onFocusGained(Cell cell, FocusEvent event) {
    getBase(cell).onFocusGained(cell, event);
  }

  @Override
  public void onFocusLost(Cell cell, FocusEvent event) {
    getBase(cell).onFocusLost(cell, event);
  }

  @Override
  public void onMouseClicked(Cell cell, MouseEvent event) {
    getBase(cell).onMouseClicked(cell, event);
  }

  @Override
  public void onMousePressed(Cell cell, MouseEvent event) {
    getBase(cell).onMousePressed(cell, event);
  }

  @Override
  public void onMouseReleased(Cell cell, MouseEvent event) {
    getBase(cell).onMouseReleased(cell, event);
  }

  @Override
  public void onMouseMoved(Cell cell, MouseEvent event) {
    getBase(cell).onMouseMoved(cell, event);
  }

  @Override
  public void onMouseDragged(Cell cell, MouseEvent event) {
    getBase(cell).onMouseDragged(cell, event);
  }

  @Override
  public void onMouseEntered(Cell cell, MouseEvent event) {
    getBase(cell).onMouseEntered(cell, event);
  }

  @Override
  public void onMouseLeft(Cell cell, MouseEvent event) {
    getBase(cell).onMouseLeft(cell, event);
  }

  @Override
  public void onKeyPressed(Cell cell, KeyEvent event) {
    getBase(cell).onKeyPressed(cell, event);
  }

  @Override
  public void onKeyPressedLowPriority(Cell cell, KeyEvent event) {
    getBase(cell).onKeyPressedLowPriority(cell, event);
  }

  @Override
  public void onKeyReleased(Cell cell, KeyEvent event) {
    getBase(cell).onKeyReleased(cell, event);
  }

  @Override
  public void onKeyReleasedLowPriority(Cell cell, KeyEvent event) {
    getBase(cell).onKeyReleasedLowPriority(cell, event);
  }

  @Override
  public void onKeyTyped(Cell cell, KeyEvent event) {
    getBase(cell).onKeyTyped(cell, event);
  }

  @Override
  public void onKeyTypedLowPriority(Cell cell, KeyEvent event) {
    getBase(cell).onKeyTypedLowPriority(cell, event);
  }

  @Override
  public void onCopy(Cell cell, CopyCutEvent event) {
    getBase(cell).onCopy(cell, event);
  }

  @Override
  public void onCut(Cell cell, CopyCutEvent event) {
    getBase(cell).onCut(cell, event);
  }

  @Override
  public void onPaste(Cell cell, PasteEvent event) {
    getBase(cell).onPaste(cell, event);
  }

  @Override
  public void onComplete(Cell cell, CompletionEvent event) {
    getBase(cell).onComplete(cell, event);
  }

  @Override
  public void onCellTraitEvent(Cell cell, CellTraitEventSpec<?> spec, Event event) {
    getBase(cell).onCellTraitEvent(cell, spec, event);
  }

  @Override
  protected void provideProperties(Cell cell, PropertyCollector collector) {
    getBase(cell).provideProperties(cell, collector);
  }


  @Override
  public Object get(Cell cell, CellTraitPropertySpec<?> spec) {
    return getBase(cell).get(cell, spec);
  }
}