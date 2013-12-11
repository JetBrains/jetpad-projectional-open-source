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
package jetbrains.jetpad.projectional.cell.completion;

import jetbrains.jetpad.projectional.cell.trait.CellTraitPropertySpec;

import java.util.Collections;
import java.util.List;

public class Completion {
  private static final CompletionSupplier EMPTY_SUPPLIER = new CompletionSupplier() {
    @Override
    public List<CompletionItem> get(CompletionParameters cp) {
      return Collections.emptyList();
    }
  };

  public static final CellTraitPropertySpec<CompletionSupplier> COMPLETION = new CellTraitPropertySpec<CompletionSupplier>("completion", EMPTY_SUPPLIER);
  public static final CellTraitPropertySpec<CompletionSupplier> LEFT_TRANSFORM = new CellTraitPropertySpec<CompletionSupplier>("leftTransform", EMPTY_SUPPLIER);
  public static final CellTraitPropertySpec<CompletionSupplier> RIGHT_TRANSFORM = new CellTraitPropertySpec<CompletionSupplier>("rightTransform", EMPTY_SUPPLIER);
  public static final CellTraitPropertySpec<CompletionController> COMPLETION_CONTROLLER = new CellTraitPropertySpec<CompletionController>("completionController");
}