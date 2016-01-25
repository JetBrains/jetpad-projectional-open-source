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
package jetbrains.jetpad.cell.text;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.cell.position.PositionHandler;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.DerivedProperty;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.PropertyChangeEvent;
import jetbrains.jetpad.model.property.ReadableProperty;

import static jetbrains.jetpad.cell.text.TextNavigationTrait.getMaxPos;
import static jetbrains.jetpad.cell.text.TextNavigationTrait.getMinPos;

public class TextPositionHandler implements PositionHandler {
  private final TextEditor myEditor;

  public TextPositionHandler(TextEditor editor) {
    myEditor = editor;
  }

  @Override
  public boolean isHome() {
    return getMinPos(myEditor) == myEditor.caretPosition().get();
  }

  @Override
  public boolean isEnd() {
    return getMaxPos(myEditor) == myEditor.caretPosition().get();
  }

  @Override
  public void home() {
    myEditor.caretPosition().set(getMinPos(myEditor));
  }

  @Override
  public void end() {
    myEditor.caretPosition().set(getMaxPos(myEditor));
  }

  @Override
  public Property<Integer> caretOffset() {
    final ReadableProperty<Integer> derivedOffset = new DerivedProperty<Integer>(myEditor.caretPosition()) {
      @Override
      public Integer doGet() {
        return myEditor.getCaretOffset(myEditor.caretPosition().get());
      }

      @Override
      public String getPropExpr() {
        return "derivedOffset(" + myEditor + ")";
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
        myEditor.caretPosition().set(myEditor.getCaretAt(value));
      }

      @Override
      public String getPropExpr() {
        return "caretOffset(" + myEditor + ")";
      }
    };
  }
}