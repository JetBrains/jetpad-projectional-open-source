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

import jetbrains.jetpad.base.Pair;

/**
 * Split and join items in projectional synchronizer as alternative to simple insert-new and delete.
 */
public interface ItemsSplitterJoiner<SourceItemT, ViewT> {
  /**
   * Dry run of {@link #split(Object, Object)}.
   */
  boolean canSplit(SourceItemT item, ViewT view);

  /**
   * @param item What to split.
   * @param view Contains splitting position so that result may depend on it.
   */
  Pair<SourceItemT, SourceItemT> split(SourceItemT item, ViewT view);

  /**
   * Dry run of {@link #join(Object, Object, JoinDirection)}.
   */
  boolean canJoin(SourceItemT left, SourceItemT right, JoinDirection direction);

  /**
   * @param left Left or upper item.
   * @param right Right or lower item.
   * @param direction See {@link JoinDirection}
   * @return First: the join result. It may be identical (==) to left or right item
   * which means one item has been inserted to inside another and latter is returned
   * as a result. Second: runnable to setup the view (focus).
   */
  Pair<SourceItemT, Runnable> join(SourceItemT left, SourceItemT right, JoinDirection direction);

  class NotSupported<SourceItemT, ViewT> implements ItemsSplitterJoiner<SourceItemT, ViewT> {
    @Override
    public boolean canSplit(SourceItemT item, ViewT view) {
      return false;
    }

    @Override
    public Pair<SourceItemT, SourceItemT> split(SourceItemT item, ViewT view) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean canJoin(SourceItemT left, SourceItemT right, JoinDirection direction) {
      return false;
    }

    @Override
    public Pair<SourceItemT, Runnable> join(SourceItemT left, SourceItemT right, JoinDirection direction) {
      throw new UnsupportedOperationException();
    }
  }

  enum JoinDirection {
    /**
     * Join event is emitted from left (upper) item.
     */
    FORWARD,

    /**
     * Join event is emitted from right (lower) item.
     */
    BACKWARD
  }
}
