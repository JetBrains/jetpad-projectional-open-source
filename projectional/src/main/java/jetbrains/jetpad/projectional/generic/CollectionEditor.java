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

import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.ModifierKey;

import java.util.List;

public abstract class CollectionEditor<ItemT, ViewT> {
  private List<ItemT> myItems;
  private List<ViewT> myViews;
  private boolean myCanCreateNew;

  protected CollectionEditor(List<ItemT> items, List<ViewT> views, boolean canCreateNew) {
    myItems = items;
    myViews = views;
    myCanCreateNew = canCreateNew;
  }

  protected abstract ItemT newItem();

  protected abstract boolean isEmpty(ViewT item);
  protected abstract boolean isHome(ViewT item);
  protected abstract boolean isEnd(ViewT item);

  protected abstract void selectOnCreation(int index);
  protected abstract void selectHome(int index);
  protected abstract void selectEnd(int index);
  protected abstract void selectPlaceholder();

  private boolean isEmpty() {
    return myItems.isEmpty();
  }

  private boolean isLast(int index) {
    return index == myItems.size() - 1;
  }

  private boolean isFirst(int index) {
    return index == 0;
  }

  private boolean isEmpty(int index) {
    return isEmpty(myViews.get(index));
  }

  public void handleKey(ViewT view, KeyEvent event) {
    int index = myViews.indexOf(view);
    boolean isHome = isHome(view);
    boolean isEnd = isEnd(view);

    if (myCanCreateNew) {
      if (event.is(KeyStrokeSpecs.INSERT_AFTER) && isHome) {
        myItems.add(index, newItem());
        if (!isEmpty(index)) {
          selectOnCreation(index);
        }
        event.consume();
        return;
      }

      if (event.is(KeyStrokeSpecs.INSERT_AFTER) && !isHome) {
        myItems.add(index + 1, newItem());
        selectOnCreation(index + 1);
        event.consume();
        return;
      }

      if ((event.is(KeyStrokeSpecs.INSERT_BEFORE))) {
        int i = isEnd && !isHome ? index + 1 : index;
        myItems.add(i, newItem());
        selectOnCreation(i);
        event.consume();
        return;
      }
    }

    if (event.is(Key.BACKSPACE) && isHome && index > 0 &&
      (isEmpty(index) == isEmpty(index - 1) || !isEmpty(index))) {
      myItems.remove(index - 1);
      event.consume();
      return;
    }

    if (event.is(Key.BACKSPACE) && index > 0 && isEmpty(index) && !isEmpty(index - 1)) {
      myItems.remove(index);
      selectEnd(index - 1);
      event.consume();
      return;
    }

    if (event.is(Key.DELETE) && isEnd && !isLast(index) && (isEmpty(index) == isEmpty(index + 1) || !isEmpty(index))) {
      myItems.remove(index + 1);
      event.consume();
      return;
    }

    if (isDeleteEvent(event)) {
      myItems.remove(index);
      selectAfterClear(index);
      event.consume();
      return;
    }
  }

  private void selectAfterClear(int index) {
    if (isEmpty()) {
      selectPlaceholder();
    } else {
      if (isLast(index)) {
        selectHome(index);
      } else if (isFirst(index)) {
        selectEnd(index);
      } else {
        selectEnd(index - 1);
      }
    }
  }


  protected boolean isDeleteEvent(KeyEvent event) {
    return isAnyPositionDeleteEvent(event) || event.is(Key.BACKSPACE) || event.is(Key.DELETE);
  }

  private boolean isAnyPositionDeleteEvent(KeyEvent event) {
    return event.is(Key.BACKSPACE, ModifierKey.META) || event.is(Key.DELETE, ModifierKey.META) ||
      event.is(Key.DELETE, ModifierKey.CONTROL) || event.is(Key.BACKSPACE, ModifierKey.CONTROL);
  }
}