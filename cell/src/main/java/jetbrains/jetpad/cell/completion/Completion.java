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
package jetbrains.jetpad.cell.completion;

import jetbrains.jetpad.base.Async;
import jetbrains.jetpad.base.Handler;
import jetbrains.jetpad.base.SimpleAsync;
import jetbrains.jetpad.cell.Cell;
import jetbrains.jetpad.cell.trait.CellTraitPropertySpec;
import jetbrains.jetpad.completion.*;

import java.util.ArrayList;
import java.util.List;

public class Completion {
  public static final CellTraitPropertySpec<CompletionSupplier> COMPLETION = new CellTraitPropertySpec<>("completion", CompletionSupplier.EMPTY);
  public static final CellTraitPropertySpec<CompletionSupplier> LEFT_TRANSFORM = new CellTraitPropertySpec<>("leftTransform", CompletionSupplier.EMPTY);
  public static final CellTraitPropertySpec<CompletionSupplier> RIGHT_TRANSFORM = new CellTraitPropertySpec<>("rightTransform", CompletionSupplier.EMPTY);
  public static final CellTraitPropertySpec<CompletionController> COMPLETION_CONTROLLER = new CellTraitPropertySpec<>("completionController");

  public static Async<List<CompletionItem>> allCompletion(Cell cell, CompletionParameters params) {
    final CompletionSupplier supplier = cell.get(COMPLETION);
    final List<CompletionItem> syncCompletion = supplier.get(params);
    Async<List<CompletionItem>> asyncCompletion = supplier.getAsync(params);

    final SimpleAsync<List<CompletionItem>> allItems = new SimpleAsync<>();
    asyncCompletion.onResult(items -> {
      List<CompletionItem> result = new ArrayList<>(items);
      result.addAll(syncCompletion);
      allItems.success(result);
    }, allItems::failure);

    return allItems;
  }

  public static boolean isCompletionEmpty(Cell cell, CompletionParameters params) {
    return cell.get(Completion.COMPLETION).isEmpty(params) && cell.get(Completion.COMPLETION).isAsyncEmpty(params);
  }

  public static CompletionItems completionFor(Cell cell, CompletionParameters cp) {
    return completionFor(cell, cp, COMPLETION);
  }

  public static CompletionItems rightTransformFor(Cell cell, CompletionParameters cp) {
    return completionFor(cell, cp, RIGHT_TRANSFORM);
  }

  public static CompletionItems leftTransformFor(Cell cell, CompletionParameters cp) {
    return completionFor(cell, cp, LEFT_TRANSFORM);
  }

  public static CompletionItems completionFor(Cell cell, CompletionParameters cp, CellTraitPropertySpec<CompletionSupplier> prop) {
    return new CompletionItems(cell.get(prop).get(cp));
  }
}