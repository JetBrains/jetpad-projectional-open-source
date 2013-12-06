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

import jetbrains.jetpad.projectional.cell.Cell;

import java.util.AbstractList;
import java.util.List;

public abstract class SeparatedCellList extends AbstractList<Cell> {
  private List<Cell> myBaseList;

  protected SeparatedCellList(List<Cell> baseList) {
    myBaseList = baseList;
  }

  protected abstract Cell createSeparator(Cell left, Cell right);

  @Override
  public Cell get(int index) {
    return myBaseList.get(index * 2);
  }

  @Override
  public int size() {
    return (myBaseList.size() + 1) / 2;
  }

  @Override
  public void add(int index, Cell element) {
    if (index == 0) {
      if (!myBaseList.isEmpty()) {
        myBaseList.add(0, createSeparator(element, get(0)));
      }
      myBaseList.add(0, element);
    } else {
      myBaseList.add(index * 2 - 1, createSeparator(get(index - 1), element));
      myBaseList.add(index * 2, element);
      if (index + 1 < size()) {
        myBaseList.set(index * 2 + 1, createSeparator(element, get(index + 1)));
      }
    }
  }

  @Override
  public Cell remove(int index) {
    if (index == 0) {
      Cell result = myBaseList.remove(0);
      if (size() != 0) {
        myBaseList.remove(0);
      }
      return result;
    } else {
      myBaseList.remove(index * 2 - 1);
      Cell result = myBaseList.remove(index * 2 - 1);
      if (size() > index) {
        myBaseList.set(index * 2 - 1, createSeparator(get(index - 1), get(index)));
      }
      return result;
    }
  }

  @Override
  public Cell set(int index, Cell element) {
    Cell result = remove(index);
    add(index, element);
    return result;
  }
}