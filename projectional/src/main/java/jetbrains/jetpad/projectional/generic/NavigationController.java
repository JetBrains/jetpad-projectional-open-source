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
package jetbrains.jetpad.projectional.generic;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.event.*;
import jetbrains.jetpad.model.composite.*;
import jetbrains.jetpad.model.event.CompositeRegistration;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.property.*;

import java.util.Stack;

import static jetbrains.jetpad.model.composite.Composites.*;

public abstract class NavigationController<ViewT extends Composite<ViewT> & HasFocusability & HasBounds & HasVisibility> {
  private Value<Integer> myPrevXOffset = new Value<Integer>(null);
  private Value<Boolean> myStackResetEnabled = new Value<Boolean>(true);
  private Stack<ViewT> mySelectionStack = new Stack<ViewT>();

  protected NavigationController() {
  }

  protected abstract Property<ViewT> focusedView();
  protected abstract void scrollTo(ViewT view);
  protected abstract ViewT root();
  protected abstract void moveToHome(ViewT view);
  protected abstract void moveToEnd(ViewT view);
  protected abstract void moveCaretTo(ViewT view, int offset);
  protected abstract Selector<ViewT, ReadableProperty<Integer>> caretPositionSelector();

  public CompositeRegistration install() {
    CompositeRegistration reg = new CompositeRegistration();
    reg.add(
      selectedCaretOffset().addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
        @Override
        public void onEvent(PropertyChangeEvent<Integer> event) {
          myPrevXOffset.set(null);
        }
      }),
      focusedView().addHandler(new EventHandler<PropertyChangeEvent<ViewT>>() {
        @Override
        public void onEvent(PropertyChangeEvent<ViewT> event) {
          if (myStackResetEnabled.get()) {
            mySelectionStack.clear();
          }
        }
      }));
    return reg;
  }

  protected void handleMousePress(MouseEvent event) {
    ViewT closest = Composites.<ViewT>findClosest(root(), event.location());
    if (closest != null) {
      focusedView().set(closest);
      if (event.x() < closest.getBounds().origin.x) {
        moveToHome(closest);
      } else {
        moveToEnd(closest);
      }
      event.consume();
    }
  }

  protected void handleKeyPress(KeyEvent event) {
    ViewT current = focusedView().get();
    Integer currentOffset = null;

    if (event.is(Key.UP) || event.is(Key.DOWN)) {
      currentOffset = myPrevXOffset.get();
      if (currentOffset == null) {
        currentOffset = selectedXOffset();
      }
    }

    ViewT next = null;
    boolean restoreOffset = false;

    if (event.is(Key.RIGHT) || event.is(Key.TAB)) {
      next = nextFocusable(current);
      moveToHome(next);
    } else if (event.is(Key.RIGHT, ModifierKey.ALT) || event.is(Key.RIGHT, ModifierKey.CONTROL)) {
      next = nextFocusable(current);
      moveToEnd(next);
    } else if (event.is(Key.LEFT) || event.is(Key.TAB, ModifierKey.SHIFT)) {
      next = prevFocusable(current);
      moveToEnd(next);
    } else if (event.is(Key.LEFT, ModifierKey.ALT) || event.is(Key.LEFT, ModifierKey.CONTROL)) {
      next = prevFocusable(current);
      moveToHome(next);
    } else if (event.is(Key.UP)) {
      next = upperFocusable(current, currentOffset);
      restoreOffset = true;
    } else if (event.is(Key.DOWN)) {
      next = lowerFocusable(current, currentOffset);
      restoreOffset = true;
    } else if (event.is(KeyStrokeSpecs.HOME)) {
      next = Composites.homeElement(current);
      moveToHome(next);
    } else if (event.is(KeyStrokeSpecs.END)) {
      next = Composites.endElement(current);
      moveToEnd(next);
    } else if (event.is(Key.UP, ModifierKey.ALT)) {
      ViewT focusableParent = Composites.focusableParent(current);
      if (focusableParent != null) {
        mySelectionStack.push(current);
        next = focusableParent;
        myStackResetEnabled.set(false);
      }
    } else if (event.is(Key.DOWN, ModifierKey.ALT)) {
      if (mySelectionStack.isEmpty()) {
        next = Composites.firstFocusable(current, false);
      } else {
        next = mySelectionStack.pop();
      }
      myStackResetEnabled.set(false);
    }
    if (next != null) {
      focusedView().set(next);
      scrollTo(next);

      if (restoreOffset) {
        moveCaretTo(next, currentOffset - next.getBounds().origin.x);
        myPrevXOffset.set(currentOffset);
      }

      event.consume();
    }
    myStackResetEnabled.set(true);
  }

  private int selectedXOffset() {
    return selectedCaretOffset().get() + focusedView().get().getBounds().origin.x;
  }

  private ReadableProperty<Integer> selectedCaretOffset() {
    return Properties.select(focusedView(), caretPositionSelector());
  }
}