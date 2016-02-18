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
package jetbrains.jetpad.completion;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import jetbrains.jetpad.base.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class CompletionSupplier {
  public static final CompletionSupplier EMPTY = new CompletionSupplier() {
    @Override
    public List<CompletionItem> get(CompletionParameters cp) {
      return Collections.emptyList();
    }
  };

  public static CompletionSupplier create(final List<CompletionItem> items) {
    return new CompletionSupplier() {
      @Override
      public List<CompletionItem> get(CompletionParameters cp) {
        return items;
      }
    };
  }

  public static CompletionSupplier create(CompletionItem... items) {
    return create(Arrays.asList(items));
  }

  public Iterable<CompletionItem> get(CompletionParameters cp) {
    return Collections.emptyList();
  }

  public Async<? extends Iterable<CompletionItem>> getAsync(CompletionParameters cp) {
    return Asyncs.constant(Collections.<CompletionItem>emptyList());
  }

  public final boolean isAsyncEmpty(CompletionParameters cp) {
    Async<? extends Iterable<CompletionItem>> async = getAsync(cp);
    final Value<Boolean> loaded = new Value<>(false);
    final List<CompletionItem> items = new ArrayList<>();
    final Registration reg = async.onSuccess(result -> {
      loaded.set(true);
      items.addAll(FluentIterable.from(result).toList());
    });

    reg.remove();
    if (!loaded.get()) {
      return false;
    } else {
      return items.isEmpty();
    }
  }

  public final boolean isEmpty(CompletionParameters cp) {
    return FluentIterable.from(get(cp)).isEmpty();
  }

}