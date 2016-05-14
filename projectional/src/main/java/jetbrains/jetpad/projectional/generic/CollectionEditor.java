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
package jetbrains.jetpad.projectional.generic;

import com.google.common.base.Predicate;
import jetbrains.jetpad.base.Pair;
import jetbrains.jetpad.event.Key;
import jetbrains.jetpad.event.KeyEvent;
import jetbrains.jetpad.event.KeyStrokeSpecs;
import jetbrains.jetpad.event.ModifierKey;
import jetbrains.jetpad.model.property.Property;

import java.util.List;

public abstract class CollectionEditor<ItemT, ViewT> {
  private final List<ItemT> myItems;
  private final List<ViewT> myViews;
  private final Property<ItemT> myForDeletion;
  private final boolean myCanCreateNew;
  private final Predicate<ViewT> myReplaceWithNewOnDelete;
  private final int myNumAllowedEmptyLines;
  private final SplitJoinHandler<ItemT, ViewT> mySplitJoinHandler;

  protected CollectionEditor(List<ItemT> items, List<ViewT> views, Property<ItemT> forDeletion, boolean canCreateNew,
      Predicate<ViewT> replaceWithNewOnDelete, int numAllowedEmptyLines, SplitJoinHandler<ItemT, ViewT> splitJoinHandler) {
    myItems = items;
    myViews = views;
    myForDeletion = forDeletion;
    myCanCreateNew = canCreateNew;
    myReplaceWithNewOnDelete = replaceWithNewOnDelete;
    myNumAllowedEmptyLines = numAllowedEmptyLines;
    mySplitJoinHandler = splitJoinHandler;
  }

  protected abstract ItemT newItem();

  protected abstract boolean isEmpty(ViewT item);
  protected abstract boolean isHome(ViewT item);
  protected abstract boolean isEnd(ViewT item);

  protected abstract void selectOnCreation(int index);
  protected abstract void selectHome(int index);
  protected abstract void selectEnd(int index);
  protected abstract void selectPlaceholder();

  protected abstract boolean addAfterParent();

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

  private int getNumEmptyLinesBefore(int index) {
    int count = 0;
    for (int i = index - 1; i >= 0; i--) {
      if (!isEmpty(i)) {
        break;
      }
      count++;
    }
    return count;
  }

  public void handleKey(ViewT cell, KeyEvent event) {
    int index = myViews.indexOf(cell);
    boolean isHome = isHome(cell);
    boolean isEnd = isEnd(cell);

    if (myCanCreateNew) {
      if (event.is(KeyStrokeSpecs.INSERT_AFTER) && isHome) {
        myItems.add(index, newItem());
        if (!isEmpty(index)) {
          selectOnCreation(index);
        } else if (isEmpty(index + 1) && index + 1 == myViews.size() - 1) {
          int numEmptyLinesBefore = getNumEmptyLinesBefore(index);
          if ((index == 0 || numEmptyLinesBefore >= myNumAllowedEmptyLines) && addAfterParent()) {
            myItems.remove(index + 1);
            myItems.remove(index);
            for (int i = 0; i < numEmptyLinesBefore; i++) {
              myItems.remove(index - 1 - i);
            }
          }
        }
        event.consume();
        return;
      }

      if (event.is(KeyStrokeSpecs.INSERT_AFTER) && !isHome) {
        if (isEnd || !trySplit(index, event)) {
          myItems.add(index + 1, newItem());
          selectOnCreation(index + 1);
        }
        event.consume();
        return;
      }

      if (event.is(KeyStrokeSpecs.INSERT_BEFORE)) {
        if (isHome || isEnd || !trySplit(index, event)) {
          int i = isEnd && !isHome ? index + 1 : index;
          myItems.add(i, newItem());
          selectOnCreation(i);
        }
        event.consume();
        return;
      }
    }

    if (event.is(Key.BACKSPACE) && isHome && index > 0 && (isEmpty(index) == isEmpty(index - 1) || !isEmpty(index))) {
      if (!tryJoin(index - 1, index, SplitJoinHandler.JoinDirection.BACKWARD)) {
        myItems.remove(index - 1);
      }
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
      if (!tryJoin(index, index + 1, SplitJoinHandler.JoinDirection.FORWARD)) {
        myItems.remove(index + 1);
      }
      event.consume();
      return;
    }

    if (isSimpleDeleteEvent(event) && myForDeletion.get() != myItems.get(index) && !isEmpty(index)) {
      myForDeletion.set(myItems.get(index));
      event.consume();
      return;
    }

    if (isDeleteEvent(event)) {
      if (myReplaceWithNewOnDelete.apply(myViews.get(index))) {
        myItems.set(index, newItem());
        selectHome(index);
      } else {
        myItems.remove(index);
        selectAfterClear(index);
      }
      event.consume();
    }
  }

  private boolean trySplit(int index, KeyEvent event) {
    ItemT item = myItems.get(index);
    ViewT view = myViews.get(index);
    if (mySplitJoinHandler.canSplit(item, view)) {
      Pair<ItemT, ItemT> separated = mySplitJoinHandler.split(item, view);
      myItems.set(index, separated.first);
      myItems.add(index + 1, separated.second);
      if (event.is(KeyStrokeSpecs.INSERT_BEFORE)) {
        selectEnd(index);
      } else {
        selectHome(index + 1);
      }
      return true;
    }
    return false;
  }

  private boolean tryJoin(int leftIndex, int rightIndex, SplitJoinHandler.JoinDirection direction) {
    ItemT left = myItems.get(leftIndex);
    ItemT right = myItems.get(rightIndex);
    if (mySplitJoinHandler.canJoin(left, right, direction)) {
      Pair<ItemT, Runnable> joinResult = mySplitJoinHandler.join(left, right, direction);
      replaceItems(leftIndex, rightIndex, joinResult.first);
      joinResult.second.run();
      return true;
    }
    return false;
  }

  private void replaceItems(int leftIndex, int rightIndex, ItemT with) {
    if (with == myItems.get(leftIndex)) {
      myItems.remove(rightIndex);
    } else if (with == myItems.get(rightIndex)) {
      myItems.remove(leftIndex);
    } else {
      myItems.remove(rightIndex);
      myItems.set(leftIndex, with);
    }
  }

  private void selectAfterClear(int index) {
    if (isEmpty()) {
      selectPlaceholder();
    } else {
      if (isLast(index) || isFirst(index)) {
        selectHome(index);
      } else {
        selectEnd(index - 1);
      }
    }
  }

  private boolean isDeleteEvent(KeyEvent event) {
    return isAnyPositionDeleteEvent(event) || isSimpleDeleteEvent(event);
  }

  private boolean isSimpleDeleteEvent(KeyEvent event) {
    return event.is(Key.BACKSPACE) || event.is(Key.DELETE);
  }

  private boolean isAnyPositionDeleteEvent(KeyEvent event) {
    return event.is(Key.BACKSPACE, ModifierKey.META) || event.is(Key.DELETE, ModifierKey.META) ||
      event.is(Key.DELETE, ModifierKey.CONTROL) || event.is(Key.BACKSPACE, ModifierKey.CONTROL);
  }
}
