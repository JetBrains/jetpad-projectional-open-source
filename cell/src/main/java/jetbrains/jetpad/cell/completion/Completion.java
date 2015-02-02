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

  public static final CellTraitPropertySpec<AsyncCompletionSupplier> ASYNC_COMPLETION = new CellTraitPropertySpec<AsyncCompletionSupplier>("asyncCompletion", AsyncCompletionSupplier.EMPTY);

  public static Async<List<CompletionItem>> allCompletion(Cell cell, CompletionParameters params) {
    final List<CompletionItem> syncCompletion = cell.get(COMPLETION).get(params);
    AsyncCompletionSupplier async = cell.get(ASYNC_COMPLETION);

    Async<List<CompletionItem>> asyncCompletion = async.get(params);

    final SimpleAsync<List<CompletionItem>> allItems = new SimpleAsync<>();
    asyncCompletion.onResult(new Handler<List<CompletionItem>>() {
      @Override
      public void handle(List<CompletionItem> items) {
        List<CompletionItem> result = new ArrayList<>(items);
        result.addAll(syncCompletion);
        allItems.success(result);
      }
    }, new Handler<Throwable>() {
      @Override
      public void handle(Throwable item) {
        allItems.failure(item);
      }
    });

    return allItems;
  }

  public static boolean isCompletionEmpty(Cell cell, CompletionParameters params) {
    return cell.get(Completion.COMPLETION).isEmpty(params) && cell.get(Completion.ASYNC_COMPLETION).isEmpty(params);
  }
}