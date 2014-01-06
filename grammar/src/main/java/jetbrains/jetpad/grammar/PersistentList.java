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
package jetbrains.jetpad.grammar;

import java.util.AbstractList;
import java.util.List;

class PersistentList<ValueT> extends AbstractList<ValueT> {
  static <ValueT> PersistentList<ValueT> nil() {
    PersistentList<ValueT> result = new PersistentList<ValueT>();
    result.myNil = true;
    result.mySize = 0;
    return result;
  }

  static <ValueT> PersistentList<ValueT> cons(ValueT head, PersistentList<ValueT> tail) {
    PersistentList<ValueT> result = new PersistentList<ValueT>();
    result.myHead = head;
    result.myTail = tail;
    result.mySize = tail.size() + 1;
    return result;
  }

  private boolean myNil;
  private ValueT myHead;
  private PersistentList<ValueT> myTail;
  private int mySize;

  private PersistentList() {
  }

  @Override
  public ValueT get(int index) {
    if (myNil) throw new IndexOutOfBoundsException();
    if (index == 0) return myHead;
    return myTail.get(index - 1);
  }

  @Override
  public int size() {
    return mySize;
  }
}