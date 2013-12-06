/*
 * Copyright 2012-2013 JetBrains s.r.o
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
package jetbrains.jetpad.projectional.cell.support;

import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.Registration;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.projectional.cell.TextCell;

import static jetbrains.jetpad.projectional.cell.support.TextNavigationTrait.getMaxPos;
import static jetbrains.jetpad.projectional.cell.support.TextNavigationTrait.getMinPos;

class TextCellPositionHandler implements PositionHandler {
  private final TextCell myTextCell;

  TextCellPositionHandler(TextCell textCell) {
    myTextCell = textCell;
  }

  @Override
  public boolean isHome() {
    return getMinPos(myTextCell) == myTextCell.caretPosition().get();
  }

  @Override
  public boolean isEnd() {
    return getMaxPos(myTextCell) == myTextCell.caretPosition().get();
  }

  @Override
  public void home() {
    myTextCell.caretPosition().set(getMinPos(myTextCell));
  }

  @Override
  public void end() {
    myTextCell.caretPosition().set(getMaxPos(myTextCell));
  }

  @Override
  public Property<Integer> caretOffset() {
    final ReadableProperty<Integer> derivedOffset = new DerivedProperty<Integer>(myTextCell.caretPosition()) {
      @Override
      public Integer get() {
        return myTextCell.getCaretOffset(myTextCell.caretPosition().get());
      }

      @Override
      public String getPropExpr() {
        return "derivedOffset(" + myTextCell + ")";
      }
    };

    return new Property<Integer>() {
      @Override
      public Integer get() {
        return derivedOffset.get();
      }

      @Override
      public Registration addHandler(EventHandler<? super PropertyChangeEvent<Integer>> handler) {
        return derivedOffset.addHandler(handler);
      }

      @Override
      public void set(Integer value) {
        myTextCell.caretPosition().set(myTextCell.getCaretAt(value));
      }

      @Override
      public String getPropExpr() {
        return "caretOffset(" + myTextCell + ")";
      }
    };
  }
}