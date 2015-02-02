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
package jetbrains.jetpad.completion;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Asyncs;

import java.util.ArrayList;
import java.util.List;

public abstract class AsyncCompletionSupplier {
  public static final AsyncCompletionSupplier EMPTY = new AsyncCompletionSupplier() {
    @Override
    public Async<List<CompletionItem>> get(CompletionParameters cp) {
      return Asyncs.<List<CompletionItem>>constant(new ArrayList<CompletionItem>());
    }

    @Override
    public boolean isEmpty(CompletionParameters cp) {
      return true;
    }
  };

  public abstract Async<List<CompletionItem>> get(CompletionParameters cp);

  public boolean isEmpty(CompletionParameters cp) {
    return false;
  }
}